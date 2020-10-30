package builder.formal.rebuilder

import builder.formal.*
import builder.formal.information.DirtyBitInformation
import builder.formal.tasks.Task

class DirtyBitRebuilder<K : Key, V : Value>(private val makeInfo: DirtyBitInformation<K>) : Rebuilder<K, V> {
    /**
     * Returns a function that will check if that key is dirty
     */
    override fun rebuild(task: Task<K, V>, v: V?, fetch: (Task<K, V>) -> V): () -> V {
        return {
            val boolOutput = makeInfo[task.output]

            val dirty = if (v == null || boolOutput == null) {
                true
            } else {
                // It is dirty if any of the inputs is dirty
                task.input.any { key ->
                    val dirty = makeInfo[key]
                    dirty == null || dirty.value
                }
            }
            if (dirty) {
                makeInfo[task.output] = BooleanValue(false)
                fetch(task)
            } else {
                println(" Rebuilder for ${task.output} => task is not dirty, not rebuilding")
                v ?: fetch(task)
            }
        }
    }
}