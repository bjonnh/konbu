package net.nprod.konbu.builder.formal.tasks

import net.nprod.konbu.builder.formal.Key
import net.nprod.konbu.builder.formal.Value



class Tasks<K : Key, V : Value>(val content: Set<Task<K, V>>) {
    fun findTask(k: K): Task<K, V>? =
        content.firstOrNull { it.output == k }
}