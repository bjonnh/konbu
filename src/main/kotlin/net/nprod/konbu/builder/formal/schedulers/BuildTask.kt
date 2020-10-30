package net.nprod.konbu.builder.formal.schedulers

import net.nprod.konbu.builder.formal.Value

class BuildTask<V : Value>(
    val f: () -> V
)