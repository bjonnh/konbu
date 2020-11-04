package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.controllers.robot.RobotController
import mu.KotlinLogging
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.controllers.OntoTask
import java.io.File

/**
 * Generate a preseed file for the given ontology and imports
 *
 * @param buildParameters: The build parameters from the ontology (needs root, and sources)
 * @param robotController: The ROBOT controller
 */
class PreseedHandler(private val buildParameters: BuildParameters, private val robotController: RobotController) {
    private val root = buildParameters.root
    private val build = File(root, "build")
    private val preseed = File(build, "preseed")

    init {
        preseed.mkdirs()
    }

    /**
     * location of the preseed file
     */
    val preseedFile: File = File(preseed, "pre_seed.txt")


    fun getTasks(imports: Set<Import>): List<OntoTask> {
        // TODO: add force update
        // TODO: add the onSkipped

        val mainSource = File(root, buildParameters.mainSource)
        val extraFiles = buildParameters.extraSources.map { File(root, it) }
        val mergedFile = File(preseed, "merged-no-imports.owl")

        if (imports.any { !File(build, "imports/${it.name}_import.owl").exists() }) {
            logger.warn("Skipping preseed generation as all imports have not been processed")
            // We create an empty file if it doesn't exist
            if (!preseedFile.exists()) preseedFile.writeText("")
            return listOf()
        }

        return listOf(
            OntoTask(
                "Generate merged file",
                listOf(mainSource) + extraFiles,
                mergedFile
            ) {
                robotController.handler().mergeAndRemoveImports(mainSource, extraFiles, mergedFile)
                NullValue()
            },
            OntoTask(
                "Generate preseed file",
                listOf(mergedFile),
                preseedFile
            ) {
                robotController.handler().extractTerms(mergedFile, preseedFile)
                NullValue()
            }

        )
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}