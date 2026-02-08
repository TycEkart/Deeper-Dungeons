plugins {
    kotlin("multiplatform") version "2.2.20"
    id("org.jetbrains.compose") version "1.6.10"
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.20"
    kotlin("plugin.serialization") version "2.2.20"
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
                implementation("io.ktor:ktor-client-core:2.3.10")
                implementation("io.ktor:ktor-client-js:2.3.10")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.10")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.10")
            }
        }
    }
}