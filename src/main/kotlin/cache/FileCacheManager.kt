package cache

import ifExists
import java.io.File

class FileCacheManager(private val cacheLocation: File) : CacheManager<File> {
    init {
        if (!cacheLocation.exists()) cacheLocation.mkdirs()
    }

    fun getEntryFile(entry: String): File {
        return File(cacheLocation, entry)
    }

    override fun hasEntry(entry: String): Boolean {
        return File(cacheLocation, entry).exists()
    }

    override fun getEntry(entry: String): File {
        val file = File(cacheLocation, entry)
        require(file.exists()) { "The entry $entry does not exist in the cache." }
        return file
    }

    override fun getEntryOrNull(entry: String): File? {
        return File(cacheLocation, entry).ifExists()
    }

    override fun delete(entry: String): Boolean {
        return getEntry(entry).delete()
    }
}