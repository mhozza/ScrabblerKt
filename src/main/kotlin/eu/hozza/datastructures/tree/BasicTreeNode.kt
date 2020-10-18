package eu.hozza.datastructures.tree

import java.util.*

class BasicTreeNode<K, V : TreeNode<K, V>> : TreeNode<K, V> {
    private val children: MutableMap<K, V> = mutableMapOf()

    override fun get(key: K): V? = children[key]

    override fun getChildren(sort: Boolean): Map<K, V> {
        if (sort) {
            return TreeMap(children)
        }
        return children.toMap()
    }

    override fun setIfNotPresent(key: K, node: V): V {
        if (!children.containsKey(key)) {
            children[key] = node
        }
        return children.getValue(key)
    }
}