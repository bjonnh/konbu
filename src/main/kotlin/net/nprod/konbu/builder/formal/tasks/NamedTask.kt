package net.nprod.konbu.builder.formal.tasks

import net.nprod.konbu.builder.formal.Key
import net.nprod.konbu.builder.formal.Value

data class NamedTask<K : Key, V : Value>(
    val name: String,
    override val input: List<K>,
    override val output: K,
    override val f: (fetch: (K) -> V) -> V
) : Task<K, V>(input, output, f)