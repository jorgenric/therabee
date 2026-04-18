package com.therapycompanion.data.model

/**
 * Domain model for an exercise. Used throughout the UI layer.
 * Never use ExerciseEntity directly in ViewModels or Composables.
 *
 * videoFileName is nullable and reserved for v2.0 video clips.
 */
data class Exercise(
    val id: String,
    val name: String,
    val bodySystem: String,
    val instructions: String,
    val notes: String?,
    val durationMinutes: Int,
    val frequency: Frequency,
    val scheduledDays: Int,
    val priority: Int,
    val active: Boolean,
    val imageFileName: String?,
    val videoFileName: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    /** Human-readable scheduled days string, e.g. "Mon, Wed, Fri" or "Daily" */
    val scheduledDaysDisplay: String get() = DayBits.toDisplayString(scheduledDays)

    /** True if this exercise is scheduled for the given day bitmask */
    fun isScheduledForDay(dayBit: Int): Boolean = (scheduledDays and dayBit) != 0
}
