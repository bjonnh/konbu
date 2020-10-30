package builder.formal.schedulers

import builder.formal.Value

class BuildTask<V : Value>(
    val f: () -> V
)