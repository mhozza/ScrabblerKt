package eu.hozza.scrabbler

import eu.hozza.datastructures.Counter
import eu.hozza.datastructures.toCounter
import eu.hozza.datastructures.tree.ActionOutcome
import eu.hozza.datastructures.tree.bfs
import eu.hozza.datastructures.tree.getNode
import eu.hozza.datastructures.trie.Trie
import eu.hozza.string.times
import java.lang.Integer.min

class Scrabbler(val words: List<String>, val trie: Trie?, val isFiltered: Boolean = false) {
    fun answer(
        word: String,
        regex: Boolean = false,
        limit: Int? = null,
        allowShorter: Boolean = false,
        wildcard: Char? = null,
        prefix: String? = null,
        multipleWords: Boolean = false,
    ): List<String> {
        val lowercaseWord = word.toLowerCase()
        return if (regex) {
            words.filterByRegex(lowercaseWord, limit = limit)
        } else {
            if (multipleWords) {
                trie?.findPermutationMultiWord(
                    lowercaseWord,
                    use_all_letters = !allowShorter,
                    wildcard = wildcard,
                    limit = limit,
                )?.map { wordList -> wordList.joinToString(separator = " ") { it } } ?: listOf()
            } else {
                if (isFiltered && !allowShorter && wildcard == null) {
                    if (limit == null) {
                        words
                    } else {
                        words.subList(0, min(limit, words.size))
                    }
                } else {
                    trie?.findPermutations(
                        lowercaseWord,
                        prefix = prefix,
                        use_all_letters = !allowShorter,
                        wildcard = wildcard,
                        limit = limit,
                    ) ?: listOf()
                }
            }
        }
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

    private data class PartialSentenceNode(
        val words: Counter<String>,
        val remainingLetters: Counter<Char>,
    )

    private fun Trie.findPermutationMultiWord(
        letters: String,
        use_all_letters: Boolean = true,
        wildcard: Char? = null,
        limit: Int? = null,
    ): List<List<String>> {
        val possibleWords =
            this.findPermutations(letters, prefix = null, use_all_letters = false, wildcard = wildcard)
        val letterCounter = letters.toCounter()

        val sentenceNodeMap = mutableMapOf<PartialSentenceNode, PartialSentenceNode>()

        val q = ArrayDeque(possibleWords.map {
            PartialSentenceNode(Counter(it), letterCounter - it.toCounter())
        })

        var currentLimit = limit
        val sentences = mutableListOf<List<String>>()

        while (q.isNotEmpty()) {
            val partialSentenceNode = q.removeFirst()
            if (partialSentenceNode in sentenceNodeMap) {
                continue
            }

            val nextWords = this.findPermutations(partialSentenceNode.remainingLetters.entries.joinToString {
                it.key * it.value
            }, prefix = null, use_all_letters = false, wildcard = wildcard)

            // Mark as final.
            if (nextWords.isEmpty() && (partialSentenceNode.remainingLetters.isEmpty() || !use_all_letters)) {
                sentences.add(partialSentenceNode.words.entries.flatMap { entry -> List(entry.value) { entry.key } })
                if (currentLimit != null) {
                    currentLimit -= 1
                    if (currentLimit <= 0) {
                        return sentences
                    }
                }
                continue
            }
            for (word in nextWords) {
                val node = PartialSentenceNode(
                    partialSentenceNode.words + Counter(word),
                    partialSentenceNode.remainingLetters - word.toCounter()
                )

                q.addLast(node)
                sentenceNodeMap[partialSentenceNode] = node
            }
        }

        return sentences
    }

}