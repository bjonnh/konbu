package net.nprod.konbu.builder

/**
 * Kind of ontology to build
 */
enum class TargetType {
    /**
     * The full ontology, reasoned
     */
    FULL,

    /**
     * The base ontology, with no imports
     */
    BASE
}

/**
 * Description of the target ontology
 *
 * @name: Name of the target
 * @reasoning: Should reasoning be applied
 * @targetType: Kind of ontology to build
 */
data class Target(
    val name: String,
    val reasoning: Boolean,
    val targetType: TargetType
)