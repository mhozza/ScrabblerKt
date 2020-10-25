@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

import eu.hozza.datastructures.trie.Trie
import eu.hozza.cli.CharArg
import kotlinx.cli.*
import java.io.File

fun main(args: Array<String>) {
    val parser = ArgParser("Scrabbler")
    val word by parser.argument(ArgType.String, description = "Input word").optional()
    val dict by parser.option(ArgType.String, shortName = "d", description = "List of words to search in.").required()
    val limit by parser.option(ArgType.Int, shortName = "l", description = "Limit the number of words printed.")
    val prefix by parser.option(ArgType.String, description = "Only print words starting with the specified prefix.")
    val allowShorter by parser.option(ArgType.Boolean, "Don't require using all letters.").default(false)
    val wildcard by parser.option(CharArg, description = "Set a wildcard for the permutation matching.")
    val regex by parser.option(ArgType.Boolean, shortName = "r", description = "Print words matching regex.")
        .default(false)
    parser.parse(args)

    var words = loadDictionary(dict)

    if (word != null) {
        var trie: Trie? = null
        if (!regex) {
            words = filterDictionary(words, word!!, prefix = prefix, wildcard = wildcard, useAllLetters = !allowShorter)
            if (allowShorter || wildcard != null) {
                trie = buildTrie(words)
            }
        }
        Scrabbler(words, trie, isFiltered = true).answer(word!!, regex, limit, allowShorter, wildcard, prefix)
    }
}

fun buildTrie(words: List<String>): Trie {
    val trie = Trie()
    trie.addAll(words)
    return trie
}

fun loadDictionary(fname: String): List<String> {
    return File(fname).readLines().map { it.trim().toLowerCase() }
}

fun filterDictionary(
    words: List<String>,
    letters: String,
    wildcard: Char? = null,
    useAllLetters: Boolean = true,
    prefix: String? = null
): List<String> {
    val sortedLetters = letters.sorted()
    val letterSet = letters.toSet()
    val numWildcards = if (wildcard == null) 0 else letters.count { it == wildcard }

    fun isValidWordWithoutPrefix(word: String): Boolean {
        if (word.length > letters.length) return false
        if (useAllLetters && word.length != letters.length) return false
        if ((word.toSet() - letterSet).size > numWildcards) return false
        if (wildcard == null && useAllLetters && word.sorted() != sortedLetters) return false
        return true
    }

    fun isValidWord(word: String): Boolean {
        var word_ = word
        if (!prefix.isNullOrEmpty()) {
            if (!word.startsWith(prefix)) return false
            word_ = word.substring(prefix.length)
        }
        return isValidWordWithoutPrefix(word_)
    }
    return words.filter { isValidWord(it) }
}


fun String.sorted(): String {
    return toCharArray().sorted().joinToString("")
}