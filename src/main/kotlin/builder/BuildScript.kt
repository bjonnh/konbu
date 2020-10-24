package builder

import builder.handlers.Import
import builder.handlers.ImporterHandler
import builder.handlers.PreseedHandler
import controllers.robot.RobotController
import mu.KotlinLogging
import timeBlock
import java.io.File

/**
 * A staged ontology building system
 *
 * Build steps:
 * 1. Process the imports
 *    a) Mirror the ontology if needed
 */
class BuildScript(
    private val imports: Set<Import>,
    buildParameters: BuildParameters
) {
    private val robotController: RobotController = RobotController(buildParameters)
    private val importerHandler: ImporterHandler = ImporterHandler(buildParameters, robotController)
    private val preseedHandler: PreseedHandler = PreseedHandler(buildParameters, robotController)

    /**
     * Execute the build script
     */
    fun execute() {
        logger.timeBlock("executing the build script") {
            // TODO: Generate catalog file
            val preseedFile = processPreseed()
            processImports(preseedFile)
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

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}