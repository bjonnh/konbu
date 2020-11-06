package net.nprod.konbu.builder

import FilePath
import net.nprod.konbu.builder.handlers.Import
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript

data class OntoModule(
    val name: String,
    val require: List<String>
)

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
    var extraSources: Set<String> = setOf()

    /**
     * do we need to generate a preseed
     */
    var preseedGeneration: Boolean = true

    /**
     * location of the catalog (relative to root)
     */
    var catalog: String? = null


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
    var formats: Set<String> = setOf()

    private val imports: MutableSet<Import> = mutableSetOf()
    private val targets: MutableSet<Target> = mutableSetOf()
    private val modules: MutableSet<OntoModule> = mutableSetOf()
    private val prefixes: MutableSet<Prefix> = mutableSetOf()

    /**
     * Add a prefix
     */
    @Suppress("unused")
    fun prefix(prefix: String, uri: String) {
        prefixes.add(Prefix(prefix, uri))
    }

    /**
     * Add an external ontology
     */
    @Suppress("unused")
    fun importAndExtract(name: String, location: String, terms: FilePath, forceUpdate: Boolean) {
        imports.add(Import(name, location, File(File(root, termsListsPath), terms), forceUpdate = forceUpdate))
    }

    /**
     * Import an external ontology
     */
    @Suppress("unused")
    fun import(name: String, location: String, forceUpdate: Boolean) {
        imports.add(Import(name, location, forceUpdate = forceUpdate))
    }

    /**
     * Add a new module built using a robot template
     */
    @Suppress("unused")
    fun module(file: String, require: List<String> = listOf()) {
        modules.add(OntoModule(file, require))
    }

    @Suppress("unused")
    fun buildFull(name: String, reasoning: Boolean) {
        targets.add(Target(name, reasoning, TargetType.FULL))
    }

    @Suppress("unused")
    fun buildBase(name: String, reasoning: Boolean) {
        targets.add(Target(name, reasoning, TargetType.BASE))
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
            buildParameters = BuildParameters(
                root = root,
                name = name,
                uribase = uribase,
                version = version,
                catalog = catalog,
                mainSource = mainSource,
                extraSources = extraSources,
                preseedGeneration = preseedGeneration,
                prefixes = prefixes,
                imports = imports,
                modules = modules,
                targets = targets,
                formats = formats
            )
        )
    }

    companion object {
        /**
         * Generate a build script using the DSL
         */
        @Suppress("unused")
        fun buildScript(f: BuildScriptRecipe.() -> Unit): BuildScriptRecipe {
            return BuildScriptRecipe().apply(f)
        }
    }
}