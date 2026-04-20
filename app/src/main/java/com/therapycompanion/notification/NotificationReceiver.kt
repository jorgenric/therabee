package com.therapycompanion.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * BroadcastReceiver that fires when a scheduled notification alarm goes off.
 *
 * Conditional behaviour:
 * - AFTERNOON: suppressed silently if ≥1 exercise was completed today (spec §7).
 * - EVENING: picks between two warm messages depending on whether exercises were done.
 * - MORNING / CUSTOM: always fires.
 *
 * After handling the notification the receiver re-schedules all alarms for tomorrow.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId   = intent.getStringExtra(EXTRA_CHANNEL_ID)   ?: return
        val title       = intent.getStringExtra(EXTRA_TITLE)         ?: return
        val body        = intent.getStringExtra(EXTRA_BODY)          ?: return
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)
        val type        = intent.getStringExtra(EXTRA_TYPE)          ?: NotificationScheduler.TYPE_MORNING

        val tapIntent = NotificationScheduler.createTodayDeepLinkIntent(context)
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val completedToday = countCompletedToday(context)

                when (type) {
                    NotificationScheduler.TYPE_AFTERNOON -> {
                        // Spec §7: only fire if zero exercises completed today.
                        if (completedToday == 0) {
                            postNotification(context, channelId, title, body, requestCode, tapPendingIntent)
                        }
                    }
                    NotificationScheduler.TYPE_EVENING -> {
                        // Spec §7: distinct messages for done vs. not done.
                        val message = if (completedToday > 0)
                            "You did something today. That matters."
                        else
                            "Rest is also part of healing. See you tomorrow."
                        postNotification(context, channelId, title, message, requestCode, tapPendingIntent)
                    }
                    else -> {
                        // Morning and custom reminders always fire.
                        postNotification(context, channelId, title, body, requestCode, tapPendingIntent)
                    }
                }

                // Re-schedule for the next occurrence (tomorrow same time).
                NotificationScheduler.rescheduleAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun postNotification(
        context: Context,
        channelId: String,
        title: String,
        body: String,
        requestCode: Int,
        tapPendingIntent: PendingIntent
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(requestCode, notification)
    }

    /** Returns the number of completed sessions that started today in device local time. */
    private suspend fun countCompletedToday(context: Context): Int {
        val app = context.applicationContext as TherapyCompanionApp
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val dayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return app.sessionRepository
            .getSessionsInDateRange(dayStart, dayEnd)
            .count { it.status == SessionStatus.Completed }
    }

    companion object {
        const val EXTRA_CHANNEL_ID   = "channel_id"
        const val EXTRA_TITLE        = "title"
        const val EXTRA_BODY         = "body"
        const val EXTRA_REQUEST_CODE = "request_code"
        const val EXTRA_TYPE         = "notification_type"
    }
}
