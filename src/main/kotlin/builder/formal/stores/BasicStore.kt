package builder.formal.stores

import builder.formal.Information
import builder.formal.Key
import builder.formal.Store
import builder.formal.Value

class BasicStore<K : Key, V : Value, I : Information>(override var info: I) : Store<K, V, I> {
    private val content = mutableMapOf<K, V>()

    override fun get(key: K) = content[key]

    override fun put(key: K, value: V) {
        content[key] = value
    }
}