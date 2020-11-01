package net.nprod.konbu.controllers.action

import net.nprod.konbu.changedetectors.ChangeDetector
import java.io.File

data class Action(
    val inputs: List<File>,
    val outputs: List<File>
)

enum class ActionStatus {
    SKIPPED,
    FAILED,
    EXECUTED
}

fun action(
    inputs: List<File>,
    outputs: List<File>,
    changeDetector: ChangeDetector,
    forceOn: Boolean,
    f: () -> Unit
): ActionResult {
    return if (forceOn || changeDetector.didChange(
            Action(
                inputs,
                outputs,
            )
        )
    ) {
        try {
            f()
            ActionResult(
                ActionStatus.EXECUTED
            )
        } catch (e: Exception) {
            ActionResult(
                ActionStatus.FAILED,
                e.localizedMessage
            )
        }
    } else {
        ActionResult(ActionStatus.SKIPPED)
    }
}