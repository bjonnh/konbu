package builder.formal.graph

import builder.formal.*
import builder.formal.tasks.Task
import builder.formal.tasks.Tasks

/**
 * These are probably not efficient at all.
 */

/**
 * Return a list of all tasks that are linked somehow to that one
 */
fun <K : Key, V : Value> Tasks<K, V>.reachable(
    k: K
): Set<Task<K, V>> {
    val tasks = content.filter { k == it.output }
    return tasks.flatMap {
        setOf(it) + it.input.flatMap { input -> reachable(input) }
    }.toSet()
}

/**
 * Does any of these tasks have cycles
 */
fun <K : Key, V : Value> Tasks<K, V>.hasCycle(): Boolean {
    return this.content.any {
        this.findCycleWith(it)
    }
}

/**
 * Find if there is a cycle if we try to build this task
 */
fun <K : Key, V : Value> Tasks<K, V>.findCycleWith(
    t: Task<K, V>,
    inputs: MutableSet<K>? = null,
    outputs: MutableSet<K> = mutableSetOf()
): Boolean {
    return t.input.any { key ->
        // do we have any output producing one of those inputs already?
        if (outputs.contains(key)) {
            return true
        }
        val allOutputs = this.content.filter { it.output == key }
        val realInputs = inputs ?: mutableSetOf(key)
        outputs.addAll(allOutputs.map { it.output }.toSet() - realInputs)
        allOutputs.any { findCycleWith(it, realInputs, outputs) }
    }
}

fun <K : Key, V : Value> Tasks<K, V>.topSort(): List<Task<K, V>> =
    DFS(this).topSort