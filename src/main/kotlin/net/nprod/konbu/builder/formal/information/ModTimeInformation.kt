package net.nprod.konbu.builder.formal.information

import net.nprod.konbu.builder.formal.Information
import net.nprod.konbu.builder.formal.Key
import net.nprod.konbu.builder.formal.TimeValue

interface ModTimeInformation<K : Key> : Information {
    operator fun get(k: K): TimeValue?
    operator fun set(k: K, value: TimeValue)
}