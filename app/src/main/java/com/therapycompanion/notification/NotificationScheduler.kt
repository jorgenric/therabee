package com.therapycompanion.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.therapycompanion.MainActivity
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.ui.navigation.EXTRA_NAVIGATE_TO
import com.therapycompanion.ui.navigation.NAVIGATE_TO_TODAY
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Schedules exact notification alarms using AlarmManager.
 *
 * Architecture notes (from spec §8):
 * - Use setExactAndAllowWhileIdle() for time-sensitive triggers — survives Doze mode
 * - Quiet hours are enforced at scheduling time, not by suppressing after the fact
 * - BootReceiver calls rescheduleAll() after device restart
 */
object NotificationScheduler {

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    private const val REQUEST_MORNING = 1001
    private const val REQUEST_CHECKIN = 1002
    private const val REQUEST_EVENING = 1003

    suspend fun rescheduleAll(context: Context) = withContext(Dispatchers.IO) {
        val app = context.applicationContext as TherapyCompanionApp
        val settings = app.userSettingsRepository.getUserSettingsOnce()
        scheduleAll(context, settings)
    }

    fun scheduleAll(context: Context, settings: UserSettings) {
        cancelAll(context)

        if (settings.morningReminderEnabled) {
            schedule(
                context = context,
                requestCode = REQUEST_MORNING,
                timeStr = settings.morningReminderTime,
                channelId = TherapyCompanionApp.CHANNEL_MORNING,
                title = "Time for your exercises",
                body = "Your therapy session is ready for today.",
                quietStart = settings.quietHoursStart,
                quietEnd = settings.quietHoursEnd
            )
        }

        if (settings.afternoonCheckInEnabled) {
            schedule(
                context = context,
                requestCode = REQUEST_CHECKIN,
                timeStr = settings.afternoonCheckInTime,
                channelId = TherapyCompanionApp.CHANNEL_CHECKIN,
                title = "How are you feeling?",
                body = "Take a moment to log your pain and energy levels.",
                quietStart = settings.quietHoursStart,
                quietEnd = settings.quietHoursEnd
            )
        }

        if (settings.eveningEncouragementEnabled) {
            schedule(
                context = context,
                requestCode = REQUEST_EVENING,
                timeStr = settings.eveningEncouragementTime,
                channelId = TherapyCompanionApp.CHANNEL_EVENING,
                title = "You showed up today.",
                body = "Every session matters. Rest well.",
                quietStart = settings.quietHoursStart,
                quietEnd = settings.quietHoursEnd
            )
        }
    }

    private fun schedule(
        context: Context,
        requestCode: Int,
        timeStr: String,
        channelId: String,
        title: String,
        body: String,
        quietStart: String?,
        quietEnd: String?
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        val zoneId = ZoneId.systemDefault()
        val today = LocalDate.now()

        var targetTime = parseTime(timeStr, zoneId, today)

        // If the time has already passed today, schedule for tomorrow
        val now = System.currentTimeMillis()
        if (targetTime <= now) {
            targetTime = parseTime(timeStr, zoneId, today.plusDays(1))
        }

        // Enforce quiet hours at scheduling level — advance past quiet period if needed
        targetTime = applyQuietHours(targetTime, quietStart, quietEnd, zoneId)

        val notifyIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationReceiver.EXTRA_CHANNEL_ID, channelId)
            putExtra(NotificationReceiver.EXTRA_TITLE, title)
            putExtra(NotificationReceiver.EXTRA_BODY, body)
            putExtra(NotificationReceiver.EXTRA_REQUEST_CODE, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notifyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) return // permission not granted
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTime, pendingIntent)
    }

    fun cancelAll(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: return
        listOf(REQUEST_MORNING, REQUEST_CHECKIN, REQUEST_EVENING).forEach { requestCode ->
            val intent = Intent(context, NotificationReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    /** Creates a launch intent that deep-links to the Today screen */
    fun createTodayDeepLinkIntent(context: Context): Intent =
        Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_TO_TODAY)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

    private fun parseTime(timeStr: String, zoneId: ZoneId, date: LocalDate): Long {
        return try {
            val time = LocalTime.parse(timeStr, timeFormatter)
            date.atTime(time).atZone(zoneId).toInstant().toEpochMilli()
        } catch (_: Exception) {
            // Default to 8 AM if parsing fails
            date.atTime(8, 0).atZone(zoneId).toInstant().toEpochMilli()
        }
    }

    /**
     * If [targetMs] falls within the quiet hours window, advances it to quietEnd.
     * Quiet hours are enforced at scheduling time — not by canceling after the fact.
     */
    private fun applyQuietHours(
        targetMs: Long,
        quietStart: String?,
        quietEnd: String?,
        zoneId: ZoneId
    ): Long {
        if (quietStart == null || quietEnd == null) return targetMs

        return try {
            val startTime = LocalTime.parse(quietStart, timeFormatter)
            val endTime = LocalTime.parse(quietEnd, timeFormatter)
            val date = java.time.Instant.ofEpochMilli(targetMs).atZone(zoneId).toLocalDate()
            val quietStartMs = date.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli()
            val quietEndMs = date.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli()

            if (targetMs in quietStartMs..quietEndMs) quietEndMs else targetMs
        } catch (_: Exception) {
            targetMs
        }
    }
}
