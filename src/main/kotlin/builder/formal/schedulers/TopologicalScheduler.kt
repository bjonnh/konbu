package builder.formal.schedulers

import builder.formal.*
import builder.formal.graph.reachable
import builder.formal.graph.topSort
import builder.formal.tasks.Task
import builder.formal.tasks.Tasks

class TopologicalScheduler<K: Key, V: Value, I: Information> :
    Scheduler<K, V, I> {
    override fun schedule(
        store: Store<K, V, I>,
        rebuilder: Rebuilder<K, V>,
        tasks: Tasks<K, V>,
        target: K,
        fetch: (Task<K, V>) -> V
    ): BuildTask<V> {
        println("Running the scheduler")
        println(" We have ${tasks.content.size} tasks")
        println(" Reachable with that target: ${tasks.reachable(target).size}")
        val toBuildOrdered = Tasks(tasks.reachable(target)).topSort()
        println("to build ordered: ${toBuildOrdered.map { it.output }}")
        return BuildTask {
            val last = toBuildOrdered.map {
                it to rebuilder.rebuild(it, store.get(it.output), fetch)
            }.last()
            last.second()
        }
    }
}