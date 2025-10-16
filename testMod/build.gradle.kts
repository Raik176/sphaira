plugins {
    id("fabric-loom") version "1.11-SNAPSHOT"
}

val minecraft = "1.21"

repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }

    maven { url = uri("https://maven.shedaniel.me/") }
    maven { url = uri("https://maven.terraformersmc.com/releases/") }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-loader:0.17.2")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.0+$minecraft")
    modImplementation(project(":fabric:$minecraft"))
    modCompileOnly("me.shedaniel.cloth:cloth-config-fabric:15.0.140") {
        exclude("net.fabricmc.fabric-api")
    }
}