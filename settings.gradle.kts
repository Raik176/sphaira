pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net")
        maven("https://maven.architectury.dev")
        maven("https://maven.minecraftforge.net")
        maven("https://maven.neoforged.net/releases")
        maven("https://maven.kikugie.dev/snapshots")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.8-alpha.7"
}

stonecutter {
    centralScript = "build.gradle.kts"
    kotlinController = true
    create(rootProject) {
        versions(
            "1.17.1",
            "1.18", "1.18.2",
            "1.19", "1.19.1", "1.19.3", "1.19.4",
            "1.20", "1.20.4", "1.20.6",
            "1.21", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.9"
        )
        vcsVersion = "1.20.6"
        branch("fabric")
        branch("forge")
        branch("neoforge") { versions(
            "1.20.4", "1.20.6",
            "1.21", "1.21.3", "1.21.4", "1.21.5", "1.21.6", "1.21.9"
        ) }
    }
}

rootProject.name = "Sphaira"