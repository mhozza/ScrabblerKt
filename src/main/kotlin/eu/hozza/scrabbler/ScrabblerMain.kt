@file:Suppress("WarningOnMainUnusedParameterMigration")

package eu.hozza.scrabbler

//import eu.hozza.datastructures.tree.print
import eu.hozza.datastructures.trie.Trie
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.required
import java.io.File

fun main(args: Array<String>) {
    val parser = ArgParser("playground")
    val dict by parser.option(ArgType.String, shortName = "d").required()
    parser.parse(args)

    val words = File(dict).readLines()
    println(words.size)

    val trie = Trie()

    for (w in words) {
        trie.add(w)
    }

    println(trie.root.getChildren())
}