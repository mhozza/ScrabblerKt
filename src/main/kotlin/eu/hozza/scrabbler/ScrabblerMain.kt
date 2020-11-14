@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

import eu.hozza.datastructures.trie.Trie
import eu.hozza.cli.CharArg
import kotlinx.cli.*
import java.io.File
import eu.hozza.string.*

fun main(args: Array<String>) {
    val parser = ArgParser("Scrabbler")
    val word by parser.argument(ArgType.String, description = "Input word").optional()
    val dict by parser.option(ArgType.String, shortName = "d", description = "List of words to search in.").required()
    val limit by parser.option(ArgType.Int, shortName = "l", description = "Limit the number of words printed.")
    val prefix by parser.option(ArgType.String, description = "Only print words starting with the specified prefix.")
    val allowShorter by parser.option(ArgType.Boolean, "Don't require using all letters.").default(false)
    val wildcard by parser.option(CharArg, shortName = "w", description = "Set a wildcard for the permutation matching.").default('?')
    val regex by parser.option(ArgType.Boolean, shortName = "r", description = "Print words matching regex.")
        .default(false)
    parser.parse(args)

    var words = loadDictionary(dict)

    if (word != null) {
        val _wildcard = if (word!!.contains(wildcard)) wildcard else null
        var trie: Trie? = null
        if (!regex) {
            words = filterDictionary(words, word!!, prefix = prefix, wildcard = _wildcard, useAllLetters = !allowShorter)
            if (allowShorter || _wildcard != null) {
                trie = buildTrie(words)
            }
        }
        Scrabbler(words, trie, isFiltered = true).answer(word!!, regex, limit, allowShorter, _wildcard, prefix)
    } else {
        val trie = buildTrie(words)
        val scrabbler = Scrabbler(words, trie, isFiltered = true)
        while (true) {
            print(">>> ")
            val line = readLine()
            if (line.isNullOrEmpty()) {
                break
            }
            scrabbler.answer(line, regex, limit, allowShorter, wildcard, prefix)
        }
    }
}

