package controllers.action

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