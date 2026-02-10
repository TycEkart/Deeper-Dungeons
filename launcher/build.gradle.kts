plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("edu.sc.seis.launch4j") version "4.0.0"
}

// Version is inherited from root project

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":backend"))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

val copyFrontend by tasks.registering(Copy::class) {
    dependsOn(":frontend:jsBrowserDistribution")
    from(project(":frontend").layout.buildDirectory.dir("dist/js/productionExecutable"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

tasks.named("processResources") {
    dependsOn(copyFrontend)
}

tasks.bootJar {
    mainClass.set("com.deeperdungeons.launcher.LauncherKt")
    launchScript()
}

launch4j {
    productName = "Deeper Dungeons"
    outfile = "Deeper Dungeons.exe"
    icon = "${project.projectDir}/src/main/resources/logo.ico"
    headerType = "gui"
    jreMinVersion = "21"
    downloadUrl = "https://adoptium.net/temurin/releases/?version=21"
    copyConfigurable = listOf(tasks.bootJar)
    setJarFiles(project.files(tasks.bootJar))
}

// Disable the task that copies dependencies to a lib folder
tasks.matching { it.name.startsWith("copyL4jLib") }.configureEach {
    enabled = false
}