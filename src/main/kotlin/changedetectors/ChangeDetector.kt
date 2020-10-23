package changedetectors

import controllers.Action

interface ChangeDetector {
    fun didChange(action: Action): Boolean
}