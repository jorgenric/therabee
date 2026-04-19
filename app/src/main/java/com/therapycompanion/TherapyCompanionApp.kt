package com.therapycompanion

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.therapycompanion.data.db.AppDatabase
import com.therapycompanion.data.repository.CheckInRepository
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import com.therapycompanion.data.repository.UserSettingsRepository
import com.therapycompanion.backup.BackupWorker
import com.therapycompanion.notification.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TherapyCompanionApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // ── Repositories — manual DI (no Hilt needed for a single-user offline app) ──
    val database by lazy { AppDatabase.getInstance(this) }
    val exerciseRepository by lazy { ExerciseRepository(database.exerciseDao()) }
    val sessionRepository by lazy { SessionRepository(database.sessionDao()) }
    val checkInRepository by lazy { CheckInRepository(database.checkInDao()) }
    val userSettingsRepository by lazy { UserSettingsRepository(database.userSettingsDao()) }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        initializeDefaultSettings()
        observeSettingsForNotifications()
        BackupWorker.schedule(this)
    }

    /**
     * Creates the three notification channels on first launch.
     * Users can control each independently in Android system settings.
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_MORNING,
                "Morning reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to start your exercises"
            },
            NotificationChannel(
                CHANNEL_CHECKIN,
                "Afternoon check-in",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Prompt to log how you are feeling today"
            },
            NotificationChannel(
                CHANNEL_EVENING,
                "Evening encouragement",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "A quiet end-of-day acknowledgment"
            }
        )

        channels.forEach { manager.createNotificationChannel(it) }
    }

    private fun initializeDefaultSettings() {
        applicationScope.launch {
            userSettingsRepository.initializeDefaults()
        }
    }

    /**
     * Observes the settings Flow and reschedules all notification alarms whenever
     * settings change — including the first emission on startup, which replaces any
     * stale alarms from a previous session.
     */
    private fun observeSettingsForNotifications() {
        applicationScope.launch {
            userSettingsRepository.getUserSettings().collect { settings ->
                NotificationScheduler.scheduleAll(this@TherapyCompanionApp, settings)
            }
        }
    }

    companion object {
        const val CHANNEL_MORNING = "channel_morning"
        const val CHANNEL_CHECKIN = "channel_checkin"
        const val CHANNEL_EVENING = "channel_evening"
    }
}
