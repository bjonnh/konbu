import mu.KLogger
import kotlin.system.measureTimeMillis

/**
 * Checks if a string is alphanumerical
 */
fun String.isAlnum(): Boolean = this.matches("^[a-zA-Z0-9]*$".toRegex())

fun <T> KLogger.timeBlock(s: String, function: () -> T): T {
    this.info("Task: $s")
    val out: T
    val time = measureTimeMillis {
        out = function()
    }
    this.info("Finished $s: spent $time ms")
    return out
}