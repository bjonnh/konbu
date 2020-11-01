package net.nprod.konbu.builder.formal.tasks

import net.nprod.konbu.builder.formal.Key
import net.nprod.konbu.builder.formal.Value

class NullValue : Value

open class Task<K : Key, V : Value>(
    open val input: List<K>,
    open val output: K,
    open val f: (fetch: (K) -> V) -> V
) {
    fun valid(): Boolean = true
}