package net.nprod.konbu.controllers.robot

import java.io.File

/**
 * A specific type for the robot output files.
 *
 * This is used to have access to the .argArray() extension function used to
 * build arguments for robot command line.
 */
typealias RobotOutputFile = File

/**
 * Return an array with the parameters for output file or an empty array
 * if null.
 */
fun RobotOutputFile?.argArray(): Array<String> {
    return this?.let { arrayOf("--output", it.path) } ?: arrayOf()
}
