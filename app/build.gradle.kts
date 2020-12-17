plugins {
    id("scrabbler.kotlin-application-conventions")
}

group = "eu.hozza.scrabbler.cli"

repositories {
    mavenCentral()
    maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
    implementation(project(":scrabblerlib"))
    implementation(project(":datastructures"))
    implementation(project(":cli"))
}

application {
    mainClass.set("eu.hozza.scrabbler.cli.ScrabblerMainKt")
}
