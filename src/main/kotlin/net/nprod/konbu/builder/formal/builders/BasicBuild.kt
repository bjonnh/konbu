package net.nprod.konbu.builder.formal.builders

import mu.KotlinLogging
import net.nprod.konbu.builder.formal.*
import net.nprod.konbu.builder.formal.tasks.Task
import net.nprod.konbu.builder.formal.tasks.Tasks

class BasicBuild<K : Key, V : Value, I : Information>(
    private val scheduler: Scheduler<K, V, I>,
    private val rebuilder: Rebuilder<K, V>
) : Build<K, V, I> {
    override fun build(
        tasks: Tasks<K, V>,
        key: K,
        store: Store<K, V, I>,
        fetch: (Task<K, V>) -> V
    ): Store<K, V, I> {
        val buildTask = scheduler.schedule(
            store,
            rebuilder,
            tasks,
            key,
            fetch
        )
        logger.info("Build task is $buildTask")
        buildTask?.let {
            it.f()
        } // Weirdly buildTask?.f() doesn't work

        return store // TODO: We may want to update store
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}