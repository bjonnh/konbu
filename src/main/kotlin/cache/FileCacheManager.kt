package cache

import ifExists
import mu.KotlinLogging
import java.io.File

class FileCacheManager(private val cacheLocation: File) : CacheManager<File> {
    init {
        if (!cacheLocation.exists()) cacheLocation.mkdirs()
    }

    fun getEntryFile(entry: String): File {
        logger.debug("Getting entry $entry from cache.")
        return File(cacheLocation, entry)
    }

    override fun hasEntry(entry: String): Boolean {
        logger.debug("Checking entry $entry in cache.")
        return File(cacheLocation, entry).exists()
    }

    override fun getEntry(entry: String): File {
        val file = getEntryFile(entry)
        require(file.exists()) { "The entry $entry does not exist in the cache." }
        return file
    }

    override fun getEntryOrNull(entry: String): File? {
        return getEntry(entry).ifExists()
    }

    override fun delete(entry: String): Boolean {
        logger.debug("Delete entry $entry in cache.")
        return getEntry(entry).delete()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}