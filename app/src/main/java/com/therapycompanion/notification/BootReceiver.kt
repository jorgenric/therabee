package com.therapycompanion.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Reschedules all notification alarms after device restart.
 *
 * AlarmManager alarms do not survive reboots — this receiver listens for
 * ACTION_BOOT_COMPLETED and ACTION_MY_PACKAGE_REPLACED to restore them.
 *
 * Manifest registration required (already in AndroidManifest.xml):
 *   <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
 *   <receiver android:name=".notification.BootReceiver" android:exported="true">
 *     <intent-filter>
 *       <action android:name="android.intent.action.BOOT_COMPLETED"/>
 *       <action android:name="android.intent.action.MY_PACKAGE_REPLACED"/>
 *     </intent-filter>
 *   </receiver>
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in setOf(
                Intent.ACTION_BOOT_COMPLETED,
                Intent.ACTION_MY_PACKAGE_REPLACED
            )
        ) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                NotificationScheduler.rescheduleAll(context)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
