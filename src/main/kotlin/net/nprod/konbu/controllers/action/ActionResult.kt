package net.nprod.konbu.controllers.action

data class ActionResult(
    val status: ActionStatus,
    val message: String? = null
) {
    fun onSkipped(f: (ActionResult) -> Any): ActionResult {
        if (status == ActionStatus.SKIPPED)
            f(this)
        return this
    }

    fun onFailed(f: (ActionResult) -> Any): ActionResult {
        if (status == ActionStatus.FAILED)
            f(this)
        return this
    }

    fun onExecuted(f: (ActionResult) -> Any): ActionResult {
        if (status == ActionStatus.EXECUTED)
            f(this)
        return this
    }
}