import builder.buildScript

// TODO: Robot controller
// TODO: location of tmp files
// TODO: force rebuild (maybe a global change manager?)
// TODO: add templating
// TODO:

fun main() {
    val forceUpdate = false
    buildScript {
        uribase = "http://pho.nprod.net"
        version = "0.01"
        root = "data"
        catalog = "catalog-v001.xml" // This is the default in robot anyway
        termsListsPath = "terms" // Path of term lists

        mainSource = "main-edit.owl"
        extraSources = listOf()

        preseedGeneration = true

        importAndExtract(
            name = "bfo",
            location = "http://purl.obolibrary.org/obo/bfo.owl",
            terms = "bfo_terms.txt",
            forceUpdate = forceUpdate
        )

        importAndExtract(
            name = "ro",
            location = "http://purl.obolibrary.org/obo/ro.owl",
            terms = "ro_terms.txt",
            forceUpdate = forceUpdate
        )

        importAndExtract(
            name = "eco",
            location = "http://purl.obolibrary.org/obo/eco.owl",
            terms = "eco_terms.txt",
            forceUpdate = forceUpdate
        )
    }.execute()
}

/**
 all:

   NA odkversion: Displays the ODK version and robot version
    X all_imports
      all_main: $MAIN_FILES

      all_subsets
      sparql_test
      all_reports
      all_assets

 prepare_release ($ASSETS $PATTERN_RELEASE_FILES)

 */