plugins {
    id("scrabbler.kotlin-application-conventions")
}

//group = "eu.hozza.scrabbler.cli"
//version = "1.0.2-SNAPSHOT"

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
