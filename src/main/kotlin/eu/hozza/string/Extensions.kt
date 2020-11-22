package eu.hozza.string

fun String.sorted(): String {
    return toCharArray().sorted().joinToString("")
}

operator fun Char.times(n: Int): String = this.toString().repeat(n)
