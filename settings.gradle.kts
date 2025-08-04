rootProject.name = "Citizens3"
include("API")
include("MAIN")
include("v1_21_R5")
include("v1_20_R4")
include("v1_19_R3")

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenLocal()
        mavenCentral()
    }
}

plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}