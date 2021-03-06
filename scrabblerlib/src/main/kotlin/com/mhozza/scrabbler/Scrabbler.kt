package com.mhozza.scrabbler

import com.mhozza.datastructures.Counter
import com.mhozza.datastructures.toCounter
import com.mhozza.datastructures.tree.ActionOutcome
import com.mhozza.datastructures.tree.bfs
import com.mhozza.datastructures.tree.getNode
import com.mhozza.datastructures.trie.Trie
import eu.hozza.string.sorted
import eu.hozza.string.times
import java.lang.Integer.min

class Scrabbler(private val dictionary: Dictionary) {
    fun findPermutations(
        word: String,
        limit: Int? = null,
        useAllLetters: Boolean = true,
        wildcard: Char? = '?',
        prefix: String? = null,
        suffix: String? = null,
        contains: String? = null,
        regexFilter: String? = null,
        smartSort: Boolean = true,
    ): List<String> {
        val lowercaseWord = word.toLowerCase()

        @Suppress("NAME_SHADOWING")
        val wildcard = if (wildcard!= null &&  word.contains(wildcard)) wildcard else null

        val filteredDictionary = filterDictionary(dictionary, word, wildcard, useAllLetters, prefix, suffix, contains, regexFilter, false)
        val trie = if (!useAllLetters || wildcard != null) {
            buildTrie(filteredDictionary.dictionary.keys)
        } else {
            null
        }

        return if (useAllLetters && wildcard == null) {
            var words = if (smartSort) {
                filteredDictionary.dictionary.toList().sortedByDescending { it.second }.map { it.first }
            } else {
                filteredDictionary.dictionary.toList().sortedBy { it.first }.map { it.first }
            }
            if (limit != null) {
                words = words.subList(0, min(limit, words.size))
            }
            words
        } else {
            var words = trie?.findPermutations(
                lowercaseWord,
                prefix = prefix,
                useAllLetters = useAllLetters,
                wildcard = wildcard,
                limit = if (smartSort) null else limit,
                sort = !smartSort
            ) ?: listOf()
            if (smartSort) {
                words = words.sortedByDescending { dictionary.dictionary[it] }
                if (limit != null) {
                    words = words.subList(0, min(limit, words.size))
                }
            }
            words
        }

    }

    fun findPermutationsMultiWord(
        word: String,
        limit: Int? = null,
        useAllLetters: Boolean = true,
        wildcard: Char? = '?',
    ): List<String> {
        val lowercaseWord = word.toLowerCase()

        @Suppress("NAME_SHADOWING")
        val wildcard = if (wildcard != null && word.contains(wildcard)) wildcard else null

        val filteredDictionary = filterDictionary(dictionary, word, wildcard, useAllLetters, multipleWords = true)

        val trie = buildTrie(filteredDictionary.dictionary.keys)

        return trie.findPermutationMultiWord(
            lowercaseWord,
            useAllLetters = useAllLetters,
            wildcard = wildcard,
            limit = limit,
        ).map { wordList -> wordList.joinToString(separator = " ") { it } }
    }

    fun findByRegex(word: String, limit: Int? = null, smartSort: Boolean = true): List<String> {
        val regex = Regex(word.toLowerCase())
        var dictionaryItems = dictionary.dictionary.filter { regex.matches(it.key) }.toList()
        if (smartSort) {
            dictionaryItems = dictionaryItems.sortedByDescending { it.second }
        }
        if (limit != null) {
            dictionaryItems = dictionaryItems.subList(0, min(limit, dictionaryItems.size))
        }
        return dictionaryItems.map { it.first }
    }

