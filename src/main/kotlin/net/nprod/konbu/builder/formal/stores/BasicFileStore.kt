package net.nprod.konbu.builder.formal.stores

import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.Information
import net.nprod.konbu.builder.formal.Store
import net.nprod.konbu.builder.formal.tasks.NullValue

class BasicFileStore<I : Information>(override var info: I) : Store<FileKey, NullValue, I> {
    override fun get(key: FileKey): NullValue? = if (key.exists()) {
        NullValue()
    } else {
        null
    }

    override fun put(key: FileKey, value: NullValue) {
    }
}