package net.nprod.konbu.builder.formal.stores

import net.nprod.konbu.builder.formal.Information
import net.nprod.konbu.builder.formal.Key
import net.nprod.konbu.builder.formal.Store
import net.nprod.konbu.builder.formal.Value

class BasicStore<K : Key, V : Value, I : Information>(override var info: I) : Store<K, V, I> {
    private val content = mutableMapOf<K, V>()

    override fun get(key: K) = content[key]

    override fun put(key: K, value: V) {
        content[key] = value
    }
}