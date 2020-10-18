@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

//import eu.hozza.datastructures.tree.print
import eu.hozza.datastructures.trie.Trie
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File

fun buildTrie(words: List<String>): Trie {
    val trie = Trie()

    for (w in words) {
        trie.add(w)
    }

    return trie
}

fun loadDictionary(fname: String): List<String> {
    return File(fname).readLines().map { it.trim().toLowerCase() }
}

fun filterDictionary(words: List<String>, letters: String, wildcard: Char? = null, useAllLetters: Boolean = true, prefix: String? = null): List<String> {
    val sortedLetters = letters.sort()
    val letterSet = letters.toSet()
    val numWilddcards = if (wildcard == null) 0 else letters.count { it == wildcard }

    fun isValidWordWithoutPrefix(word: String): Boolean {
        if (word.length > letters.length) return false
        if (useAllLetters && word.length != letters.length) return false
        if ((word.toSet() - letterSet).size > numWilddcards) return false
        if (wildcard == null && useAllLetters && word.sort() != sortedLetters) return false
        return true
    }

    fun isValidWord(word: String): Boolean {
        var _word = word
        if (!prefix.isNullOrEmpty()) {
            if (!word.startsWith(prefix)) return false
            _word = word.substring(prefix.length)
        }
        return isValidWordWithoutPrefix(_word)
    }
    return words.filter { isValidWord(it) }
}

fun main(args: Array<String>) {
    val parser = ArgParser("playground")
    val dict by parser.option(ArgType.String, shortName = "d").required()
    parser.parse(args)

    val words = loadDictionary(dict)
    println(words.size)

    val trie = buildTrie(words)
    println(trie.root.getChildren())
}

fun String.sort(): String {
    return toCharArray().sorted().joinToString("")
}