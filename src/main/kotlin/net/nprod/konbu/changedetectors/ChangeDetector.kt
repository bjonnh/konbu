package net.nprod.konbu.changedetectors

import net.nprod.konbu.controllers.action.Action

interface ChangeDetector {
    fun didChange(action: Action): Boolean
}