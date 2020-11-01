package net.nprod.konbu.builder.formal.rebuilder

import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.Rebuilder
import net.nprod.konbu.builder.formal.Value
import net.nprod.konbu.builder.formal.compareTo
import net.nprod.konbu.builder.formal.information.FileModTimeInformation
import net.nprod.konbu.builder.formal.tasks.Task

class FileModTimeRebuilder<K : FileKey, V : Value>(private val makeInfo: FileModTimeInformation<K>) : Rebuilder<K, V> {
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
                    time?.let { time > timeOutput } ?: true
                }
            }

            if (dirty) {
                fetch(task)
            } else {
                println(" Rebuilder for ${task.output} => task is not dirty, not rebuilding")
                v ?: fetch(task)
            }
        }
    }
}