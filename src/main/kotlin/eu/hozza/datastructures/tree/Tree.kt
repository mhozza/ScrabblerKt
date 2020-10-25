package eu.hozza.datastructures.tree

interface Tree<K, V : TreeNode<K, V>> {
    val root: V
}

interface TreeNode<K, V : TreeNode<K, V>> {
    fun get(key: K): V?
    fun getChildren(sort: Boolean = false): Map<K, V>
    fun setIfNotPresent(key: K, node: V): V
}

enum class ActionOutcome {
    CONTINUE,
    SKIP,
    HALT,
}

fun <K, V : TreeNode<K, V>> Tree<K, V>.getNode(sequence: Iterable<K>): V? {
    var node: V? = root
    for (k in sequence) {
        node = node?.get(k)
        if (node == null) break
    }
    return node
}

fun <V : TreeNode<Char, V>> Tree<Char, V>.getNode(word: CharSequence): V? = this.getNode(word.asIterable())

fun <K, V : TreeNode<K, V>> Tree<K, V>.dfs(
    sort: Boolean = false,
    preAction: ((V, Int, V?, K) -> ActionOutcome)? = null,
    postAction: ((V, Int, V?, K) -> ActionOutcome)? = null,
) {
    fun visit(node: V, depth: Int = 0, parent: V? = null) {
        for ((c, n) in node.getChildren(sort = sort)) {
            if (preAction != null) {
                when (preAction(node, depth, parent, c)) {
                    ActionOutcome.SKIP -> continue
                    ActionOutcome.HALT -> return
                    ActionOutcome.CONTINUE -> {
                    }
                }
            }
            visit(n, depth + 1, node)
            if (postAction != null) {
                when (postAction(node, depth, parent, c)) {
                    ActionOutcome.SKIP -> continue
                    ActionOutcome.HALT -> return
                    ActionOutcome.CONTINUE -> {
                    }
                }
            }
        }
    }
    visit(root)
}

fun <T, K, V : TreeNode<K, V>> Tree<K, V>.bfs(
    rootNodeData: T? = null,
    sort: Boolean = false,
    makeChildInfo: ((K, V, T?) -> T?)? = null,
    action: ((K?, V, T?) -> ActionOutcome),
) {
    data class NodeData<T, K, V : TreeNode<K, V>> constructor(val key: K? = null, val node: V, val info: T)

    val q = ArrayDeque(listOf(NodeData(null, root, rootNodeData)))
    while (q.isNotEmpty()) {
        val (key, node, nodeInfo) = q.removeFirst()
        when (action(key, node, nodeInfo)) {
            ActionOutcome.SKIP -> continue
            ActionOutcome.HALT -> return
            ActionOutcome.CONTINUE -> {
            }
        }
        val subNodes = node.getChildren(sort)
        for ((k, subNode) in subNodes) {
            q.addLast(NodeData(k, subNode, makeChildInfo?.invoke(k, subNode, nodeInfo)))
        }
    }
}

fun <K, V : TreeNode<K, V>> Tree<K, V>.print() {
    dfs(sort = true, preAction = { _, depth, _, key ->
        println("|-".repeat(depth) + key)
        ActionOutcome.CONTINUE
    })
}
