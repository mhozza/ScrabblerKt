package eu.hozza.scrabbler

import eu.hozza.datastructures.Counter
import eu.hozza.datastructures.toCounter
import eu.hozza.datastructures.tree.ActionOutcome
import eu.hozza.datastructures.tree.bfs
import eu.hozza.datastructures.tree.getNode
import eu.hozza.datastructures.trie.Trie

class Scrabbler(val words: List<String>, val trie: Trie?, val isFiltered: Boolean = false) {
    fun answer(
        word: String,
        regex: Boolean = false,
        limit: Int? = null,
        allowShorter: Boolean = false,
        wildcard: Char? = null,
        prefix: String? = null
    ) {
        val lowercaseWord = word.toLowerCase()
        var result: List<String>?
        if (regex) {
            result = words.filterByRegex(lowercaseWord, limit = limit)
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

    private fun List<String>.filterByRegex(pattern: String, limit: Int? = null): List<String> {
        val regex = Regex(pattern)
        val wordsSequence = this.asSequence().filter { regex.matches(it) }
        if (limit != null) {
            return wordsSequence.take(limit).toList()
        }
        return wordsSequence.toList()
    }


    private fun Trie.findPermutations(
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

    private fun List<Any>?.println() {
        this?.forEach { println(it) }
    }
}