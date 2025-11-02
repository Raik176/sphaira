import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.fabricmc.loom.task.RemapJarTask
import net.fabricmc.loom.task.RemapSourcesJarTask
import org.gradle.kotlin.dsl.repositories
import org.gradle.kotlin.dsl.withType
import java.net.HttpURLConnection
import java.net.URI

buildscript {
    configurations.all {
        resolutionStrategy {
            force(
                "com.fasterxml.jackson.core:jackson-core:2.17.1",
                "com.fasterxml.jackson.core:jackson-databind:2.17.1",
                "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1",
                "com.fasterxml.jackson.core:jackson-annotations:2.17.1"
            )
        }
    }
}

plugins {
    id("dev.kikugie.stonecutter")
    id("dev.architectury.loom") version "1.11-SNAPSHOT" apply false
    id("architectury-plugin") version "3.4-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.0.0"
    id("se.bjurr.gitchangelog.git-changelog-gradle-plugin") version "3.1.1"
    id("com.gradleup.shadow") version "9.2.2" apply false

    kotlin("jvm") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false

    id("dev.kikugie.fletching-table") version "0.1.0-alpha.22" apply false
    id("dev.kikugie.fletching-table.fabric") version "0.1.0-alpha.22" apply false
    id("dev.kikugie.fletching-table.lexforge") version "0.1.0-alpha.22" apply false
    id("dev.kikugie.fletching-table.neoforge") version "0.1.0-alpha.22" apply false
}
stonecutter active "1.20.6" /* [SC] DO NOT EDIT */

val changelogProvider = layout.buildDirectory.file("CHANGELOG.md")
changelogProvider.get().asFile.apply {
    if (!exists()) {
        parentFile?.mkdirs()
        createNewFile()
    }
}

val changelogContentsProvider = providers.fileContents(changelogProvider)

tasks.register("build") {
    group = "build"
    description = "Builds all subprojects"

    subprojects.forEach { sub ->
        sub.tasks.findByName("build")?.let { buildTask ->
            dependsOn(buildTask)
        }
    }
}

