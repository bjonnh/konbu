import builder.buildScript

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

fun main() {
    val forceUpdate = false
    buildScript {
        name = "pho"
        uribase = "http://pho.nprod.net"
        version = "0.01"
        root = "data"

        catalog = "catalog-v001.xml" // This is the default in robot anyway
        termsListsPath = "terms" // Path of term lists

        mainSource = "main-edit.owl"
        extraSources = listOf()

        preseedGeneration = true

        formats = listOf("obo", "json")

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

        build_full(name = "full", reasoning = true)
        build_base(name = "base", reasoning = false)
    }.execute()
}

/**
 all:

    $ANNOTATE_ONTOLOGY_VERSION = "annotate -V $(ONTBASE)/releases/$(VERSION)/$@ --annotation owl:versionInfo $(VERSION)"
 (we do that already in the robot thingy)

   NA odkversion: Displays the ODK version and robot version
    X all_imports
      all_main: $MAIN_FILES
        $MAIN_FILES: $MAIN_PRODUCTS *(x.y) $MAIN_FORMATS (eg main_product_1.format_1 …)
        $MAIN_PRODUCTS: $ONTNAME *(x-y) $RELEASE_ARTIFACTS (eg pho-full, pho-base) + $ONT

        $ONT-full.obo: $ONT-full.owl
            - $(ROBOT) convert --input $< --check false -f obo $(OBO_FORMAT_OPTIONS) -o $@.tmp.obo && grep -v ^owl-axioms $@.tmp.obo > $@ && rm $@.tmp.obo
        $ONT-base.obo: $ONT-base.owl
            - $(ROBOT) convert --input $< --check false -f obo $(OBO_FORMAT_OPTIONS) -o $@.tmp.obo && grep -v ^owl-axioms $@.tmp.obo > $@ && rm $@.tmp.obo
            $OBO_FORMAT_OPTIONS =           ""
        $ONT-full.json: $ONT-full.owl
            - $(ROBOT) annotate --input $< --ontology-iri $(ONTBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) convert --check false -f json -o $@.tmp.json && mv $@.tmp.json $@
            $ONTBASE = $uribase/$ont

        $ONT-base.json: $ONT-base.owl
            - $(ROBOT) annotate --input $< --ontology-iri $(ONTBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) convert --check false -f json -o $@.tmp.json && mv $@.tmp.json $@
        $ONT.owl: $ONT-full.owl
            - $(ROBOT) annotate --input $< --ontology-iri $(URIBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) convert -o $@.tmp.owl && mv $@.tmp.owl $@
        $ONT.obo: $ONT.owl
            - $(ROBOT) convert --input $< --check false -f obo $(OBO_FORMAT_OPTIONS) -o $@.tmp.obo && grep -v ^owl-axioms $@.tmp.obo > $@ && rm $@.tmp.obo
        $ONT.json: $ONT-full.owl  # This is unclear why they do that the same as $ONT-full.json
            - $(ROBOT) annotate --input $< --ontology-iri $(URIBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) convert --check false -f json -o $@.tmp.json && mv $@.tmp.json $@

        $ONT-base.owl: $SRC $OTHER_SRC
            - $(ROBOT) remove --input $< --select imports --trim false \
                merge $(patsubst %, -i %, $(OTHER_SRC)) \
                annotate --link-annotation http://purl.org/dc/elements/1.1/type http://purl.obolibrary.org/obo/IAO_8000001 \
                --ontology-iri $(ONTBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) \
                --output $@.tmp.owl && mv $@.tmp.owl $@

        $ONT-full.owl: $SRC $OTHER_SRC
            - $(ROBOT) merge --input $< \
                 reason --reasoner ELK --equivalent-classes-allowed all --exclude-tautologies structural \
                relax \
                reduce -r ELK \
                annotate --ontology-iri $(ONTBASE)/$@ $(ANNOTATE_ONTOLOGY_VERSION) --output $@.tmp.owl && mv $@.tmp.owl $@

   NA all_subsets: we ignore for now, would need an example for that
      sparql_test
      all_reports
      all_assets

 prepare_release ($ASSETS $PATTERN_RELEASE_FILES)

 */