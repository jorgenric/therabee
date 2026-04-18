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
    val notes: String?
) {
    val isCompleted: Boolean get() = status == SessionStatus.Completed
    val isSkipped: Boolean get() = status == SessionStatus.Skipped
    val isInProgress: Boolean get() = status == SessionStatus.InProgress
}
