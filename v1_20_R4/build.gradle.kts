import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.kotlin.dsl.*
import org.gradle.api.artifacts.Configuration

plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.0.0-rc3"
}

dependencies {
    // Local API module
    api(project(":API"))
    implementation(project(":MAIN"))

    // Compile Only
    compileOnly("org.spigotmc:spigot:1.20.6-R0.1-SNAPSHOT") {
        artifact {
            classifier = "remapped-mojang"
        }
    }

    compileOnly(libs.vault.api)
    compileOnly(libs.protocol.lib)
    compileOnly(libs.phtree)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.worldguard.bukkit)

    // Shade
    implementation(libs.libby)
    implementation(libs.adventure.platform.bukkit)
    implementation(libs.adventure.text.minimessage)
}

configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

val shadowDependencies: Configuration by configurations.creating {
    extendsFrom(configurations.runtimeClasspath.get())
}

tasks.named<ShadowJar>("shadowJar") {
    configurations = listOf(shadowDependencies)

    // Explicitly add the project's own classes to the ShadowJar
    from(sourceSets.main.get().output)

    // Set the output directory to the parent project's build/libs folder
    archiveFileName.set("Citizens-${project.name}-${project.version}.jar")
    destinationDirectory.set(layout.projectDirectory.dir("../build/libs"))

    // Dynamically relocate all dependencies from the resolved configuration
    shadowDependencies.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        val group = artifact.moduleVersion.id.group
        if (group != "net.citizensnpcs" && !group.startsWith("org.jetbrains.kotlin")) {
            relocate(group, "net.citizensnpcs.libs.$group")
        }
    }
}

// Javadocs & Sources

tasks.register<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.register<Jar>("javadocJar") {
    archiveClassifier.set("javadoc")
    from(tasks.javadoc.get().destinationDir)
    dependsOn(tasks.javadoc)
}

tasks.withType<Javadoc> {
    options.apply {
        isFailOnError = false
        (this as CoreJavadocOptions).addStringOption("link", "https://hub.spigotmc.org/javadocs/spigot")
        this.addStringOption("link", "https://jd.advntr.dev/platform/bukkit/4.4.1/")
        this.addStringOption("link", "https://jd.advntr.dev/api/4.24.0/")
        this.addStringOption("Xdoclint:none", "-quiet")
    }
}

// Publishing
publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            groupId = project.group.toString()
            artifactId = "citizens-${project.name}"
            version = project.version.toString()
            artifact(tasks.named("sourcesJar").get())
            artifact(tasks.named("javadocJar").get())
        }
    }
}