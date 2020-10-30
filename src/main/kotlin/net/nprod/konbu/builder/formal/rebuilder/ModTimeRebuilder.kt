package net.nprod.konbu.builder.formal.rebuilder

import net.nprod.konbu.builder.formal.*
import net.nprod.konbu.builder.formal.information.ModTimeInformation
import net.nprod.konbu.builder.formal.tasks.Task

class ModTimeRebuilder<K : Key, V : Value>(private val makeInfo: ModTimeInformation<K>) : Rebuilder<K, V> {
    private var now: Long = 1

    private fun now(): Long = now++

    /**
     * Returns a function that will check that timestamps are ok before running
     */
    override fun rebuild(task: Task<K, V>, v: V?, fetch: (Task<K, V>) -> V): () -> V {
        return {
            val timeOutput = makeInfo[task.output]

            val dirty = if (v == null || timeOutput == null) {
                true
            } else {
                // It is dirty if any of the inputs is older than current output time
                task.input.any { key ->
                    val time = makeInfo[key]
                    time?.let { it > timeOutput } ?: true
                }
            }
            if (dirty) {
                makeInfo[task.output] = TimeValue(now())
                fetch(task)
            } else {
                println(" Rebuilder for ${task.output} => task is not dirty, not rebuilding")
                v ?: fetch(task)
            }
        }
    }
}