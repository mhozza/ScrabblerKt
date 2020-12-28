@file:Suppress("WarningOnMainUnusedParameterMigration")

package com.mhozza.scrabbler.cli

import com.mhozza.cli.CharArg
import com.mhozza.scrabbler.Dictionary
import com.mhozza.scrabbler.Scrabbler
import kotlinx.cli.*

fun main(args: Array<String>) {
    val parser = ArgParser("Scrabbler")
    val word by parser.argument(ArgType.String, description = "Input word").optional()
    val dict by parser.option(ArgType.String, shortName = "d", description = "List of words to search in.").required()
    val limit by parser.option(ArgType.Int, shortName = "l", description = "Limit the number of words printed.")
    val prefix by parser.option(ArgType.String, description = "Only print words starting with the specified prefix.")
    val allowShorter by parser.option(ArgType.Boolean, description = "Don't require using all letters.").default(false)
    val sortAlphabetically by parser.option(ArgType.Boolean, description = "Sort words alphabetically.").default(false)
    val removeAccents by parser.option(ArgType.Boolean, description = "Remove accents from dictionary.").default(false)
    val wildcard by parser.option(
        CharArg,
        shortName = "w",
        description = "Set a wildcard for the permutation matching."
    ).default('?')
    val regex by parser.option(ArgType.Boolean, shortName = "r", description = "Print words matching regex.")
        .default(false)
    val multipleWords by parser.option(ArgType.Boolean, description = "Allow multiple words.").default(false)
    parser.parse(args)

    val dictionary = if (removeAccents) Dictionary.load(dict).removeAccents() else Dictionary.load(dict)

    if (word != null) {
        if (regex) {
            Scrabbler(dictionary).findByRegex(word!!, limit).println()
        } else {
            Scrabbler(dictionary).findPermutations(
                word!!,
                limit,
                !allowShorter,
                wildcard,
                prefix,
                multipleWords,
                smartSort = !sortAlphabetically
            ).println()
        }
    } else {
        val scrabbler = Scrabbler(dictionary)
        while (true) {
            print(">>> ")
            val line = readLine()
            if (line.isNullOrEmpty()) {
                break
            }
            if (regex) {
                scrabbler.findByRegex(line, limit).println()
            } else {
                scrabbler.findPermutations(line, limit, !allowShorter, wildcard, prefix, multipleWords).println()
            }
        }
    }
}

private fun List<Any>?.println() {
    this?.forEach { println(it) }
}
