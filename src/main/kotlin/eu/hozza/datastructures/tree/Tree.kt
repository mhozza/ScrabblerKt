package eu.hozza.datastructures.tree

interface Tree<K, V : TreeNode<K, V>> {
    val root: V
}

interface TreeNode<K, V : TreeNode<K, V>> {
    fun get(key: K): V?
    fun getChildren(sort: Boolean = false): Map<K, V>
    fun setIfNotPresent(key: K, node: V): V
}

fun <K, V : TreeNode<K, V>> Tree<K, V>.dfs(
    sort: Boolean = false,
    preAction: ((V, Int, V?, K) -> Boolean)? = null,
    postAction: ((V, Int, V?, K) -> Boolean)? = null,
) {
    fun visit(node: V, depth: Int = 0, parent: V? = null) {
        for ((c, n) in node.getChildren(sort = sort)) {
            if (preAction != null) {
                if (preAction(node, depth, parent, c)) {
                    return
                }
            }
            visit(n, depth + 1, node)
            if (postAction != null) {
                if (postAction(node, depth, parent, c)) {
                    return
                }
            }
        }
    }
    visit(root)
}

fun <K, V : TreeNode<K, V>> Tree<K, V>.print() {
    dfs(sort = true, preAction = { node, depth, parent, key ->
        println("|-".repeat(depth) + key)
        false
    })
}
