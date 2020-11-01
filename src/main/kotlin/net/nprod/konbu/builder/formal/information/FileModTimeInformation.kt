package net.nprod.konbu.builder.formal.information

import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.TimeValue

/**
 * Use the file system as a store for modification time
 */
class FileModTimeInformation<K : FileKey> : ModTimeInformation<K> {
    /**
     * Return null if the file does not exist or the last modified time if it does
     */
    override operator fun get(k: K): TimeValue? {
        if (!k.exists()) return null // File does not exist
        return k.lastModified()
    }

    /**
     * This is ignored as we use the file system as a store
     */
    override operator fun set(k: K, value: TimeValue) {
    }
}