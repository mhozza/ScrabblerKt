package eu.hozza.datastructures.trie

import eu.hozza.datastructures.tree.BasicTreeNode
import eu.hozza.datastructures.tree.Tree
import eu.hozza.datastructures.tree.TreeNode

data class TrieNode constructor(var isWord: Boolean = false) : TreeNode<Char, TrieNode> by BasicTreeNode()

class Trie : Tree<Char, TrieNode> {
    override val root: TrieNode = TrieNode()

    fun add(word: String): TrieNode {
        var node = root
        for (c in word) {
            node = node.setIfNotPresent(c, TrieNode(isWord = false))
        }
        node.isWord = true
        return node
    }

    fun getNode(word: String): TrieNode? {
        var node: TrieNode? = root
        for (c in word) {
            node = node?.get(c)
            if (node == null) break
        }
        return node
    }

    fun find(word: String): Boolean {
        return getNode(word)?.isWord ?: false
    }

    fun isPrefix(word: String): Boolean {
        return getNode(word) != null
    }
}