import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.kotlin.dsl.*
import org.gradle.api.plugins.JavaPluginExtension

description = "Citizens Parent BS"

allprojects {
    group = "net.citizensnpcs"
    version = "2.0.39-SNAPSHOT"
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://jitpack.io")
        maven("https://repo.dmulloy2.net/repository/public/")
        maven("https://repo.byteflux.net/repository/maven-releases/")
        maven("https://repo.alessiodp.com/releases")
    }
}

subprojects {
    apply(plugin = "java-library")
    configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}