package builder.formal.information

import builder.formal.BooleanValue
import builder.formal.Key

class BasicDirtyBitInformation<K : Key> : DirtyBitInformation<K> {
    private val store = mutableMapOf<K, BooleanValue>()

    override operator fun get(k: K): BooleanValue? {
        println("modtimeInfo Trying to get info about the key $k")
        return return store[k] ?: BooleanValue(true)
    }

    override operator fun set(k: K, value: BooleanValue) {
        store[k] = value
        println("modtimeInfo Trying to set $k to $value")
    }
}