pluginManagement {
    repositories {
        maven("https://maven.meteorclient.com/")
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("meteordevelopment.meteor-addon") version "1.5.0"
    alias(libs.plugins.fabric.loom)
}
plugins {
    alias(libs.plugins.fabric.loom)
    id("meteordevelopment.meteor-addon") version "1.5.0"

}

version = project.property("mod_version") as String
group = project.property("maven_group") as String

repositories {
    maven("https://maven.meteorclient.com/")
    maven("https://repo.spongepowered.org/repository/maven-public/")
}

dependencies {
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)
    implementation(libs.meteor.client)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get().toInt()))
    }
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "minecraft_version" to libs.versions.minecraft.get(),
            "jdk_version" to libs.versions.jdk.get()
        )

        inputs.properties(propertyMap)
        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    jar {
        inputs.property("archivesName", project.base.archivesName.get())

        from("LICENSE") {
            rename { "${it}_${inputs.properties["archivesName"]}" }
        }
    }
}

withType<JavaCompile>().configureEach {
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:deprecation",
            "-Xlint:unchecked"
        )
    )
}
