package builder.formal.graph

import builder.formal.*
import builder.formal.tasks.Task
import builder.formal.tasks.Tasks

/**
 * A really naive implementation of a DFS to make a topological ordering
 */
class DFS<K: Key, V: Value>(val ts: Tasks<K, V>) {
    private val marked = BooleanArray(ts.content.size) { false }
    private val postorder = mutableListOf<Task<K, V>>()

    val topSort: List<Task<K, V>>
        get() = postorder.reversed()

    init {
        ts.content.forEachIndexed { index, task ->
            if (!marked[index]) dfs(index, task)
        }
    }

    /**
     * Not the most efficient as we are going over the tasks multiple times
     */
    private fun dfs(index: Int, task: Task<K, V>) {
        marked[index] = true
        val subTasks = ts.content.filter { it.input.contains(task.output) }
        subTasks.forEach { nt ->
            val w = ts.content.indexOf(nt)
            if (!marked[w]) dfs(w, nt)
        }
        postorder.add(task)
    }
}