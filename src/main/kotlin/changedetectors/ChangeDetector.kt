package changedetectors

import controllers.action.Action

interface ChangeDetector {
    fun didChange(action: Action): Boolean
}