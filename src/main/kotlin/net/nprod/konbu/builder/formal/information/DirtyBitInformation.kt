package net.nprod.konbu.builder.formal.information

import net.nprod.konbu.builder.formal.BooleanValue
import net.nprod.konbu.builder.formal.Information
import net.nprod.konbu.builder.formal.Key

interface DirtyBitInformation<K : Key> : Information {
    operator fun get(k: K): BooleanValue?
    operator fun set(k: K, value: BooleanValue)
}