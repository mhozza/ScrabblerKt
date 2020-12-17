plugins {
    id("scrabbler.kotlin-library-conventions")
}

group = "eu.hozza.cli"

repositories {
  maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
}
