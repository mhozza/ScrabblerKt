package com.mhozza.scrabbler

import com.mhozza.datastructures.trie.Trie
import eu.hozza.string.sorted
import java.io.File


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
    prefix: String? = null,
    multipleWords: Boolean = false,
): List<String> {
    val sortedLetters = letters.sorted()
    val letterSet = letters.toSet()
    val numWildcards = if (wildcard == null) 0 else letters.count { it == wildcard }

    fun isValidWordWithoutPrefix(word: String): Boolean {
        if (word.length > letters.length) return false
        if (!multipleWords && useAllLetters && word.length != letters.length) return false
        //TODO: This can be optimized even more using counter.
        if ((word.toSet() - letterSet).size > numWildcards) return false
        if (wildcard == null && useAllLetters && !multipleWords && word.sorted() != sortedLetters) return false
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