package eu.hozza.string

fun String.sorted(): String {
    return toCharArray().sorted().joinToString("")
}