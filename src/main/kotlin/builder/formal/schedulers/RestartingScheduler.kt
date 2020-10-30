package builder.formal.schedulers

import builder.formal.*
import builder.formal.tasks.Task
import builder.formal.tasks.Tasks

/**
 * This is a really dirty implementation that doesn't allow for dynamic dependencies
 */
class RestartingScheduler<K : Key, V : Value, I : Information> :
    Scheduler<K, V, I> {
    override fun schedule(
        store: Store<K, V, I>,
        rebuilder: Rebuilder<K, V>,
        tasks: Tasks<K, V>,
        target: K,
        fetch: (Task<K, V>) -> V
    ): BuildTask<V> {
        // need to get the chain from the info
        // need to build a newChain from the chain in which we add the target if it is not in the chain
        // TODO: for now we don't have a persistent chain, add it!
        return BuildTask {
            val chain = arrayListOf(target)
            val status = mutableMapOf(target to false)
            while (status.values.any { !it }) { // We continue while we have something not calculated
                // A task fail if any of its dependencies have not been computed
                println("We still have tasks that are not built: ${status.filter { !it.value }}")
                println("Chain: $chain")
                println("---*")
                val failedTask = chain.firstOrNull {
                    val task = tasks.findTask(it)
                    if (task != null) { // if the task is null, it is an input task we don't build
                        val anythingInStatus = status.filter {
                            task.input.contains(it.key)
                        }
                        // If the task has no input, it cannot fail
                        // Then if there is nothing in the status matching those inputs, it means that we don't
                        //  know about them yet.
                        // Or if of the known there are some we didn't compute yet.
                        task.input.isNotEmpty() && (anythingInStatus.isEmpty() || anythingInStatus.any { !it.value })
                    } else {
                        false
                    }
                }
                println("Failedtask is $failedTask")
                if (failedTask != null) {
                    println("Lets add to the chain")
                    // If this task failed to build (one of its input wasn't built already
                    // we add all its inputs that are not built already before it
                    val toAdd = tasks.findTask(failedTask)?.input?.filter {
                        status[it] != true
                    } ?: listOf()
                    println("We want to add $toAdd")
                    chain.addAll(chain.indexOf(failedTask), toAdd)
                    status[failedTask] = true
                    println("Chain is now $chain")
                }
            }
            val last = chain.mapNotNull {
                val task = tasks.findTask(it)
                if (task != null) {
                    it to rebuilder.rebuild(task, store.get(task.output), fetch)
                } else {
                    null
                }
            }.last()
            last.second()
        }
    }
}