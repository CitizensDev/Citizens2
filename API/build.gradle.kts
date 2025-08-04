description = "CitizensAPI"

plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.placeholder.api)
    compileOnly(libs.worldguard.bukkit)
    compileOnly(libs.vault.api)
    compileOnly(libs.phtree)
    implementation(libs.adventure.text.minimessage)
    implementation(libs.adventure.platform.bukkit)

    // Tests
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(libs.spigot.api)
    testImplementation(libs.adventure.text.minimessage)
    testImplementation(libs.adventure.platform.bukkit)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
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
            artifactId = "citizensapi"
            version = project.version.toString()
            artifact(tasks.named("sourcesJar").get())
            artifact(tasks.named("javadocJar").get())
        }
    }
}