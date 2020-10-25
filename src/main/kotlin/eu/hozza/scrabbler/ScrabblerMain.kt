@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

import eu.hozza.datastructures.trie.Trie
import eu.hozza.datastructures.Counter
import eu.hozza.datastructures.toCounter
import eu.hozza.datastructures.tree.ActionOutcome
import eu.hozza.datastructures.tree.bfs
import eu.hozza.datastructures.tree.getNode
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

fun Trie.findPermutations(
    word: String,
    prefix: String? = null,
    use_all_letters: Boolean = true,
    wildcard: Char? = null,
    limit: Int? = null,
): List<String> {
    data class NodeInfo constructor(val word: String, val letters: Counter<Char>)

    val trie = if (!prefix.isNullOrEmpty()) Trie(getNode(prefix) ?: return emptyList()) else this
    val letters = word.toCounter()
    val words = mutableListOf<String>()
    var remainingLimit = limit

    trie.bfs(
        rootNodeData = NodeInfo("", letters),
        makeChildInfo = { c, _, nodeInfo ->
            if ((nodeInfo == null) || ((wildcard == null || nodeInfo.letters.getOrDefault(
                    wildcard,
                    0
                ) == 0) && nodeInfo.letters.getOrDefault(c, 0) <= 0)
            ) {
                null
            } else {
                NodeInfo(
                    nodeInfo.word + c, if (nodeInfo.letters.getOrDefault(c, 0) > 0) {
                        nodeInfo.letters - Counter(c)
                    } else {
                        nodeInfo.letters - Counter(wildcard)
                    }
                )
            }
        }) { _, node, nodeInfo ->
        if (nodeInfo == null) {
            ActionOutcome.SKIP
        } else {
            if ((!use_all_letters || nodeInfo.word.length == word.length) && node.isWord) {
                words.add(nodeInfo.word)
                if (remainingLimit != null) {
                    remainingLimit -= 1
                }
            }
            ActionOutcome.CONTINUE
        }
    }

    return if (!prefix.isNullOrEmpty()) words.map { "$prefix$it" } else words
}

fun findRegex(pattern: String, words: List<String>, limit: Int? = null): List<String> {
    val regex = Regex(pattern)
    val wordsSequence = words.asSequence().filter { regex.matches(it) }
    if (limit != null) {
        return wordsSequence.take(limit).toList()
    }
    return wordsSequence.toList()
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
    val lowercaseWord = word.toLowerCase()
    var result: List<String>?
    if (regex) {
        result = findRegex(lowercaseWord, words, limit = limit)
    } else {
        if (isFiltered && !allowShorter && wildcard == null) {
            result = if (limit == null) {
                words
            } else {
                words.subList(0, limit)
            }
        } else {
            result = trie?.findPermutations(
                lowercaseWord,
                prefix = prefix,
                use_all_letters = !allowShorter,
                wildcard = wildcard,
                limit = limit,
            )
        }
    }
    result.println()
}

/**
 * Argument type for string values.
 */
object CharArg : ArgType<Char>(true) {
    override val description: kotlin.String
        get() = "{ Char }"

    override fun convert(value: kotlin.String, name: kotlin.String): kotlin.Char {
        check(value.length <= 1)
        if (value == "")
            return '?'
        return value.first()
    }
}


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
        answer(word!!, trie, words, isFiltered = true, regex, limit, allowShorter, wildcard, prefix)
    }
}

fun String.sorted(): String {
    return toCharArray().sorted().joinToString("")
}

fun List<Any>?.println() {
    this?.forEach { println(it) }
}