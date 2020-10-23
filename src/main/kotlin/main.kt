fun main() {
    buildScript {
        uribase = "http://pho.nprod.net"
        version = "0.01"
        root = "data"
        catalog = "catalog-v001.xml"
        termsListsPath = "terms" // Path of term lists

        import(
            "bfo",
            "http://purl.obolibrary.org/obo/bfo.owl",
            "bfo_terms.txt"
        )
    }.execute()
}