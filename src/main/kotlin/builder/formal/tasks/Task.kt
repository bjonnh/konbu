package builder.formal.tasks

import builder.formal.Key
import builder.formal.Value

class NullValue : Value

open class Task<K : Key, V : Value>(
    open val input: List<K>,
    open val output: K,
    open val f: (fetch: (K) -> V) -> V
)