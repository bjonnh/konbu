package net.nprod.konbu.cache

interface CacheManager<T> {
    fun hasEntry(entry: String): Boolean
    fun getEntry(entry: String): T
    fun getEntryOrNull(entry: String): T?
    fun delete(entry: String): Boolean
    fun clean()
}