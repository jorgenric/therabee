package com.therapycompanion.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for user preferences. Single-row table — id is always 1.
 *
 * Times stored as "HH:mm" strings for simplicity and human-readability.
 * Notification scheduling converts these to AlarmManager trigger times.
 */
@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Int = 1,

    /** Daily exercise load: 1 (lightest) to 10 (heaviest) */
    @ColumnInfo(name = "daily_load")
    val dailyLoad: Int = 5,

    /** When enabled, today's load is halved (floored at 1) */
    @ColumnInfo(name = "easier_day_enabled")
    val easierDayEnabled: Boolean = false,

    // ── Notification toggles ───────────────────────────────────────

    @ColumnInfo(name = "morning_reminder_enabled")
    val morningReminderEnabled: Boolean = true,

    /** "HH:mm" — default 8:00 AM */
    @ColumnInfo(name = "morning_reminder_time")
    val morningReminderTime: String = "08:00",

    @ColumnInfo(name = "afternoon_check_in_enabled")
    val afternoonCheckInEnabled: Boolean = true,

    /** "HH:mm" — default 2:00 PM */
    @ColumnInfo(name = "afternoon_check_in_time")
    val afternoonCheckInTime: String = "14:00",

    @ColumnInfo(name = "evening_encouragement_enabled")
    val eveningEncouragementEnabled: Boolean = true,

    /** "HH:mm" — default 8:00 PM */
    @ColumnInfo(name = "evening_encouragement_time")
    val eveningEncouragementTime: String = "20:00",

    // ── Quiet hours ────────────────────────────────────────────────

    /** "HH:mm" — null means no quiet hours configured */
    @ColumnInfo(name = "quiet_hours_start")
    val quietHoursStart: String? = null,

    /** "HH:mm" — null means no quiet hours configured */
    @ColumnInfo(name = "quiet_hours_end")
    val quietHoursEnd: String? = null,

    // ── Check-in feature ──────────────────────────────────────────

    /** Master toggle for the FPS-R check-in feature */
    @ColumnInfo(name = "check_ins_enabled")
    val checkInsEnabled: Boolean = true
)
