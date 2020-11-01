package net.nprod.konbu.controllers

import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.builders.BasicBuild
import net.nprod.konbu.builder.formal.information.FileModTimeInformation
import net.nprod.konbu.builder.formal.rebuilder.FileModTimeRebuilder
import net.nprod.konbu.builder.formal.schedulers.TopologicalScheduler
import net.nprod.konbu.builder.formal.stores.BasicStore
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
        BasicStore<FileKey, NullValue, FileModTimeInformation<FileKey>>(FileModTimeInformation())
    private val build = BasicBuild(
        TopologicalScheduler<FileKey, NullValue, FileModTimeInformation<FileKey>>(),
        FileModTimeRebuilder(store.info)
    )

    private val content: MutableSet<Task<FileKey, NullValue>> = mutableSetOf()

    fun add(name: String, inputs: List<File>, output: File, f: ((FileKey) -> NullValue) -> NullValue) {
        content.add(
            NamedTask(
                name,
                inputs.map { FileKey(it) },
                FileKey(output),
                f
            )
        )
    }

    fun execute(file: File) {
        val tasks = Tasks(content)
        val buildTime = measureTimeMillis {
            build.build(
                tasks,
                FileKey(file),
                store
            ) {
                NullValue()
            }
        }
        println("Built ${file.path} in $buildTime ms")
    }

    fun addAll(tasks: Collection<OntoTask>) {
        tasks.forEach {
            add(it.name, it.inputs, it.output, it.f)
        }
    }
}