package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.changedetectors.TimestampChangeDetector
import net.nprod.konbu.controllers.action.action
import net.nprod.konbu.controllers.robot.RobotController
import mu.KotlinLogging
import timeBlock
import java.io.File

/**
 * Generate a preseed file for the given ontology and imports
 *
 * @param buildParameters: The build parameters from the ontology (needs root, and sources)
 * @param robotController: The ROBOT controller
 */
class PreseedHandler(private val buildParameters: BuildParameters, private val robotController: RobotController) {
    private val root = buildParameters.root

    init {
        File(root, "tmp").mkdir()
    }

    /**
     * Process the given imports
     */
    fun process(imports: Collection<Import>, forceUpdate: Boolean = false): File {
        val mainSource = File(root, buildParameters.mainSource)
        val extraFiles = buildParameters.extraSources.map { File(root, it) }
        val mergedFile = File(File(root, "tmp"), "merged-no-imports.owl")
        val preseedFile = File(File(root, "tmp"), "pre_seed.txt")

        logger.timeBlock("generating preseed") {
            if (imports.any { !File(root, "imports/${it.name}_import.owl").exists() }) {
                logger.warn("Skipping preseed generation as all imports have not been processed")
                // We create an empty file if it doesn't exist
                if (!preseedFile.exists()) preseedFile.writeText("")
                return@timeBlock
            }

            action(
                inputs = listOf(mainSource) + extraFiles,
                outputs = listOf(preseedFile),
                changeDetector = TimestampChangeDetector,
                forceOn = forceUpdate
            ) {
                robotController.handler().mergeAndRemoveImports(mainSource, extraFiles, mergedFile)
            }.onSkipped {
                logger.info("Not generating the merged file as nothing changed")
            }

            action(
                inputs = listOf(mergedFile),
                outputs = listOf(preseedFile),
                changeDetector = TimestampChangeDetector,
                forceOn = forceUpdate
            ) {
                robotController.handler().extractTerms(mergedFile, preseedFile)
            }.onSkipped {
                logger.info("Not generating the preseed file as nothing changed")
            }
        }

        return preseedFile
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}