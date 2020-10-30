package net.nprod.konbu

import net.nprod.konbu.builder.BuildScriptRecipe
import java.io.File
import javax.script.ScriptEngineManager

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
// TODO:

// Taken and simplified from: https://github.com/s1monw1/KtsRunner

inline fun <reified T> Any?.castOrError() = takeIf { it is T }?.let { it as T }
    ?: throw IllegalArgumentException("Cannot cast $this to expected type ${T::class}")
class LoadException(message: String? = null, cause: Throwable? = null) : RuntimeException(message, cause)


fun main() {
    val file = File("konbu.kts")
    if (!file.exists()) throw RuntimeException("You need a konbu.kts file for this tool to work.")

    val engine = ScriptEngineManager().getEngineByExtension("kts")
    if (engine==null) throw RuntimeException("Problem when packaging the application, we cannot run scripts!")
    val o: BuildScriptRecipe =
        runCatching { engine.eval(file.reader()) }
            .getOrElse { throw LoadException("Cannot load script", it) }
            .castOrError()
    o.build().execute()
}

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