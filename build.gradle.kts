plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    kotlin("jvm") version "2.2.20" apply false
    kotlin("multiplatform") version "2.2.20" apply false
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
    kotlin("plugin.spring") version "2.2.20" apply false
    id("org.jetbrains.compose") version "1.6.10" apply false
}

val projectVersion: String by project

allprojects {
    group = "org.example"
    version = projectVersion

    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}