import mu.KotlinLogging
import java.io.File
import java.net.URI

class BuildScriptRecipe {
    private val logger = KotlinLogging.logger {}

    var catalog: String? = null

    val imports: MutableSet<Import> = mutableSetOf()


    var root: String = "data"

    var termsListsPath: String = "imports"

    var uribase: String = ""
    var version: String = ""


    fun import(name: String, path: String, termsListPath: String) {
        imports.add(Import(name, URI(path), File(File(root, termsListsPath), termsListPath)))
    }

    fun import(name: String, path: String) {
        imports.add(Import(name, URI(path)))
    }

    /**
     * Generate a BuildScript from the DSL description
     */
    fun build(): BuildScript {
        require(uribase != "") { "uribase must be set." }
        require(version != "") { "version must be set." }
        require(File(root).exists()) { "Root directory ${root} must exist." }
        return BuildScript(
            root = File(root),
            imports = imports.toSet(),
            ontologyParameters = OntologyParameters(uribase, version)
        )
    }
    // TODO: Triggers
}