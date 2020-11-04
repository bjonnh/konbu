val forceUpdate = false


name = "pho"
uribase = "http://pho.nprod.net"
version = "0.01"
root = "data"

catalog = "catalog-v001.xml" // This is the default in robot anyway
termsListsPath = "terms" // Path of term lists

mainSource = "main-edit.owl"
extraSources = setOf()

preseedGeneration = true

formats = setOf("json")

prefix("PHO", uribase)
prefix("OBI", "http://purl.obolibrary.org/obo/OBI_")

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

importAndExtract(
    name = "obi",
    location = "http://purl.obolibrary.org/obo/obi.owl",
    terms = "obi_terms.txt",
    forceUpdate = forceUpdate
)


module(file = "administration-methods.tsv")

buildFull(name = "full", reasoning = true)
buildBase(name = "base", reasoning = false)