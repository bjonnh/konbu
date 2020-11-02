package net.nprod.konbu.builder.handlers

import isAlnum
import java.io.File

/**
 * An ontology that will be downloaded and some of its terms extracted
 * (for now we don't handle local files or repositories)
 *
 * @param name: name of the ontology (alphanumeric only)
 * @param uri: URI of the ontology
 * @param termListFile: File containing the list of terms
 * @param forceUpdate: This ontology should be downloaded and term extracted every time
 */
data class Import(
    val name: String,
    val uri: String,
    val termListFile: File? = null,
    val forceUpdate: Boolean = false
) {
    init {
        require(name.isAlnum()) { "Name of an import can only consist of ASCII letters and numbers." }
    }
}