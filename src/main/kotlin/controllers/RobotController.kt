package controllers

import FilePath
import RobotOutputFile
import SparqlFile
import argArray
import builder.BuildParameters
import org.obolibrary.robot.*
import org.semanticweb.owlapi.model.IRI
import java.io.File
import java.net.URI

/**
 * Handle Robot commands in a single place.
 */
class RobotController(private val buildParameters: BuildParameters) {
    private val root = buildParameters.root

    /**
     * Return an handler that can be used to chain robot commands
     */
    fun handler(): RobotHandler = RobotHandler(root, buildParameters.catalog)
}

/**
 * Chainable Robot commands
 *
 * @param root: the root directory
 * @param state: give an initial state coming from a Robot command
 */
class RobotHandler(val root: FilePath, catalog: String?, private var state: CommandState? = CommandState()) {
    private val catalogArray: Array<String> =
        catalog?.let { arrayOf("--catalog", File(root, it).path) }
            ?: arrayOf()

    /**
     * Extract the terms from the input file, save in outputFile
     */
    fun extractTerms(input: File, outFile: RobotOutputFile? = null): RobotHandler {
        QueryCommand().execute(
            state,
            arrayOf(
                *catalogArray,
                "-f", "csv",
                "-i", input.path,
                "--query", File(File(root, "sparql"), "terms.sparql").path,
                *outFile.argArray()
            )
        )
        return this
    }

    /**
     * Export anything but the imports from the mainSource and the extraFiles
     * and write it in mergedFile.
     */
    fun mergeAndRemoveImports(
        input: File,
        extraInput: List<File>,
        outFile: RobotOutputFile? = null
    ): RobotHandler {
        RemoveCommand().execute(
            state,
            arrayOf(
                *catalogArray,
                "-i", input.path,
                "--select", "imports",
                "--trim", "false",
                "merge", *extraInput.flatMap {
                    listOf("-i", it.path)
                }.toTypedArray(),
                *outFile.argArray()
            )
        )
        return this
    }

    /**
     * Extract terms from the input using the given method
     */
    fun extract(input: File, terms: File, method: String, outFile: RobotOutputFile? = null): RobotHandler {
        // TODO: validate method and add MIREOT
        ExtractCommand().execute(
            state,
            arrayOf(
                "-i", input.path,
                "-T", terms.path,
                "--force", "true",
                "--method", method,
                *outFile.argArray()
            )
        )
        return this
    }

    /**
     * Execute a query update with the given Sparql
     */
    fun queryUpdate(file: SparqlFile, outFile: RobotOutputFile? = null): RobotHandler {
        QueryCommand().execute(
            state,
            arrayOf(
                "--update", file.path,
                *outFile.argArray()
            )
        )
        return this
    }

    /**
     * Really specific annotation we may want something a bit more generic at some point
     */
    fun annotateOntology(
        iri: String,
        version: String,
        versionName: String,
        outFile: RobotOutputFile? = null
    ): RobotHandler {
        AnnotateCommand().execute(
            state,
            arrayOf(
                "--ontology-iri", iri
            )
        )

        AnnotateCommand().execute(
            state,
            arrayOf(
                "-V", version
            )
        )

        AnnotateCommand().execute(
            state,
            arrayOf(
                "--annotation", "owl:versionInfo", versionName,
                *outFile.argArray()
            )
        )
        return this
    }

    /**
     * Load an ontology
     */
    fun loadOntology(uri: String, outFile: RobotOutputFile? = null): RobotHandler {
        ConvertCommand().execute(
            state,
            arrayOf(
                "-I", uri,
                *outFile.argArray()
            )
        )
        return this
    }
}