package net.nprod.konbu.builder

import net.nprod.konbu.builder.handlers.BuildHandler
import net.nprod.konbu.builder.handlers.Import
import net.nprod.konbu.builder.handlers.ImporterHandler
import net.nprod.konbu.builder.handlers.PreseedHandler
import net.nprod.konbu.controllers.robot.RobotController
import mu.KotlinLogging
import net.nprod.konbu.builder.formal.FileKey
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.cache.FileCacheManager
import net.nprod.konbu.controllers.TaskManager
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
        logger.timeBlock("constructing the build script") {
            // TODO: Generate catalog file
            processPreseed()
        }

        logger.timeBlock("running tasks") {
            taskManager.execute(preseedHandler.preseedFile)
            processImports(preseedHandler.preseedFile)
            processTargets()
        }
    }

    private fun processPreseed() {
        val tasks = preseedHandler.getTasks(imports)
        taskManager.addAll(tasks)
    }

    private fun processImports(preseedFile: File) {
        logger.timeBlock("processing imports") {
            imports.forEach {
                importerHandler.import(it, preseedFile = preseedFile)
            }
        }
    }

    private fun processTargets() {
        logger.timeBlock("processing targets") {
            buildParameters.targets.forEach {
                buildHandler.build(it)
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}