for (node in stonecutter.tree.nodes) {
    val minecraft = node.metadata.version

    node.project.repositories {
        fun strictMaven(url: String, alias: String, vararg groups: String) = exclusiveContent {
            forRepository { maven(url) { name = alias } }
            filter { groups.forEach(::includeGroup) }
        }
        strictMaven("https://www.cursemaven.com", "Curseforge", "curse.maven")
        strictMaven("https://api.modrinth.com/maven", "Modrinth", "maven.modrinth")
    }

    node.project.plugins.apply("org.jetbrains.kotlin.jvm")
    node.project.plugins.apply("com.google.devtools.ksp")
    node.project.plugins.apply("com.gradleup.shadow")
    node.project.plugins.apply("maven-publish")

    node.project.afterEvaluate {
        val projectStonecutter = node.project.extensions.getByType<dev.kikugie.stonecutter.build.StonecutterBuildExtension>()
        val loader = node.project.prop("loom.platform") ?: "common"
        val common: Project = requireNotNull(projectStonecutter.node.sibling("")) {
            "No common project for $project"
        }.project

        val commonBundle: Configuration by node.project.configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        val shadowBundle: Configuration by node.project.configurations.creating {
            isCanBeConsumed = false
            isCanBeResolved = true
        }
        val fancyName = when (node.branch.id) {
            "fabric" -> "Fabric"
            "forge" -> "Forge"
            "neoforge" -> "NeoForge"
            "" -> "Common"
            else -> {
                TODO("Invalid branch: ${node.branch.id}")
            }
        }

        node.project.dependencies {
            add("minecraft", "com.mojang:minecraft:$minecraft")
            add(
                "mappings",
                node.project.extensions.getByType<net.fabricmc.loom.api.LoomGradleExtensionAPI>().officialMojangMappings()
            )
        }

        node.project.configurations {
            named("compileClasspath") { extendsFrom(commonBundle) }
            named("runtimeClasspath") { extendsFrom(commonBundle) }
            maybeCreate("development$fancyName").extendsFrom(commonBundle)
        }

        node.project.dependencies {
            if (loader != "common") {
                commonBundle(project(common.path, "namedElements")) { isTransitive = false }
                shadowBundle(project(common.path, "transformProduction$fancyName")) { isTransitive = false }
            }
        }

        node.project.tasks.named<ShadowJar>("shadowJar") {
            configurations = listOf(shadowBundle)
            archiveClassifier = "dev-shadow"
        }

        node.project.tasks.named<RemapJarTask>("remapJar") {
            injectAccessWidener = true
            inputFile = node.project.tasks.shadowJar.get().archiveFile
            archiveClassifier = null
            dependsOn(node.project.tasks.shadowJar)
        }

        node.project.tasks.named<Jar>("jar") {
            archiveClassifier = "dev"
        }

        node.project.extensions.configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    node.project.tasks.named(if (loader == "common") "jar" else "remapJar").let {
                        artifact(it) {
                            classifier = null
                            builtBy(it)
                        }
                    }
                    node.project.tasks.named(if (loader == "common") "sourcesJar" else "remapSourcesJar").let {
                        artifact(it) {
                            classifier = "sources"
                            builtBy(it)
                        }
                    }

                    groupId = mod.group
                    artifactId = mod.id
                    version = "${mod.version}+$minecraft-$loader"
                }
            }

            val repoUrl = System.getenv("REPOSILITE_URL")
            val repoUser = System.getenv("REPOSILITE_USERNAME")
            val repoPass = System.getenv("REPOSILITE_PASSWORD")
            if (repoUrl != null && repoUser != null && repoPass != null) {
                repositories {
                    maven {
                        url = uri(repoUrl)

                        credentials(PasswordCredentials::class.java) {
                            username = repoUser
                            password = repoPass
                        }

                        authentication {
                            create<BasicAuthentication>("basic")
                        }
                    }
                }
            }
        }

        if (loader != "common") {
            node.project.tasks.withType<RemapJarTask> {
                destinationDirectory = rootProject.layout.buildDirectory.dir("libs/${mod.version}/$loader")
            }
            node.project.tasks.withType<RemapSourcesJarTask> {
                destinationDirectory = rootProject.layout.buildDirectory.dir("libs/${mod.version}/$loader")
            }

            node.project.extensions.configure<me.modmuss50.mpp.ModPublishExtension> {
                file.set(node.project.tasks.named("remapJar", RemapJarTask::class.java).flatMap { it.archiveFile })
                changelog = changelogContentsProvider.asText
                type = STABLE

                modrinth {
                    accessToken = providers.environmentVariable("MODRINTH_API_KEY")
                    projectId = mod.prop("modrinthId")
                    minecraftVersions = common.mod.prop("mc_targets").split(" ")
                    projectDescription = providers.fileContents(rootProject.layout.projectDirectory.file("README.md")).asText
                }
                curseforge {
                    accessToken = providers.environmentVariable("CF_API_KEY")
                    projectId = mod.prop("curseforgeId")
                    minecraftVersions = common.mod.prop("mc_targets").split(" ")
                }

                dryRun = providers.environmentVariable("PUBLISH_DRY_RUN").isPresent
            }
        }

        node.project.version = "${mod.version}+$minecraft"
        node.project.group = "${mod.group}.$loader"
        node.project.extensions.configure<BasePluginExtension> {
            archivesName.set("${mod.id}-$loader")
        }

        node.project.extensions.configure<JavaPluginExtension> {
            val java = when {
                projectStonecutter.eval(minecraft, ">=1.20.5") -> JavaVersion.VERSION_21
                projectStonecutter.eval(minecraft, ">=1.17") -> JavaVersion.VERSION_17
                else -> JavaVersion.VERSION_1_8
            }
            targetCompatibility = java
            sourceCompatibility = java
        }

        node.project.extensions.configure<net.fabricmc.loom.api.LoomGradleExtensionAPI> {
            if (node.branch.id == "forge") {
                forge {
                    convertAccessWideners = true
                    forge.mixinConfigs("${mod.id}-common.mixins.json")
                }
            }

            accessWidenerPath = rootProject.file("src/main/resources/${mod.id}.accesswidener")

            decompilers {
                get("vineflower").apply { // Adds names to lambdas - useful for mixins
                    options.put("mark-corresponding-synthetics", "1")
                }
            }

            runConfigs.all {
                isIdeConfigGenerated = false
                runDir = project.layout.projectDirectory.asFile.toPath().toAbsolutePath()
                    .relativize(rootProject.layout.projectDirectory.file("run").asFile.toPath())
                    .toString()
                vmArgs(
                    "-Dmixin.debug.export=true",
                    "-XX:+AllowEnhancedClassRedefinition"
                )
            }
        }
    }

    if (!node.branch.id.isEmpty() && node.metadata.version == stonecutter.current?.version) {
        for (type in listOf("Client", "Server")) tasks.register("runActive$type${node.branch.id.upperCaseFirst()}") {
            group = "project"
            dependsOn("${node.hierarchy}:run$type")
        }
    }
}

