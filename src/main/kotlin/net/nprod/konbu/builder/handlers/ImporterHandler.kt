package net.nprod.konbu.builder.handlers

import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.cache.FileCacheManager
import net.nprod.konbu.changedetectors.TimestampChangeDetector
import net.nprod.konbu.controllers.action.action
import net.nprod.konbu.controllers.robot.RobotController
import isAlnum
import mu.KotlinLogging
import net.nprod.konbu.cache.CacheManager
import timeBlock
import java.io.File

/**
 * An ontology that will be downloaded and some of its terms extracted
 * (for now we don't handle local files or repositories)
 *
 * @param name: name of the ontology (alphanumeric only)
 * @param uri: URI of the ontology
 * @param termListFile: File containing the list of terms
 * @param forceUpdate: This ontology should be downloaded and term extracted every time
 */
data class Import(
    val name: String,
    val uri: String,
    val termListFile: File? = null,
    val forceUpdate: Boolean = false
) {
    init {
        require(name.isAlnum()) { "Name of an import can only consist of ASCII letters and numbers." }
    }
}

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

    /**
     * Import the given Import
     *
     * @param forceUpdate: force the update even
     */
    fun import(import: Import, preseedFile: File, forceUpdate: Boolean = false) {
        logger.info("Processing ${import.name}")
        val file = mirrorOrCached(import, forceUpdate)
        if (import.termListFile != null) extract(file, import, import.termListFile, preseedFile, forceUpdate)
    }

    private fun extract(
        file: File,
        import: Import,
        termListFile: File,
        preseedFile: File,
        forceUpdate: Boolean = false
    ) {
        logger.timeBlock("extract") {
            val outFile = File(root, "imports/${import.name}_import.owl")
            val filteredTermsFile = createTempFile()

            val res = action(
                inputs = listOf(file, termListFile, preseedFile),
                outputs = listOf(outFile),
                changeDetector = TimestampChangeDetector,
                forceOn = forceUpdate
            ) {
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
            }.onSkipped {
                logger.info("Skipped the extraction as timestamps show no change")
            }.onFailed {
                throw RuntimeException("Received an error message: ${it.message}")
            }
            logger.info("We went after: $res")
            filteredTermsFile.delete()
        }
    }

    private fun mirrorOrCached(import: Import, forceUpdate: Boolean): File {
        return logger.timeBlock("mirrorOrCached") {
            val fileName = "imports/${import.name}.owl"
            val outFile = cacheManager.getEntryOrNull(fileName)
            if (!forceUpdate && !import.forceUpdate && outFile !== null) {
                logger.info("This ontology is already cached and we are not forcing updates.")
                outFile
            } else {
                robotController.handler().loadOntology(uri = import.uri, outFile = cacheManager.getEntryFile(fileName))
                cacheManager.getEntry(fileName) // We make sure that it exists
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}