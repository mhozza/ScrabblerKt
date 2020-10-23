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
    var limit_ = limit

    while (q.isNotEmpty()) {
        val (node, w, l) = q.removeFirst()
        val subNodes = if (wildcard == null || l.getOrDefault(wildcard, 0) == 0) {
            node.getChildren().filter { l.getOrDefault(it.key, 0) > 0 }.toSortedMap()
        } else {
            node.getChildren().toSortedMap()
        }
        for ((c, subNode) in subNodes) {
            if (limit_ == null || limit_ > 0) {
                val newW = w + c
                if ((!use_all_letters || newW.length == word.length) && subNode.isWord) {
                    words.add(newW)
                    if (limit_ != null) {
                        limit_ -= 1
                    }
                }
                val newL: MutableMap<Char, Int> = HashMap(l)
                if (newL.getOrDefault(c, 0) > 0) {
                    newL[c] = newL[c]?.minus(1) ?: 0
                } else if (wildcard != null){
                    newL[wildcard] = newL[wildcard]?.minus(1) ?: 0
                }
                q.addLast(NodeInfo(subNode, newW, newL))
            }
        }
    }
    return if (!prefix.isNullOrEmpty()) words.map { "$prefix$it" } else words
}

fun findRegex(pattern: String, words: List<String>, limit: Int? = null): List<String> {
    val regex = Regex(pattern)
    // TODO: use sequence.
    val words_ = words.filter { regex.matches(it) }
    if (limit != null) {
        return words_.subList(0, limit)
    }
    return words_
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
    val word_ = word.toLowerCase()
    var result: List<String>
    if (regex) {
        result = findRegex(word_, words, limit = limit)
    } else {
        if (isFiltered && !allowShorter && wildcard == null) {
            if (limit == null) {
                result = words
            } else {
                result = words.subList(0, limit)
            }
        } else {
            result = findPermutations(
                word_,
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
    val regex by parser.option(ArgType.Boolean, shortName = "r", description =  "Print words matching regex.").default(false)
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