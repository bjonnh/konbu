import mu.KLogger
import net.nprod.konbu.builder.BuildScriptRecipe
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost
import kotlin.system.measureTimeMillis

/**
 * A Sparql File
 */
typealias SparqlFile = File

/**
 * The path to a file
 */
typealias FilePath = String

/**
 * Checks if a string is alphanumerical
 */
fun String.isAlnum(): Boolean = this.matches("^[a-zA-Z0-9]*$".toRegex())

/**
 * Execute the given block and log it
 */
fun <T> KLogger.timeBlock(s: String, function: () -> T): T {
    this.info("Task: $s")
    val out: T
    val time = measureTimeMillis {
        out = function()
    }
    this.info("Finished $s: spent $time ms")
    return out
}

/**
 * Return a file if it exists null if not
 */

fun File.ifExists(): File? {
    return if (this.exists()) this else null
}

/**
 * Execute a script with the given receiver and on success, run the lambda
 */
fun runScript(file: File, receivers: Array<BuildScriptRecipe>, onSuccess: () -> Unit) {
    fun evalFile(scriptFile: SourceCode): ResultWithDiagnostics<EvaluationResult> {
        val compilationConfiguration = ScriptCompilationConfiguration {
            jvm {
                dependenciesFromCurrentContext(
                    wholeClasspath = true
                )
            }
            implicitReceivers(*receivers.map { it::class }.toTypedArray())
            defaultImports("net.nprod.konbu.builder.*")
        }

        val evaluationConfiguration = ScriptEvaluationConfiguration {
            implicitReceivers(*receivers)
        }

        return BasicJvmScriptingHost().eval(scriptFile, compilationConfiguration, evaluationConfiguration)
    }

    when (val res = evalFile(file.toScriptSource())) {
        is ResultWithDiagnostics.Failure -> {
            res.reports.forEach {
                println("Error : ${it.message}" + if (it.exception == null) "" else ": ${it.exception}")
            }
        }
        is ResultWithDiagnostics.Success -> {
            onSuccess()
        }
    }
}