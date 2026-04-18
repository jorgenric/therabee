package com.therapycompanion.data.model

/**
 * Domain model for user settings/preferences.
 * Times represented as "HH:mm" strings matching storage format.
 */
data class UserSettings(
    val dailyLoad: Int = 5,
    val easierDayEnabled: Boolean = false,
    val morningReminderEnabled: Boolean = true,
    val morningReminderTime: String = "08:00",
    val afternoonCheckInEnabled: Boolean = true,
    val afternoonCheckInTime: String = "14:00",
    val eveningEncouragementEnabled: Boolean = true,
    val eveningEncouragementTime: String = "20:00",
    val quietHoursStart: String? = null,
    val quietHoursEnd: String? = null,
    val checkInsEnabled: Boolean = true
) {
    companion object {
        val Default = UserSettings()
    }
}
