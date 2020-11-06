package net.nprod.konbu

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import net.nprod.konbu.builder.BuildScript
import net.nprod.konbu.builder.BuildScriptRecipe
import runScript
import java.io.File

// TODO: Checksum Manager
// TODO: location of tmp files
// TODO: force rebuild (maybe a global change manager?)
// TODO: add templating
// TODO: add self-tracking (if the rule change, it needs to rebuild)
//       it may be done using a counter that is incremented in each module of the code, but that's complicated
//       maybe we can somehow keep a signature of a class?
//       for the recipe it is easy, we just take the hash from the generating data class
// TODO: dynamic dependencies (when tasks create new elements that would influence other tasks)
// TODO: early cut-off if a source change (comment added) but not the destination
// TODO: keep the build graph from run to run
// TODO: consider separate compilation ala bazel
//      «When Bazel performs separate compilation, it creates a new directory and fills it with symlinks to the explicit input dependencies for the rule.»
// TODO: There is a bug when really bootstraping an ontology, for which we don't have the initial module… We may want to
//       create an empty file maybe?

/**
 * Run the specified action on the build script
 */
fun runAction(action: (BuildScript) -> Unit) {
    val file = File("konbu.kts")
    if (!file.exists()) throw RuntimeException("You need a ${file.name} file for this tool to work.")
    val recipe: BuildScriptRecipe = BuildScriptRecipe()
    runScript(file, receivers = arrayOf(recipe)) {
        recipe.build().apply(action)
    }
}

class Compiler : CliktCommand(allowMultipleSubcommands = true) {
    override fun run() {
        echo("Running konbu! ~~~")
    }
}

class Clean : CliktCommand() {
    val force by option().flag()
    override fun run() {
        runAction {
            it.clean()
        }
    }
}

class Build : CliktCommand() {
    override fun run() {
        runAction {
            it.execute()
        }
    }
}

fun main(args: Array<String>) = Compiler().subcommands(Clean(), Build()).main(args)

/**
all:

$ANNOTATE_ONTOLOGY_VERSION = "annotate -V $(ONTBASE)/releases/$(VERSION)/$@ --annotation owl:versionInfo $(VERSION)"
(we do that already in the robot thingy)

NA odkversion: Displays the ODK version and robot version
X all_imports
X all_main
NA all_subsets: we ignore for now, would need an example for that
- sparql_test
- all_reports
- all_assets

- prepare_release ($ASSETS $PATTERN_RELEASE_FILES)
 */