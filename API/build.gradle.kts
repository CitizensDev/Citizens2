/*
 * Citizens3 API Build File
 */
description = "CitizensAPI"

plugins {
    `java-library`
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    // Use JUnit Jupiter for testing.
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
