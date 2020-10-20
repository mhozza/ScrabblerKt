@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

import eu.hozza.datastructures.trie.Trie
import eu.hozza.datastructures.trie.TrieNode
import kotlinx.cli.*
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

fun filterDictionary(
    words: List<String>,
    letters: String,
    wildcard: Char? = null,
    useAllLetters: Boolean = true,
    prefix: String? = null
): List<String> {
    val sortedLetters = letters.sorted()
    val letterSet = letters.toSet()
    val numWilddcards = if (wildcard == null) 0 else letters.count { it == wildcard }

    fun isValidWordWithoutPrefix(word: String): Boolean {
        if (word.length > letters.length) return false
        if (useAllLetters && word.length != letters.length) return false
        if ((word.toSet() - letterSet).size > numWilddcards) return false
        if (wildcard == null && useAllLetters && word.sorted() != sortedLetters) return false
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

private data class NodeInfo constructor(val node: TrieNode, val word: String, val letters: Map<Char, Int>)

fun findPermutations(
    word: String,
    trie: Trie,
    prefix: String? = null,
    use_all_letters: Boolean = true,
    wildcard: Char? = null,
    limit: Int? = null,
): List<String> {
    val root = (if (!prefix.isNullOrEmpty()) trie.getNode(prefix) else trie.root) ?: return listOf()
    val letters = word.toCounter()
    val q = ArrayDeque(listOf(NodeInfo(root, "", letters)))
    val words = mutableListOf<String>()
    var _limit = limit

    while (q.isNotEmpty()) {
        val (node, w, l) = q.removeFirst()
        val subNodes = if (wildcard == null || l[wildcard] == 0) {
            node.getChildren().filter { (l[it.key] ?: 0) > 0 }.toSortedMap()
        } else {
            node.getChildren().toSortedMap()
        }
        for ((c, subNode) in subNodes) {
            if (_limit == null || _limit > 0) {
                val newW = w + c
                if ((!use_all_letters || newW.length == word.length) && subNode.isWord) {
                    words.add(newW)
                    if (_limit != null) {
                        _limit -= 1
                    }
                }
                val newL: Map<Char, Int> =
                    if (l[c] ?: 0 > 0) l.filter { it.key != c } else l.filter { it.key != wildcard }
                q.addLast(NodeInfo(subNode, newW, newL))
            }
        }
    }
    return if (!prefix.isNullOrEmpty()) words.map { "$prefix$it" } else words
}

fun findRegex(pattern: String, words: List<String>, limit: Int? = null): List<String> {
    val regex = Regex(pattern)
    // TODO: use sequence.
    val _words = words.filter { regex.matches(it) }
    if (limit != null) {
        return _words.subList(0, limit)
    }
    return _words
}

fun answer(
    word: String,
    trie: Trie?,
    words: List<String>,
    isFiltered: Boolean = false,
    regex: Boolean = false,
    limit: Int? = null,
    allowShorter: Boolean = false,
    wildcard: Char? = null,
    prefix: String? = null
) {
    val _word = word.toLowerCase()
    var result: List<String>
    if (regex) {
        result = findRegex(_word, words, limit = limit)
    } else {
        if (isFiltered && !allowShorter && wildcard == null) {
            if (limit == null) {
                result = words
            } else {
                result = words.subList(0, limit)
            }
        } else {
            result = findPermutations(
                _word,
                trie!!,
                prefix = prefix,
                use_all_letters = !allowShorter,
                wildcard = wildcard,
                limit = limit,
            )
        }
    }
    result.println()
}

fun main(args: Array<String>) {
    val parser = ArgParser("playground")
    val word by parser.argument(ArgType.String).optional()
    val dict by parser.option(ArgType.String, shortName = "d").required()
    val limit by parser.option(ArgType.Int, shortName = "l")
    val prefix by parser.option(ArgType.String)
    val allowShorter by parser.option(ArgType.Boolean).default(false)
//    val wildcard by parser.option(ArgType.String)
    val wildcard: Char? = null
    val regex by parser.option(ArgType.Boolean, shortName = "r").default(false)
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
        answer(word!!, trie, words, isFiltered = true, regex, limit, allowShorter, wildcard, prefix)
    }
}

fun String.sorted(): String {
    return toCharArray().sorted().joinToString("")
}

fun String?.toCounter(): Map<Char, Int> {
    return this?.groupingBy { it }?.eachCount() ?: mapOf()
}

fun List<Any>.println() {
    this.forEach { println(it) }
}