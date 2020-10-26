package builder.formal.stores

import builder.formal.*
import mu.KotlinLogging

/**
 * A store with no persistent memory
 */
class SimpleStore<K: Key, V: Value> : Store<NullInformation, K, V> {
    override var info = NullInformation()

    private val store = mutableMapOf<K, V>()

    override fun get(key: K): V = store[key] ?: throw Exception("Input element $key not found")

    override fun put(key: K, value: V) {
        logger.info("Updated the store for $key with $value")
        store[key] = value
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}