package net.nprod.konbu

import net.nprod.konbu.builder.BuildScriptRecipe
import java.io.File
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvmhost.createJvmEvaluationConfigurationFromTemplate

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


fun main() {
    val file = File("konbu.kts")
    if (!file.exists()) throw RuntimeException("You need a konbu.kts file for this tool to work.")
    val recipe = BuildScriptRecipe()
    fun evalFile(scriptFile: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        val compilationConfiguration = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(
                    wholeClasspath = true
                )
            }
            implicitReceivers(BuildScriptRecipe::class)
            defaultImports("net.nprod.konbu.builder.*")
        }

        val evaluationConfiguration = ScriptEvaluationConfiguration {
            implicitReceivers(recipe)
        }

        return BasicJvmScriptingHost().eval(scriptFile, compilationConfiguration, evaluationConfiguration)
    }

    val res = evalFile(file.toScriptSource())
    when (res) {
        is ResultWithDiagnostics.Failure -> {
            res.reports.forEach {
                println("Error : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
            }
        }
        is ResultWithDiagnostics.Success -> {
            recipe.build().execute()
        }
    }
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