plugins {
    id("dev.architectury.loom")
    id("architectury-plugin")

    id("dev.kikugie.fletching-table")
}

val minecraft = stonecutter.current.version

architectury.common(stonecutter.tree.branches.mapNotNull {
    if (stonecutter.current.project !in it) null
    else it.project.prop("loom.platform")
})

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${mod.dep("fabric_loader")}")

    compileOnly("org.jetbrains:annotations:${mod.dep("jetbrains_annotations")}")
    "io.github.llamalad7:mixinextras-common:${mod.dep("mixin_extras")}".let {
        annotationProcessor(it)
        compileOnly(it)
    }
}

tasks.remapSourcesJar {
    dependsOn(tasks.jar)
}

java {
    withSourcesJar()
}