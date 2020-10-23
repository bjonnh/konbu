import mu.KotlinLogging
import org.obolibrary.robot.*
import org.semanticweb.owlapi.model.IRI
import uk.ac.manchester.cs.owlapi.modularity.ModuleType
import java.io.File
import java.net.URI

data class Import(
    val name: String,
    val uri: URI,
    val termListFile: File? = null,
    val forceUpdate: Boolean = false
) {
    init {
        require(name.isAlnum()) { "Name of an import can only consist of ASCII letters and numbers." }
    }
}

class Importer(val root: File, val ontologyParameters: OntologyParameters) {
    private val logger = KotlinLogging.logger {}

    fun import(import: Import, forceUpdate: Boolean = false) {
        logger.info("Processing ${import.name}")
        val file = mirrorOrCached(import, forceUpdate)
        if (import.termListFile != null) extract(file, import, import.termListFile)
    }

    private fun extract(file: File, import: Import, termListFile: File) {
        // extract -i file -T terms.txt --force true --method BOT
        // query --update sparql/inject-subset-declaration.ru
        // annotate --ontology-iri <iriofontologyimported>
        // annotate -V $(ONTBASE)/releases/$(VERSION)/$@ --annotation owl:versionInfo $(VERSION)
        // --output blablabla
        logger.timeBlock("extract") {
            val state = CommandState()

            val filteredTermsFile = createTempFile()

            val deduplicatedTerms = termListFile.readLines().mapNotNull {
                if (it == "" || it.startsWith("#")) {
                    null
                } else {
                    it.split("\\s+".toRegex()).first()
                }
            }.toSet()

            logger.info("Extracting ${deduplicatedTerms.size} term(s)")
            filteredTermsFile.writeText(deduplicatedTerms.joinToString("\n"))

            ExtractCommand().execute(
                state,
                arrayOf(
                    "-i", file.path,
                    "-T", filteredTermsFile.path,
                    "--force", "true",
                    "--method", "BOT",
                )
            )

            filteredTermsFile.delete()

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