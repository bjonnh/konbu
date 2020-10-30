package builder.formal.tasks

import builder.formal.Key
import builder.formal.Value

data class NamedTask<K : Key, V : Value>(
    val name: String,
    override val input: List<K>,
    override val output: K,
    override val f: (fetch: (K) -> V) -> V
) : Task<K, V>(input, output, f)