package builder.formal.tasks

import builder.formal.Key
import builder.formal.Value



class Tasks<K : Key, V : Value>(val content: Set<Task<K, V>>) {
    fun findTask(k: K): Task<K, V>? =
        content.firstOrNull { it.output == k }
}