    private fun Trie.findPermutations(
        word: String,
        prefix: String? = null,
        useAllLetters: Boolean = true,
        wildcard: Char? = null,
        limit: Int? = null,
        sort: Boolean = true,
    ): List<String> {
        data class NodeInfo constructor(val word: String, val letters: Counter<Char>)

        val trie = if (!prefix.isNullOrEmpty()) Trie(getNode(prefix) ?: return emptyList()) else this
        val letters = word.toCounter()
        val words = mutableListOf<String>()
        var remainingLimit = limit

        trie.bfs(
            rootNodeData = NodeInfo("", letters),
            sort = sort,
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
                if ((!useAllLetters || nodeInfo.word.length == word.length) && node.isWord) {
                    words.add(nodeInfo.word)
                    if (remainingLimit != null) {
                        remainingLimit -= 1
                        if (remainingLimit <= 0) {
                            return@bfs ActionOutcome.HALT
                        }
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
        useAllLetters: Boolean = true,
        wildcard: Char? = null,
        limit: Int? = null,
    ): List<List<String>> {
        val possibleWords =
            this.findPermutations(letters, prefix = null, useAllLetters = false, wildcard = wildcard)

        println("Number of possible words: ${possibleWords.size}")

        val letterCounter = letters.toCounter()

        val sentenceNodeSet = mutableSetOf<Int>()

        val q = ArrayDeque(possibleWords.map {
            PartialSentenceNode(Counter(it), letterCounter - it.toCounter())
        })

        var currentLimit = limit
        val sentences = mutableListOf<List<String>>()

        while (q.isNotEmpty()) {
            val partialSentenceNode = q.removeFirst()
            if (partialSentenceNode.hashCode() in sentenceNodeSet) {
                continue
            }
            sentenceNodeSet.add(partialSentenceNode.hashCode())

            val nextWords = this.findPermutations(partialSentenceNode.remainingLetters.entries.joinToString {
                it.key * it.value
            }, prefix = null, useAllLetters = false, wildcard = wildcard)

            // Mark as final.
            if (nextWords.isEmpty() && (partialSentenceNode.remainingLetters.isEmpty() || !useAllLetters)) {
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
                // TODO: adding it to beginning will significantly reduce memory footprint, but will not order by sentence length.
                q.addLast(node)
            }
        }

        return sentences
    }

    private fun buildTrie(words: Iterable<String>): Trie {
        val trie = Trie()
        trie.addAll(words)
        return trie
    }

    private fun filterDictionary(
        dictionary: Dictionary,
        letters: String,
        wildcard: Char? = null,
        useAllLetters: Boolean = true,
        prefix: String? = null,
        suffix: String? = null,
        contains: String? = null,
        regexFilter: String? = null,
        multipleWords: Boolean = false,
    ): Dictionary {
        val sortedLetters = letters.sorted()
        val letterSet = letters.toSet()
        val numWildcards = if (wildcard == null) 0 else letters.count { it == wildcard }

        @Suppress("NAME_SHADOWING")
        val regexFilter = if (regexFilter == null) null else  Regex(regexFilter)

        fun isValidWordWithoutPrefix(word: String): Boolean {
            if (word.length > letters.length) return false
            if (!multipleWords && useAllLetters && word.length != letters.length) return false
            //TODO: This can be optimized even more using counter.
            if ((word.toSet() - letterSet).size > numWildcards) return false
            if (wildcard == null && useAllLetters && !multipleWords && word.sorted() != sortedLetters) return false
            return true
        }

        fun isValidWord(word: String): Boolean {
            @Suppress("NAME_SHADOWING")
            var word = word
            if (!prefix.isNullOrEmpty()) {
                if (!word.startsWith(prefix)) return false
                word = word.substring(prefix.length)
            }
            if (!suffix.isNullOrEmpty() && !word.endsWith(suffix)) return false
            if (!contains.isNullOrEmpty() && !word.contains(contains)) return false
            if (regexFilter != null && !regexFilter.matches(word)) return false

            return isValidWordWithoutPrefix(word)
        }
        return dictionary.copy(dictionary = dictionary.dictionary.filter { isValidWord(it.key) })
    }


}