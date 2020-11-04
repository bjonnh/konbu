package net.nprod.konbu.builder

import net.nprod.konbu.builder.handlers.Import

/**
 * Parameters used to build the ontology
 *
 * @param root: root of the data files
 * @param name: name of the ontology
 * @param uribase: base of the ontology
 * @param version: version of the ontology
 * @param catalog: location of the catalog file (if not given ROBOT's default of catalog-v001.xml) relative to root
 * @param mainSource: main source file (relative to root)
 * @param extraSources: extra source files (relative to root)
 * @param preseedGeneration: does a preseed has to be generated
 * @param modules: the list of modules to be built into the ontology (tsv files)
 * @param targets: the targets that will have to be built
 * @param formats: the formats of the ontology to be built
 */

data class BuildParameters(
    val root: String,
    val name: String,
    val uribase: String,
    val version: String,
    val catalog: String?,
    val mainSource: String,
    val extraSources: Set<String>,
    val preseedGeneration: Boolean,
    val prefixes: Set<Prefix>,
    val imports: Set<Import>,
    val modules: Set<OntoModule>,
    val targets: Set<Target>,
    val formats: Set<String>
)