plugins {
    kotlin("jvm") version "1.9.23"
    id("fabric-loom") version "1.6-SNAPSHOT"
}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

repositories {
    maven("https://maven.meteorclient.com/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    mavenCentral()
}

dependencies {
    minecraft("com.mojang:minecraft:1.21.11")
    mappings(loom.layered {
        officialMojangMappings()
    })
    modImplementation("net.fabricmc:fabric-loader:0.16.5")
    
    // Meteor Client dependency
    modImplementation("meteordevelopment:meteor-client:0.5.7")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
    withSourcesJar()
}

tasks.processResources {
    val propertyMap = mapOf(
        "version" to project.version,
        "minecraft_version" to "1.21.11",
        "jdk_version" to "21"
    )
    inputs.properties(propertyMap)
    filesMatching("fabric.mod.json") {
        expand(propertyMap)
    }
}
