package net.nprod.konbu.builder

import mu.KotlinLogging
import net.nprod.konbu.builder.handlers.BuildHandler
import net.nprod.konbu.builder.handlers.Import
import net.nprod.konbu.builder.handlers.ImporterHandler
import net.nprod.konbu.builder.handlers.PreseedHandler
import net.nprod.konbu.cache.FileCacheManager
import net.nprod.konbu.controllers.TaskManager
import net.nprod.konbu.controllers.robot.RobotController
import timeBlock
import java.io.File


/**
 * A staged ontology building system
 */
class BuildScript(
    private val imports: Set<Import>,
    private val buildParameters: BuildParameters
) {
    private val root = buildParameters.root

    private val taskManager: TaskManager = TaskManager()

    private val robotController: RobotController = RobotController(buildParameters)
    private val cacheManager = FileCacheManager(File(root, "cache"))
    private val importerHandler: ImporterHandler = ImporterHandler(buildParameters, robotController, cacheManager)
    private val preseedHandler: PreseedHandler = PreseedHandler(buildParameters, robotController)
    private val buildHandler: BuildHandler = BuildHandler(buildParameters, robotController)

    /**
     * Execute the build script
     */
    fun execute() {
        val importFiles = imports.map { import ->
            importerHandler.getOutFile(import)
        }

        logger.timeBlock("constructing the build script") {
            // TODO: Generate catalog file
            processPreseed()
            processImports(preseedHandler.preseedFile)
            processTargets(importFiles)
        }

        logger.timeBlock("running tasks") {
            buildParameters.targets.forEach { (name) -> // target name
                buildParameters.formats.forEach { format ->
                    taskManager.execute(buildHandler.outputFile(name, format))
                }
            }
        }
    }

    private fun processPreseed() {
        taskManager.addAll(preseedHandler.getTasks(imports))
    }

    private fun processImports(preseedFile: File) {
        imports.forEach {
            taskManager.addAll(importerHandler.getTasks(it, preseedFile = preseedFile))
        }
    }

    private fun processTargets(imports: List<File>) {
        logger.timeBlock("processing targets") {
            buildParameters.targets.forEach {
                taskManager.addAll(buildHandler.getTasks(it, imports))
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}