import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("fabric-loom")
    `maven-publish`
    java
}

group = property("maven_group")!!
version = property("mod_version")!!

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.

    maven("https://maven.frohnmeyer-wds.de/artifacts") {
        name = "Entry Widgets"
        content {
            includeGroup("io.gitlab.jfronny.libjf")
        }
    }

    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.shedaniel.me/")
}

loom {
    accessWidenerPath.set(file("src/main/resources/respackdeletebutton.accesswidener"))
}

dependencies {
    include(modImplementation("io.gitlab.jfronny.libjf:libjf-resource-pack-entry-widgets-v0:3.17.0-SNAPSHOT")!!)
    modImplementation("com.terraformersmc:modmenu:${property("modmenu_ver")}")
    modApi("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config")}") {
        exclude("net.fabricmc.fabric-api")
    }

    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    modImplementation("net.fabricmc:fabric-language-kotlin:${property("fabric_kotlin_version")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_api_version")}")
}

tasks {

    processResources {
        inputs.property("version", project.version)
        filesMatching("fabric.mod.json") {
            expand(getProperties())
            expand(mutableMapOf("version" to project.version))
        }
    }

    jar {
        from("LICENSE")
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                artifact(remapJar) {
                    builtBy(remapJar)
                }
                artifact(kotlinSourcesJar) {
                    builtBy(remapSourcesJar)
                }
            }
        }

        // select the repositories you want to publish to
        repositories {
            // uncomment to publish to the local maven
            // mavenLocal()
        }
    }

    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }

}

java {
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}



// configure the maven publication
