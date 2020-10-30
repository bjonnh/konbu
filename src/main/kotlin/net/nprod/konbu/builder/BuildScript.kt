package net.nprod.konbu.builder

import net.nprod.konbu.builder.handlers.BuildHandler
import net.nprod.konbu.builder.handlers.Import
import net.nprod.konbu.builder.handlers.ImporterHandler
import net.nprod.konbu.builder.handlers.PreseedHandler
import net.nprod.konbu.controllers.robot.RobotController
import mu.KotlinLogging
import timeBlock
import java.io.File

/**
 * A staged ontology building system
 */
class BuildScript(
    private val imports: Set<Import>,
    private val buildParameters: BuildParameters
) {
    private val robotController: RobotController = RobotController(buildParameters)
    private val importerHandler: ImporterHandler = ImporterHandler(buildParameters, robotController)
    private val preseedHandler: PreseedHandler = PreseedHandler(buildParameters, robotController)
    private val buildHandler: BuildHandler = BuildHandler(buildParameters, robotController)

    /**
     * Execute the build script
     */
    fun execute() {
        logger.timeBlock("executing the build script") {
            // TODO: Generate catalog file
            val preseedFile = processPreseed()
            processImports(preseedFile)
            processTargets()
        }
    }

    private fun processPreseed(): File {
        return preseedHandler.process(imports)
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