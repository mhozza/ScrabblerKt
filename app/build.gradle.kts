plugins {
    id("scrabbler.kotlin-application-conventions")
}

group = "com.mhozza.scrabbler.cli"

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
    mainClass.set("com.mhozza.scrabbler.cli.ScrabblerMainKt")
}
