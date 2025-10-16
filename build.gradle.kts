plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")

    id("dev.kikugie.fletching-table")

    id("maven-publish")
}

val minecraft = stonecutter.current.version

architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.project.prop("loom.platform")
})

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")

    compileOnly("org.jetbrains:annotations:${mod.dep("jetbrains_annotations")}")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "sphaira"
        }
    }

    val repoUrl = System.getenv("REPOSILITE_URL")
    val repoUser = System.getenv("REPOSILITE_USERNAME")
    val repoPass = System.getenv("REPOSILITE_PASSWORD")
    if (System.getenv("CI") == "true" && repoUrl != null && repoUser != null && repoPass != null) {
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