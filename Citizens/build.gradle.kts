import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.*
import org.gradle.api.artifacts.Configuration

plugins {
    id("java-library")
    id("com.gradleup.shadow") version "9.0.0-rc3"
}

dependencies {
    // Local API module
    api(project(":API"))

    // Compile Only
    compileOnly(libs.spigot)
    // Don't use libs.spigot.api, unless you want to keep the commented section below up to date.

    compileOnly(libs.vault.api)
    compileOnly(libs.protocol.lib)
    compileOnly(libs.phtree)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.worldguard.bukkit)

    //Only use if you just want to depend on spigot-api, instead of the full server
    //Here be dragons
    // compileOnly("com.mojang:authlib:6.0.58")
    // compileOnly("it.unimi.dsi:fastutil:8.5.16")
    // compileOnly("org.apache.logging.log4j:log4j-core:2.23.1")
    // compileOnly("io.netty:netty-all:4.1.122.Final")
    // compileOnly("org.slf4j:slf4j-api:2.0.13")

    // Shade
    implementation(libs.libby)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.adventure.text.minimessage)
}

val shadowDependencies: Configuration by configurations.creating {
    extendsFrom(configurations.runtimeClasspath.get())
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowDependencies)

    // Explicitly add the project's own classes to the ShadowJar
    from(sourceSets.main.get().output)

    // Set the output directory to the parent project's build/libs folder
    archiveFileName.set("Citizens.jar")
    destinationDirectory.set(layout.projectDirectory.dir("../build/libs"))

    // Dynamically relocate all dependencies from the resolved configuration
    shadowDependencies.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        val group = artifact.moduleVersion.id.group
        if (group != "net.citizensnpcs" && !group.startsWith("org.jetbrains.kotlin")) {
            relocate(group, "net.citizensnpcs.libs.$group")
        }
    }
}