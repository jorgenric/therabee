package com.therapycompanion.data.model

/**
 * Domain model for an exercise session.
 *
 * elapsedSeconds is actual elapsed time — not the target duration.
 * All timestamps are UTC epoch milliseconds.
 */
data class Session(
    val id: String,
    val exerciseId: String,
    val startedAt: Long,
    val completedAt: Long?,
    val elapsedSeconds: Long,
    val status: SessionStatus,
    val notes: String?,
    /** How the session was initiated: SOURCE_PROMPTED (guided) or SOURCE_ADHOC (I just did this). */
    val source: String = SOURCE_PROMPTED
) {
    val isCompleted: Boolean get() = status == SessionStatus.Completed
    val isSkipped: Boolean get() = status == SessionStatus.Skipped
    val isInProgress: Boolean get() = status == SessionStatus.InProgress
    val isAdhoc: Boolean get() = source == SOURCE_ADHOC

    companion object {
        const val SOURCE_PROMPTED = "Prompted"
        const val SOURCE_ADHOC = "Adhoc"
    }
}
