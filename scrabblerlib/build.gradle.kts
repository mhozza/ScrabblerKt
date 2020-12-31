plugins {
    id("scrabbler.kotlin-library-conventions")
}

group = "com.mhozza.scrabbler"

dependencies {
    implementation(project(":datastructures"))
    implementation(project(":string"))
}