tasks.named("publishMods") {
    enabled = false
}

tasks.register("publishMod") {
    group = "publishing"

    dependsOn(tasks.gitChangelog)

    stonecutter.tree.nodes.forEach {
        it.project.tasks.findByName("publishMods")?.let { publishTask ->
            dependsOn(publishTask)
        }

        it.project.tasks.findByName("publish")?.let { publishTask ->
            dependsOn(publishTask)
        }
    }

    dependsOn(tasks.named("publishGithub"))
}

tasks.named("publishMods") {
    enabled = false
}

publishMods {
    version = mod.version
    changelog = changelogContentsProvider.asText
    type = STABLE
    displayName = mod.version

    github {
        changelog = rootProject.publishMods.changelog
        accessToken = providers.environmentVariable("GITHUB_TOKEN")
        repository = mod.prop("github")
        commitish = "main"
        tagName = "v${mod.version}"

        allowEmptyFiles = true
    }

    dryRun = providers.environmentVariable("PUBLISH_DRY_RUN").isPresent
}

fun getLatestTag(): String {
    val url = URI("https://api.github.com/repos/${mod.prop("github")}/tags").toURL()
    val connection = url.openConnection() as HttpURLConnection

    val response = connection.inputStream.bufferedReader().readText()
    val json = Json.parseToJsonElement(response).jsonArray

    return json[0].jsonObject["name"]!!.jsonPrimitive.content
}

tasks.gitChangelog {
    file.set(changelogProvider.map { it.asFile })
    fromRevision.set(getLatestTag())
    toRevision.set("HEAD")

    doLast {
        val changelogFile = tasks.gitChangelog.flatMap { it.file }.get()
        if (!changelogFile.exists())
            return@doLast

        fun decodeHtmlNumericEntities(input: String): String {
            val regex = Regex("&#(x?)([0-9A-Fa-f]+);")
            return regex.replace(input) { match ->
                val isHex = match.groupValues[1].equals("x", ignoreCase = true)
                val numPart = match.groupValues[2]
                val codePoint = runCatching {
                    if (isHex) numPart.toInt(16) else numPart.toInt()
                }.getOrNull()

                if (codePoint != null && Character.isValidCodePoint(codePoint))
                    String(Character.toChars(codePoint))
                else
                    match.value
            }
        }

        val original = changelogFile.readText()
        var processed = decodeHtmlNumericEntities(original)

        processed = Regex("""(?<=- )(.*?)(?= \(\[)""").replace(processed) { match ->
            match.value.replace(Regex("#(\\d+)")) { issueMatch ->
                val issueNumber = issueMatch.groupValues[1]
                "[#$issueNumber](https://github.com/${mod.prop("github")}/issues/$issueNumber)"
            }
        }

        if (processed != original) {
            changelogFile.writeText(processed)
        }
    }

    templateContent.set("""
{{#ifContainsType commits type='feat'}}
### ✨ Features
{{#commits}}
  {{#ifCommitType . type='feat'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='fix'}}
### 🐛 Bug Fixes
{{#commits}}
  {{#ifCommitType . type='fix'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='chore'}}
### 🧹 Chores
{{#commits}}
  {{#ifCommitType . type='chore'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='docs'}}
### 📚 Documentation
{{#commits}}
  {{#ifCommitType . type='docs'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='refactor'}}
### ♻️ Refactors
{{#commits}}
  {{#ifCommitType . type='refactor'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='perf'}}
### ⚡ Performance
{{#commits}}
  {{#ifCommitType . type='perf'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='test'}}
### 🧪 Tests
{{#commits}}
  {{#ifCommitType . type='test'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='ci'}}
### 🤖 CI
{{#commits}}
  {{#ifCommitType . type='ci'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
{{#ifContainsType commits type='revert'}}
### ⏪ Reverts
{{#commits}}
  {{#ifCommitType . type='revert'}}
- {{#eachCommitScope .}} **{{.}}** {{/eachCommitScope}}{{commitDescription .}} ([{{hash}}](https://github.com/{{ownerName}}/{{repoName}}/commit/{{hash}}))
  {{/ifCommitType}}
{{/commits}}
{{/ifContainsType}}
""")
}