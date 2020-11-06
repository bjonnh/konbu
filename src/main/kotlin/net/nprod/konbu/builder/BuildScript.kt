package net.nprod.konbu.builder

import mu.KotlinLogging
import net.nprod.konbu.builder.handlers.*
import net.nprod.konbu.cache.FileCacheManager
import net.nprod.konbu.controllers.TaskManager
import net.nprod.konbu.controllers.robot.RobotController
import timeBlock
import java.io.File


/**
 * A staged ontology building system
 */
class BuildScript(
    private val buildParameters: BuildParameters
) {
    private val root = buildParameters.root

    private val taskManager: TaskManager = TaskManager()

    private val robotController: RobotController = RobotController(buildParameters)
    private val cacheManager = FileCacheManager(File(root, "cache"))
    private val importerHandler: ImporterHandler = ImporterHandler(buildParameters, robotController, cacheManager)
    private val preseedHandler: PreseedHandler = PreseedHandler(buildParameters, robotController)
    private val moduleHandler: ModuleHandler = ModuleHandler(buildParameters, robotController)
    private val buildHandler: BuildHandler = BuildHandler(buildParameters, robotController)

    /**
     * Execute the build script
     */
    fun execute() {
        val importFiles = buildParameters.imports.map { import ->
            importerHandler.getOutFile(import)
        }

        val moduleFiles = buildParameters.modules.map { module ->
            moduleHandler.getOutFile(module)
        }

        logger.timeBlock("constructing the build script") {
            // TODO: Generate catalog file
            processPreseed()
            processImports(preseedHandler.preseedFile)
            processModules(importFiles)
            processTargets(importFiles,  moduleFiles)
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
        taskManager.addAll(preseedHandler.getTasks(buildParameters.imports))
    }

    private fun processImports(preseedFile: File) {
        buildParameters.imports.forEach {
            taskManager.addAll(importerHandler.getTasks(it, preseedFile = preseedFile))
        }
    }

    private fun processModules(dependencies: List<File>) {
        logger.timeBlock("processing modules") {
            buildParameters.modules.forEach {
                taskManager.add(moduleHandler.getTasks(it, dependencies))
            }
        }
    }

    private fun processTargets(importFiles: List<File>, moduleFiles: List<File>) {
        logger.timeBlock("processing targets") {
            buildParameters.targets.forEach {
                taskManager.addAll(buildHandler.getTasks(it, importFiles, moduleFiles))
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}