plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization") version "2.2.20"
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }
    }
}