package controllers

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

data class ActionResult(
    val status: ActionStatus,
    val message: String? = null
) {
    fun onSkipped(f: () -> Any): ActionResult {
        if (status == ActionStatus.SKIPPED)
            f()
        return this
    }

    fun onFailed(f: () -> Any): ActionResult {
        if (status == ActionStatus.FAILED)
            f()
        return this
    }

    fun onExecuted(f: () -> Any): ActionResult {
        if (status == ActionStatus.EXECUTED)
            f()
        return this
    }
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