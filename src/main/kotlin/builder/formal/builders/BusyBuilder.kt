package builder.formal.builders

import builder.formal.*
import mu.KotlinLogging


data class TaskDesc<K: Key>(
    val input: MutableSet<K>,
    val outputs: MutableSet<K>
)

class BusyBuilder<C: Condition, I: Information, K: Key, V: Value>(val tasks: Set<Task<C, K, V>>, val store: Store<I, K, V>) : Builder<K, V> {
    override fun fetch(key: K): V {
        val tasksMatching = tasks.filter { it.output == key }
        return if (tasksMatching.isEmpty()) {
            store.get(key)
        } else {
            require(tasksMatching.size == 1) {
                logger.error("The following tasks are building the same output $key: ")
                tasksMatching.forEach {
                    logger.error(" - $it")
                }
                "We have two tasks making the same output!"
            }

            val task = tasksMatching.first()
            logger.info("Building $key")

            task.input.forEach { fetch(it) }
            task.f(task)
            store.get(key)
        }
    }

    private fun getAllInputsAndOutputs(task: Task<C, K, V>, taskDesc: TaskDesc<K>) {
        // We check all the inputs of the given task and we verify if they are in the already known outputs to
        // detect cycles. If not, we iterate into those tasks and check their inputs.
        task.input.forEach { key ->
            // Check if this input was produced already by one of the outputs
            if (key in taskDesc.outputs) {
                logger.error("Loop detected involving $key and task $task")
                throw Exception("We have a loop")
            }
            // Find the task making the current input
            val allOutputs = tasks.filter { it.output == key }
            taskDesc.outputs.addAll(allOutputs.map { it.output }.toSet() - taskDesc.input)
            allOutputs.map { getAllInputsAndOutputs(it, taskDesc) }
        }
    }

    fun build(target: K) {
        // Validate we have no cycles
        logger.info("Checking for loops")
        tasks.forEach {
            val taskDesc = TaskDesc(it.input.toMutableSet(), mutableSetOf(it.output))
            getAllInputsAndOutputs(it, taskDesc)
        }
        fetch(target)
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}