plugins {
    kotlin("jvm") version "1.4.10"
}

group = "eu.hozza"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
}

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "eu.hozza.scrabbler.ScrabblerMainKt",
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version
            )
        )
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
