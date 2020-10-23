import mu.KotlinLogging
import java.io.File

/**
 * A staged ontology building system
 *
 * Build steps:
 * 1. Process the imports
 *    a) Mirror the ontology if needed
 */
class BuildScript(
    root: File,
    private val imports: Set<Import>,
    ontologyParameters: OntologyParameters
) {
    private val logger = KotlinLogging.logger {}
    private val importer: Importer = Importer(root, ontologyParameters)

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
}


/**
 * Generate a build script using the DSL
 */
fun buildScript(f: BuildScriptRecipe.() -> Unit): BuildScript {
    return BuildScriptRecipe().apply(f).build()
}
