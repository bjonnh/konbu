package net.nprod.konbu.builder

import FilePath
import net.nprod.konbu.builder.handlers.Import
import mu.KotlinLogging
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

/**
 * Generate a BuildScript using a DSL
 */
@KotlinScript(fileExtension = "konbu.kts")
class BuildScriptRecipe {
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
     * formats the ontology will be built to besides owl
     */
    var formats: List<String> = listOf()

    private val targets: MutableList<Target> = mutableListOf()

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
                root = root,
                name = name,
                uribase = uribase,
                version = version,
                catalog = catalog,
                mainSource = mainSource,
                extraSources = extraSources,
                preseedGeneration = preseedGeneration,
                targets = targets,
                formats = formats
            )
        )
    }

    fun build_full(name: String, reasoning: Boolean) {
        targets.add(Target(name, reasoning, TargetType.FULL))
    }

    fun build_base(name: String, reasoning: Boolean) {
        targets.add(Target(name, reasoning, TargetType.BASE))
    }

    // TODO: Triggers

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

/**
 * Generate a build script using the DSL
 */
fun buildScript(f: BuildScriptRecipe.() -> Unit): BuildScriptRecipe {
    return BuildScriptRecipe().apply(f)
}

enum class TargetType {
    FULL,
    BASE
}

data class Target(
    val name: String,
    val reasoning: Boolean,
    val targetType: TargetType
)