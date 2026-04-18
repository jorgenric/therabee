package com.therapycompanion.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.therapycompanion.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver that displays the notification and re-schedules it for the next day.
 */
class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val body = intent.getStringExtra(EXTRA_BODY) ?: return
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        val tapIntent = NotificationScheduler.createTodayDeepLinkIntent(context)
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.notify(requestCode, notification)

        // Re-schedule for the next occurrence (tomorrow same time)
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationScheduler.rescheduleAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_BODY = "body"
        const val EXTRA_REQUEST_CODE = "request_code"
    }
}
