package com.therapycompanion.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for exercises. Schema version 1.
 *
 * Uses UUID primary key (not auto-increment) so records are safe to merge
 * in any future multi-device or cloud sync scenario.
 *
 * videoFileName is nullable and unused in v1.0 — added now to avoid a
 * destructive migration later when video clips are introduced.
 */
@Entity(tableName = "exercises")
data class ExerciseEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "body_system")
    val bodySystem: String,

    @ColumnInfo(name = "instructions")
    val instructions: String,

    @ColumnInfo(name = "notes")
    val notes: String? = null,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "frequency")
    val frequency: String,

    /** Bitmask of scheduled days. See DayBits for constants. */
    @ColumnInfo(name = "scheduled_days")
    val scheduledDays: Int,

    /** Priority 1 = highest, 3 = lowest */
    @ColumnInfo(name = "priority")
    val priority: Int,

    @ColumnInfo(name = "active")
    val active: Boolean = true,

    @ColumnInfo(name = "image_file_name")
    val imageFileName: String? = null,

    /** Reserved for v2.0 video clips — null in v1.0 */
    @ColumnInfo(name = "video_file_name")
    val videoFileName: String? = null,

    /** UTC epoch milliseconds */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    /** UTC epoch milliseconds */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
