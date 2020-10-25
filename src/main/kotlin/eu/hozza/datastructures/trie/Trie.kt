package eu.hozza.datastructures.trie

import eu.hozza.datastructures.tree.BasicTreeNode
import eu.hozza.datastructures.tree.Tree
import eu.hozza.datastructures.tree.TreeNode
import eu.hozza.datastructures.tree.getNode

data class TrieNode constructor(var isWord: Boolean = false) : TreeNode<Char, TrieNode> by BasicTreeNode()

class Trie(root: TrieNode) : Tree<Char, TrieNode> {
    constructor() : this(TrieNode())
    override val root: TrieNode = root

    fun add(word: String): TrieNode {
        var node = root
        for (c in word) {
            node = node.setIfNotPresent(c, TrieNode(isWord = false))
        }
        node.isWord = true
        return node
    }
}

fun Trie.find(word: String): Boolean {
    return getNode(word)?.isWord ?: false
}

fun Trie.isPrefix(word: String): Boolean {
    return getNode(word) != null
}