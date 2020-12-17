plugins {
    id("scrabbler.kotlin-library-conventions")
}

group = "eu.hozza.scrabbler"

dependencies {
    implementation(project(":datastructures"))
    implementation(project(":string"))
}
