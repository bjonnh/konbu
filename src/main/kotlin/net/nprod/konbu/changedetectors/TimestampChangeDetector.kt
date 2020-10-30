package net.nprod.konbu.changedetectors

import net.nprod.konbu.controllers.action.Action

/**
 * A modified based change detector. If the youngest output
 * is older than the youngest input, it can trigger a rebuild.
 */
object TimestampChangeDetector : ChangeDetector {
    override fun didChange(action: Action): Boolean {
        val lastModifiedOutput = action.outputs.mapNotNull {
            if (it.exists()) it.lastModified() else null
        }.maxOrNull() ?: return true
        val lastModifiedInput = (action.inputs.map {
            it.lastModified()
        }.maxOrNull() ?: 0)
        return lastModifiedInput > lastModifiedOutput
    }
}