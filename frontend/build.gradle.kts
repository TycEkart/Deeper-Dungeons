plugins {
    kotlin("multiplatform") version "2.2.20"
    id("org.jetbrains.compose") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
        }
        binaries.executable()
    }
    
    sourceSets {
        val jsMain by getting {
            dependencies {
                implementation(project(":shared"))
                implementation(compose.html.core)
                implementation(compose.runtime)
            }
        }
    }
}