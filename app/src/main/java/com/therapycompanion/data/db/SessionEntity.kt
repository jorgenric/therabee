package com.therapycompanion.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for exercise sessions.
 *
 * elapsedSeconds stores actual elapsed time — not just the target duration.
 * This is important for future wearable integration and accurate reporting.
 * Timestamps stored as UTC epoch milliseconds for unambiguous sorting/reporting.
 */
@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["exercise_id"]),
        Index(value = ["started_at"]),
        Index(value = ["status"])
    ]
)
data class SessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "exercise_id")
    val exerciseId: String,

    /** UTC epoch ms of session start */
    @ColumnInfo(name = "started_at")
    val startedAt: Long = System.currentTimeMillis(),

    /** UTC epoch ms of session completion — null if still in progress or skipped */
    @ColumnInfo(name = "completed_at")
    val completedAt: Long? = null,

    /** Actual elapsed time in seconds — not the target duration */
    @ColumnInfo(name = "elapsed_seconds")
    val elapsedSeconds: Long = 0L,

    /** SessionStatus enum name: InProgress, Completed, Skipped */
    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null
)
