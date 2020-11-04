package net.nprod.konbu.controllers.robot

import FilePath
import SparqlFile
import net.nprod.konbu.builder.BuildParameters
import net.nprod.konbu.builder.Prefix
import org.apache.log4j.Logger
import org.apache.log4j.spi.LoggerFactory
import org.obolibrary.robot.*
import java.io.File

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
class RobotHandler(private val root: FilePath, catalog: String?, private var state: CommandState? = CommandState()) {
    private val catalogArray: Array<String> =
        catalog?.let { arrayOf("--catalog", File(root, it).path) }
            ?: arrayOf()

    /**
     * Extract the terms from the input file, save in outputFile
     */
    fun extractTerms(input: File, outFile: RobotOutputFile): RobotHandler {

        val sparqlPath = File(File(root, "sparql"), "terms.sparql").path
        preserveRootLogger {
            QueryCommand().execute(
                state,
                arrayOf(
                    *catalogArray,
                    "-f", "csv",
                    "-i", input.path,
                    "--query", sparqlPath,
                    outFile.path
                )
            )
        }
        return this
    }

    private fun preserveRootLogger(function: () -> CommandState) {
        val level = Logger.getRootLogger().level
        function().also { Logger.getRootLogger().level = level }
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
        preserveRootLogger {
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
        }
        return this
    }

    /**
     * Merge the given ontology files.
     */
    fun merge(
        input: File,
        extraInput: List<File>,
        outFile: RobotOutputFile? = null
    ): RobotHandler {
        preserveRootLogger {
            MergeCommand().execute(
                state,
                arrayOf(
                    *catalogArray,
                    "-i", input.path,
                    *extraInput.flatMap {
                        listOf("-i", it.path)
                    }.toTypedArray(),
                    *outFile.argArray()
                )
            )
        }
        return this
    }

    /**
     * Extract terms from the input using the given method
     */
    fun extract(input: File, terms: File, method: String, outFile: RobotOutputFile? = null): RobotHandler {
        preserveRootLogger {
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
        }
        return this
    }

    /**
     * Execute a query update with the given Sparql
     */
    fun queryUpdate(file: SparqlFile, outFile: RobotOutputFile? = null): RobotHandler {
        preserveRootLogger {
            QueryCommand().execute(
                state,
                arrayOf(
                    "--update", file.path,
                    *outFile.argArray()
                )
            )
        }
        return this
    }

    /**
     * Reason (only ELK for now)
     */
    fun reason(): RobotHandler {
        preserveRootLogger {
            ReasonCommand().execute(
                state,
                arrayOf(
                    "--reasoner", "ELK",
                    "--equivalent-classes-allowed", "all",
                    "--exclude-tautologies", "structural"
                )
            )
        }
        return this
    }

    /**
     * Relax
     */
    fun relax(): RobotHandler {
        preserveRootLogger {
            RelaxCommand().execute(
                state,
                arrayOf()
            )
        }
        return this
    }

    /**
     * Reduce (ELK only for now)
     */
    fun reduce(): RobotHandler {
        preserveRootLogger {
            ReduceCommand().execute(
                state,
                arrayOf(
                    "-r", "whelk"
                )
            )
        }
        return this
    }

    /**
     * Really specific annotation we may want something a bit more generic at some point
     */
    fun annotateOntology(
        iri: String,
        version: String? = null,
        versionName: String? = null,
        outFile: RobotOutputFile? = null
    ): RobotHandler {
        require((version == null && versionName == null) || (version != null && versionName != null)) {
            "You have to specify a version and versionName or none of them"
        }
        preserveRootLogger {
            val out = AnnotateCommand().execute(
                state,
                arrayOf(
                    "--ontology-iri", iri,  // Now if we have no versions, we output now
                ) + if (version == null && versionName == null) {
                    arrayOf(*outFile.argArray())
                } else {
                    arrayOf()
                }
            )
            if (version != null) {
                AnnotateCommand().execute(
                    state,
                    arrayOf(
                        "-V", version
                    )
                )
            }

            if (versionName != null) {
                AnnotateCommand().execute(
                    state,
                    arrayOf(
                        "--annotation", "owl:versionInfo", versionName,
                        *outFile.argArray()
                    )
                )
            } else {
                out
            }
        }
        return this
    }

    /**
     * Load an ontology
     */
    fun loadOntology(uri: String, outFile: RobotOutputFile? = null): RobotHandler {
        preserveRootLogger {
            ConvertCommand().execute(
                state,
                arrayOf(
                    "-I", uri,
                    *outFile.argArray()
                )
            )
        }
        return this
    }

    /**
     * Load an ontology
     */
    fun convertFromFile(file: File, format: String, outFile: RobotOutputFile): RobotHandler {
        preserveRootLogger {
            ConvertCommand().execute(
                state,
                arrayOf(
                    "--input", file.path,
                    "-c", "false",
                    "-f", format,
                    "-o", outFile.path
                )
            )
        }
        return this
    }

    /**
     * Build a module from the ontology using a TSV template
     */
    fun template(input: File, template: File, prefixes: Set<Prefix>): RobotHandler {
        val prefixesOptions = prefixes.flatMap {
            listOf("--prefix", "\"${it.prefix}: ${it.uri}\"")
        }.toTypedArray()
        println("Prefixes: $prefixes")
        println(
            arrayOf(
                *prefixesOptions,
                "--input", input.path,
                "--template", template.path
            ).toList()
        )
        preserveRootLogger {
            TemplateCommand().execute(
                state,
                arrayOf(
                    "--input", input.path,
                    "--template", template.path,
                    *prefixesOptions
                )
            )
        }
        return this
    }
}