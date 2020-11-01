package net.nprod.konbu.builder.formal

import net.nprod.konbu.builder.formal.schedulers.BuildTask
import net.nprod.konbu.builder.formal.tasks.Task
import net.nprod.konbu.builder.formal.tasks.Tasks
import java.io.File

interface Key {
    fun exists(): Boolean = true
}

interface Value

inline class StringKey(val value: String) : Key

/**
 * Keys representing files
 */
data class FileKey(val value: File) : Key {
    constructor(value: String) : this(
        File(value)
    )

    /**
     * Get the last modified time as a TimeValue
     */
    fun lastModified() = TimeValue(value.lastModified())
    override fun exists() = value.exists()
}

inline class StringValue(val value: String) : Value
inline class TimeValue(val value: Long)
inline class BooleanValue(val value: Boolean)

operator fun TimeValue.compareTo(timeValue: TimeValue) = this.value.compareTo(timeValue.value)

interface Information

interface Store<K : Key, V : Value, I : Information> {
    var info: I
    fun get(key: K): V?
    fun put(key: K, value: V)
}

interface Hashable {
    val key: String
    val value: String
}

interface Builder<K : Key, V : Value> {
    fun fetch(key: K): V
}

interface Condition

interface Build<K : Key, V : Value, I : Information> {
    fun build(
        tasks: Tasks<K, V>,
        key: K,
        store: Store<K, V, I>,
        fetch: (Task<K, V>) -> V
    ): Store<K, V, I>
}

interface Rebuilder<K : Key, V : Value> {
    fun rebuild(task: Task<K, V>, v: V?, fetch: (Task<K, V>) -> V): () -> V
}

interface Scheduler<K : Key, V : Value, I : Information> {
    fun schedule(
        store: Store<K, V, I>,
        rebuilder: Rebuilder<K, V>,
        tasks: Tasks<K, V>,
        target: K,
        fetch: (Task<K, V>) -> V
    ): BuildTask<V>?
}