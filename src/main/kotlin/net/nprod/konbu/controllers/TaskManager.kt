package net.nprod.konbu.controllers

import mu.KotlinLogging
import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.builders.BasicBuild
import net.nprod.konbu.builder.formal.information.FileModTimeInformation
import net.nprod.konbu.builder.formal.rebuilder.FileModTimeRebuilder
import net.nprod.konbu.builder.formal.schedulers.TopologicalScheduler
import net.nprod.konbu.builder.formal.stores.BasicFileStore
import net.nprod.konbu.builder.formal.tasks.NamedTask
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.builder.formal.tasks.Task
import net.nprod.konbu.builder.formal.tasks.Tasks
import java.io.File
import kotlin.system.measureTimeMillis


data class OntoTask(
    val name: String,
    val inputs: List<File>,
    val output: File,
    val f: ((FileKey) -> NullValue) -> NullValue
)

class TaskManager {
    private val store =
        BasicFileStore<FileModTimeInformation<FileKey>>(FileModTimeInformation())
    private val build = BasicBuild(
        TopologicalScheduler<FileKey, NullValue, FileModTimeInformation<FileKey>>(),
        FileModTimeRebuilder(store.info)
    )

    private val content: MutableSet<Task<FileKey, NullValue>> = mutableSetOf()

    /**
     * Add a single OntoTask to the manager
     */
    fun add(task: OntoTask) {
        // it.name, it.inputs, it.output, it.f
        val inputKeys = task.inputs.map { FileKey(it) }
        content.add(
            NamedTask(
                task.name,
                inputKeys,
                FileKey(task.output),
                { fetch ->
                    // Fetch all inputs
                    task.f(fetch)
                }
            )
        )
    }

    fun execute(file: File) {
        val tasks = Tasks(content)

        //if (logger.isDebugEnabled) {
        /**
         * Show a list of all the tasks
         */
        content.forEach {
            when (it) {
                is NamedTask -> {
                    logger.info("  Task: ${it.name}")
                    logger.info("     I: ${it.input.map { (value) -> value.path }}")
                    logger.info("     O: ${it.output.value.path}")
                }
            }
        }
        //}

        val buildTime = measureTimeMillis {
            build.build(
                tasks,
                FileKey(file),
                store
            ) {
                it.f {
                    NullValue()
                }
                NullValue()
            }
        }
        logger.info("Built ${file.path} in $buildTime ms")
    }

    fun addAll(tasks: Collection<OntoTask>) {
        tasks.forEach {
            add(it)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}