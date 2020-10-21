import mu.KotlinLogging
import org.obolibrary.robot.*
import org.semanticweb.owlapi.model.IRI
import uk.ac.manchester.cs.owlapi.modularity.ModuleType
import java.io.File
import java.net.URI

fun main() {
    buildScript {
        root("data")
        catalog("catalog-v001.xml")
        termListsPath("terms") // Path of term lists
        import(
            "bfo",
            "http://purl.obolibrary.org/obo/bfo.owl",
            "bfo_terms.txt"
        )
    }.execute()
}

data class Import(
    val name: String,
    val uri: URI,
    val termListPath: File,
    val forceUpdate: Boolean = false
) {
    init {
        require(name.isAlnum()) { "Name of an import can only consist of ASCII letters and numbers." }
    }
}

data class OntologyParameters(
    val uribase: String,
    val version: String
)

class Importer(val root: File) {
    private val logger = KotlinLogging.logger {}

    fun import(import: Import, forceUpdate: Boolean = false) {
        logger.info("Processing ${import.name}")
        val file = mirrorOrCached(import, forceUpdate)
        extract(file, import)
    }

    private fun extract(file: File, import: Import) {
        // extract -i file -T terms.txt --force true --method BOT
        // query --update sparql/inject-subset-declaration.ru
        // annotate --ontology-iri <iriofontologyimported>
        // annotate -V $(ONTBASE)/releases/$(VERSION)/$@ --annotation owl:versionInfo $(VERSION)
        // --output blablabla
        logger.timeBlock("extract") {
            val ioHelper = IOHelper()
            val inputOntology = ioHelper.loadOntology(file)
            val terms = ioHelper.extractTerms(import.termListPath.readText())

            val moduleType = ModuleType.BOT
            val state = CommandState()
            ExtractCommand().execute(
                state,
                arrayOf(
                    "-i", file.path,
                    "-T", import.termListPath.path,
                    "--force", "true",
                    "--method", "BOT",
                )
            )

            QueryCommand().execute(
                state,
                arrayOf(
                    "--update", "data/sparql/inject-subset-declaration.ru"
                )
            )

            AnnotateCommand().execute(
                state,
                arrayOf(
                    "--ontology-iri", "http://pho.nprod.net", // TODO: Generalize
                )
            )

            AnnotateCommand().execute(
                state,
                arrayOf(
                    "-V", "http://pho.nprod.net/releases/demo/${import.name}", // TODO: Generalize
                )
            )

            AnnotateCommand().execute(
                state,
                arrayOf(
                    "--annotation", "owl:versionInfo", "demo", // TODO: Generalize
                    "--output", "/tmp/test.owl"
                )
            )

        }
    }

    private fun mirrorOrCached(import: Import, forceUpdate: Boolean): File {
        return logger.timeBlock("mirrorOrCached") {
            val outFile = File(root, "cache/imports/${import.name}.owl")
            if (!forceUpdate && !import.forceUpdate && outFile.exists()) {
                logger.info("This ontology is already cached and we are not forcing updates.")
                outFile
            } else {
                val ioHelper = IOHelper()
                val ontology = ioHelper.loadOntology(IRI.create(import.uri))
                ioHelper.saveOntology(ontology, outFile)
                outFile
            }
        }
    }
}

/**
 * A staged ontology building system
 *
 * Build steps:
 * 1. Process the imports
 *    a) Mirror the ontology if needed
 */
class BuildScript(
    val root: File,
    val importsPath: File,
    val imports: Set<Import>
) {
    private val logger = KotlinLogging.logger {}
    private val importer: Importer = Importer(root)

    /**
     * Execute the build script
     */
    fun execute() {
        logger.timeBlock("executing the build script") {
            processImports()
            // TODO: Generate catalog file
        }
    }

    private fun processImports() {
        logger.timeBlock("processing imports") {
            imports.forEach {
                importer.import(it)
            }
        }
    }

    companion object {
        fun fromRecipe(recipe: BuildScriptRecipe): BuildScript {
            return BuildScript(
                recipe.root,
                recipe.termsListsPath,
                recipe.imports.toSet()
            )
        }
    }
}


class BuildScriptRecipe {
    private val logger = KotlinLogging.logger {}

    private var catalog: File? = null

    val imports: MutableSet<Import> = mutableSetOf()
    var root: File = File("data")
    var termsListsPath: File = File(root, "imports")

    fun root(path: String) {
        root = File(path)
    }

    fun termListsPath(path: String) {
        termsListsPath = File(root, path)
    }

    fun import(name: String, path: String, termsListPath: String) {
        imports.add(Import(name, URI(path), File(termsListsPath, termsListPath)))
    }

    fun catalog(name: String) {
        catalog = File(root, name)
    }

    // TODO: Triggers
}

fun buildScript(f: BuildScriptRecipe.() -> Unit): BuildScript {
    val recipe = BuildScriptRecipe().apply(f)
    return BuildScript.fromRecipe(recipe)
}

