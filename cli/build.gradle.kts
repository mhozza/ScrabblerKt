plugins {
    id("scrabbler.kotlin-library-conventions")
}

//group = "eu.hozza"
//version = "1.0.2-SNAPSHOT"

repositories {
  maven { url = uri("https://kotlin.bintray.com/kotlinx") }
}

//tasks.jar {
//    manifest {
//        attributes(
//            mapOf(
//                "Main-Class" to "eu.hozza.scrabbler.cli.ScrabblerMainKt",
//                "Implementation-Title" to project.name,
//                "Implementation-Version" to project.version
//            )
//        )
//    }
//    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
//}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3")
}
