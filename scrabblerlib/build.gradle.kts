plugins {
    id("scrabbler.kotlin-library-conventions")
}

group = "com.mhozza.scrabbler"

dependencies {
    implementation(project(":datastructures"))
    implementation(project(":string"))
    implementation("org.apache.commons:commons-lang3:3.11")
}
