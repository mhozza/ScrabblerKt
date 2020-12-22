package com.mhozza.datastructures.trie

import com.mhozza.datastructures.tree.BasicTreeNode
import com.mhozza.datastructures.tree.Tree
import com.mhozza.datastructures.tree.TreeNode
import com.mhozza.datastructures.tree.getNode

data class TrieNode constructor(var isWord: Boolean = false) : TreeNode<Char, TrieNode> by BasicTreeNode()

class Trie(override val root: TrieNode) : Tree<Char, TrieNode> {
    constructor() : this(TrieNode())

    fun add(word: String): TrieNode {
        var node = root
        for (c in word) {
            node = node.setIfNotPresent(c, TrieNode(isWord = false))
        }
        node.isWord = true
        return node
    }

    fun addAll(words: List<String>) {
        words.forEach { add(it) }
    }
}

fun Trie.find(word: String): Boolean {
    return getNode(word)?.isWord ?: false
}

fun Trie.isPrefix(word: String): Boolean {
    return getNode(word) != null
}