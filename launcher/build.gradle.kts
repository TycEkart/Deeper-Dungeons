plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("edu.sc.seis.launch4j") version "4.0.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

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

//launch4j {
//    mainClassName = "com.deeperdungeons.launcher.LauncherKt"
//    // icon = "${project.rootDir}/frontend/src/jsMain/resources/logo.ico"
//    outfile = "DeeperDungeons.exe"
//    headerType = "console"
//    jreMinVersion = "21"
//    //downloadUrl = "https://adoptium.net/temurin/releases/?version=21"
//
//    // Configure Launch4j to use the Spring Boot fat jar
//    copyConfigurable = listOf(tasks.bootJar)
//    // Use backticks to escape the 'jar' property name to avoid conflict with the 'jar' task
//    //`jar` = "lib/${tasks.bootJar.get().archiveFileName.get()}"
//}

tasks.withType<edu.sc.seis.launch4j.tasks.DefaultLaunch4jTask> {
    outfile.set("${rootProject.name}.exe")
    mainClassName.set("com.deeperdungeons.launcher.LauncherKt")
    icon.set("${project.projectDir}/src/main/resources/logo.ico")
    productName.set("My App")
}