package net.nprod.konbu.builder.formal.builders

import net.nprod.konbu.builder.formal.*
import net.nprod.konbu.builder.formal.graph.DFS
import net.nprod.konbu.builder.formal.graph.hasCycle
import net.nprod.konbu.builder.formal.graph.reachable
import net.nprod.konbu.builder.formal.information.BasicDirtyBitInformation
import net.nprod.konbu.builder.formal.information.BasicModTimeInformation
import net.nprod.konbu.builder.formal.information.FileModTimeInformation
import net.nprod.konbu.builder.formal.rebuilder.DirtyBitRebuilder
import net.nprod.konbu.builder.formal.rebuilder.FileModTimeRebuilder
import net.nprod.konbu.builder.formal.rebuilder.ModTimeRebuilder
import net.nprod.konbu.builder.formal.schedulers.RestartingScheduler
import net.nprod.konbu.builder.formal.schedulers.TopologicalScheduler
import net.nprod.konbu.builder.formal.stores.BasicStore
import net.nprod.konbu.builder.formal.tasks.NamedTask
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.builder.formal.tasks.Task
import net.nprod.konbu.builder.formal.tasks.Tasks
import java.io.File
import kotlin.system.measureTimeMillis

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
        println("Starting build")
        val buildTask = scheduler.schedule(
            store,
            rebuilder,
            tasks,
            key,
            fetch
        )

        if (buildTask != null)
            buildTask.f()

        return store // TODO: We may want to update store
    }
}

// TODO: How do we gite MakeInfo to net.nprod.konbu.builder.formal.rebuilder.ModTimeRebuilder?

fun main() {
    val taskMakea =
        NamedTask<StringKey, StringValue>("makea",
            listOf(StringKey("1")),
            StringKey("a"), { fetch ->
                StringValue(
                    fetch(StringKey("1")).value
                )
            })
    val taskLoop = NamedTask<StringKey, StringValue>("loop",
        listOf(StringKey("A")),
        StringKey("a"),
        { fetch ->
            StringValue(
                fetch(StringKey("A")).value
            )
        }
    )

    val tasks = Tasks(
        setOf(
            NamedTask("makeC",
                listOf(StringKey("A"), StringKey("B")),
                StringKey("C"),
                { fetch ->
                    StringValue(fetch(StringKey("A")).value + fetch(StringKey("B")).value)
                }),
            NamedTask("makeA",
                listOf(StringKey("a")),
                StringKey("A"),
                { fetch -> StringValue(fetch(StringKey("a")).value) }),
            taskMakea,
            //taskLoop
        )
    )

    if (tasks.hasCycle()) {
        throw Exception("Loopy")
    }

    println("Reachable from ${taskMakea.name} output: ${tasks.reachable(taskMakea.output)}")

    val dfs = DFS(tasks)
    println("Postorder: ${dfs.topSort}")

    topological_modtime_build(tasks)
    restartingscheduler_dirtybit_build(tasks)
    println("File Builder")
    filebuilder(
        Tasks(
            setOf(
                NamedTask("building c",
                    listOf(FileKey("/tmp/a"), FileKey("/tmp/b")),
                    FileKey("/tmp/c"),
                    {
                        println("Doing something with a and b to make c")
                        File("/tmp/c").writeText("Hello")
                        NullValue()
                    })
            )
        ),
        FileKey("/tmp/c")
    )
}

private fun filebuilder(tasks: Tasks<FileKey, NullValue>, key: FileKey) {
    val store =
        BasicStore<FileKey, NullValue, FileModTimeInformation<FileKey>>(FileModTimeInformation())
    val build = BasicBuild(
        TopologicalScheduler<FileKey, NullValue, FileModTimeInformation<FileKey>>(),
        FileModTimeRebuilder(store.info)
    )
    val buildTime = measureTimeMillis {
        build.build(
            tasks,
            key,
            store
        ) {
            println(" Doing something to make ${it.output} with the fetcher")
            NullValue()
        }
    }
    println("Built Topological/ModTime in $buildTime ms")
}

private fun topological_modtime_build(tasks: Tasks<StringKey, StringValue>) {
    val store =
        BasicStore<StringKey, StringValue, BasicModTimeInformation<StringKey>>(BasicModTimeInformation())
    // We need to add the initial keys
    store.put(StringKey("1"), StringValue("foo"))


    val build = BasicBuild(
        TopologicalScheduler<StringKey, StringValue, BasicModTimeInformation<StringKey>>(),
        ModTimeRebuilder(store.info)
    )

    val buildTime = measureTimeMillis {
        build.build(
            tasks,
            StringKey("C"),
            store
        ) {
            println(" Doing something to make ${it.output} with the fetcher")
            StringValue("Fou")
        }
    }
    println("Built Topological/ModTime in $buildTime ms")
}

private fun restartingscheduler_dirtybit_build(tasks: Tasks<StringKey, StringValue>) {
    val store = BasicStore<StringKey, StringValue, BasicDirtyBitInformation<StringKey>>(BasicDirtyBitInformation())
    // We need to add the initial keys
    store.put(StringKey("1"), StringValue("foo"))


    val build = BasicBuild(
        RestartingScheduler<StringKey, StringValue, BasicDirtyBitInformation<StringKey>>(),
        DirtyBitRebuilder(store.info)
    )

    val buildTime = measureTimeMillis {
        build.build(
            tasks,
            StringKey("C"),
            store
        ) {
            println(" Doing something to make ${it.output} with the fetcher")
            StringValue("Fou")
        }
    }
    println("Built Topological/DirtyBit in $buildTime ms")
}
