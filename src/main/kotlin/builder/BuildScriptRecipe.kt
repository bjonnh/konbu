package builder

import FilePath
import builder.handlers.Import
import mu.KotlinLogging
import java.io.File

/**
 * Generate a BuildScript using a DSL
 */
class BuildScriptRecipe {
    private val logger = KotlinLogging.logger {}

    /**
     * root directory for the ontology files (source and build for now)
     */

    var root: String = "data"

    /**
     * ontology name
     */
    var name: String = ""

    /**
     * main source file for the ontology (relative to root)
     */
    var mainSource: String = ""

    /**
     * extra source files for the ontology (relative to root)
     */
    var extraSources: List<String> = listOf()

    /**
     * do we need to generate a preseed
     */
    var preseedGeneration: Boolean = true

    /**
     * location of the catalog (relative to root)
     */
    var catalog: String? = null

    private val imports: MutableSet<Import> = mutableSetOf()

    /**
     * location of the terms files (relative to root)
     */
    var termsListsPath: String = "imports"

    /**
     * base URI of the ontology
     */
    var uribase: String = ""

    /**
     * version of the ontology
     */
    var version: String = ""

    /**
     * Add an external ontology
     */
    fun importAndExtract(name: String, location: String, terms: FilePath, forceUpdate: Boolean) {
        imports.add(Import(name, location, File(File(root, termsListsPath), terms), forceUpdate = forceUpdate))
    }

    /**
     * Import an external ontology
     */
    fun import(name: String, location: String, forceUpdate: Boolean) {
        imports.add(Import(name, location, forceUpdate = forceUpdate))
    }

    /**
     * Generate a builder.BuildScript from the DSL description
     */
    fun build(): BuildScript {
        require(mainSource != "") { "mainSource must be set." }
        require(uribase != "") { "uribase must be set." }
        require(version != "") { "version must be set." }
        require(name != "") { "name must be set." }

        require(File(root).exists()) { "Root directory $root must exist." }
        return BuildScript(
            imports = imports.toSet(),
            buildParameters = BuildParameters(
                root,
                uribase,
                version,
                catalog,
                mainSource,
                extraSources,
                preseedGeneration
            )
        )
    }
    // TODO: Triggers
}

/**
 * Generate a build script using the DSL
 */
fun buildScript(f: BuildScriptRecipe.() -> Unit): BuildScript {
    return BuildScriptRecipe().apply(f).build()
}