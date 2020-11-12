plugins {
    kotlin("jvm") version "1.4.10"
    `maven-publish`
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

publishing {
    repositories {
        maven {
            name = "ScrabblerKt"
            url = uri("https://maven.pkg.github.com/mhozza/ScrabblerKt")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["kotlin"])
            artifactId = "scrabbler"
        }
    }
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
