package builder.formal.information

import builder.formal.Key
import builder.formal.StringKey
import builder.formal.TimeValue

class BasicModTimeInformation<K: Key> : ModTimeInformation<K> {
    private val store = mutableMapOf<K, TimeValue>()

    override operator fun get(k: K): TimeValue? {
        println("modtimeInfo Trying to get info about the key $k")
        return store[k] ?: TimeValue(0)
    }

    override operator fun set(k: K, value: TimeValue) {
        store[k] = value
        println("modtimeInfo Trying to set $k to $value")
    }
}