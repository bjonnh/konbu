import mu.KLogger
import java.io.File
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