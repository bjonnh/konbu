package controllers.action

import changedetectors.ChangeDetector
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
        } catch (e: Exception) {
            ActionResult(
                ActionStatus.FAILED,
                e.localizedMessage
            )
        }
        ActionResult(
            ActionStatus.EXECUTED
        )
    } else {
        ActionResult(ActionStatus.SKIPPED)
    }
}