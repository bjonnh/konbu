package builder

/**
 * Parameters used to build the ontology
 *
 * @param root: root of the data files
 * @param uribase: base of the ontology
 * @param version: version of the ontology
 * @param catalog: location of the catalog file (if not given ROBOT's default of catalog-v001.xml) relative to root
 * @param mainSource: main source file (relative to root)
 * @param extraSources: extra source files (relative to root)
 * @param preseedGeneration: does a preseed has to be generated
 */
data class BuildParameters(
    val root: String,
    val uribase: String,
    val version: String,
    val catalog: String?,
    val mainSource: String,
    val extraSources: List<String>,
    val preseedGeneration: Boolean
)