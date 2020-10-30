package builder.formal.information

import builder.formal.BooleanValue
import builder.formal.Information
import builder.formal.Key

interface DirtyBitInformation<K : Key> : Information {
    operator fun get(k: K): BooleanValue?
    operator fun set(k: K, value: BooleanValue)
}