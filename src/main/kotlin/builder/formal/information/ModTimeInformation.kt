package builder.formal.information

import builder.formal.Information
import builder.formal.Key
import builder.formal.TimeValue

interface ModTimeInformation<K : Key> : Information {
    operator fun get(k: K): TimeValue?
    operator fun set(k: K, value: TimeValue)
}