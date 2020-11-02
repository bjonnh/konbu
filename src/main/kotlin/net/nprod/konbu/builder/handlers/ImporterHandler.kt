package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.cache.FileCacheManager
import net.nprod.konbu.controllers.robot.RobotController
import mu.KotlinLogging
import net.nprod.konbu.builder.formal.tasks.NullValue
import net.nprod.konbu.controllers.OntoTask
import java.io.File

/**
 * Handle the mirroring and term extraction from a remote ontology
 *
 * @param buildParameters: Parameters from the ontology builder TODO: just give it what it needs
 * @param robotController: Controller for ROBOT
 */
class ImporterHandler(
    private val buildParameters: BuildParameters, private val robotController: RobotController,
    private val cacheManager: FileCacheManager
) {
    private val root = buildParameters.root

    init {
        File(root, "imports").mkdir()
    }

    fun getTasks(import: Import, preseedFile: File): List<OntoTask> {
        val mirroringTask = mirroringTask(import)
        val tasks = mutableListOf(mirroringTask)
        if (import.termListFile != null) tasks.add(
            extractTask(mirroringTask.output, import, import.termListFile, preseedFile)
        )
        return tasks
    }

    internal fun getOutFile(import: Import): File = File(root, "imports/${import.name}_import.owl")

    private fun extractTask(
        file: File,
        import: Import,
        termListFile: File,
        preseedFile: File
    ): OntoTask {
        val outFile = getOutFile(import)

        return OntoTask(
            "Extracting terms from ${import.name}",
            listOf(termListFile, preseedFile, file),
            outFile
        ) {
            val filteredTermsFile = createTempFile()

            logger.info("Preseed file is $preseedFile")
            val deduplicatedTerms = termListFile.readLines() + preseedFile.readLines().mapNotNull {
                if (it == "" || it.startsWith("#")) {
                    null
                } else {
                    it.split("\\s+".toRegex()).first()
                }
            }.toSet()

            logger.info("Extracting ${deduplicatedTerms.size} term(s)")

            filteredTermsFile.writeText(deduplicatedTerms.joinToString("\n"))

            val method = "BOT"
            robotController.handler()
                .extract(
                    input = file,
                    terms = filteredTermsFile,
                    method = method
                ).queryUpdate(
                    File(File(root, "sparql"), "inject-subset-declaration.ru")
                ).annotateOntology(
                    iri = buildParameters.uribase,
                    version = "${buildParameters.uribase}/releases/${buildParameters.version}/${import.name}",
                    versionName = buildParameters.version,
                    outFile = outFile
                )

            filteredTermsFile.delete()
            NullValue()
        }
    }

    private fun mirroringTask(import: Import): OntoTask {
        val fileName = "imports/${import.name}.owl"
        val outFile = cacheManager.getEntryFile(fileName)
        return OntoTask(
            "Mirroring ${import.name}",
            listOf(),
            outFile
        ) {
            robotController.handler().loadOntology(uri = import.uri, outFile = cacheManager.getEntryFile(fileName))
            cacheManager.getEntry(fileName) // We make sure that it exists
            NullValue()
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}