package net.nprod.konbu.cache

import ifExists
import mu.KotlinLogging
import java.io.File

/**
 * Handle cached files
 */
class FileCacheManager(private val cacheLocation: File) : CacheManager<File> {
    init {
        logger.info("Cache location: ${cacheLocation.path}")
        if (!cacheLocation.exists()) cacheLocation.mkdirs()
    }

    /**
     * Get the File for an entry, but doesn't check if it exists
     */
    fun getEntryFile(entry: String): File {
        logger.debug("Getting entry $entry from cache.")
        return File(cacheLocation, entry)
    }

    /**
     * Checks if an entry exists in the cache
     */
    override fun hasEntry(entry: String): Boolean {
        logger.debug("Checking entry $entry in cache.")
        return File(cacheLocation, entry).exists()
    }

    /**
     * Get an entry and throw an exception if it does not exist
     */
    override fun getEntry(entry: String): File {
        val file = getEntryFile(entry)
        require(file.exists()) { "The entry $entry does not exist in the cache: ${file.path}." }
        return file
    }

    /**
     * Get an entry and return null if it does not exist
     */
    override fun getEntryOrNull(entry: String): File? {
        return getEntryFile(entry).ifExists()
    }

    /**
     * Delete an entry in the cache (but not the file)
     */
    override fun delete(entry: String): Boolean {
        logger.debug("Delete entry $entry in cache.")
        return getEntry(entry).delete()
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}