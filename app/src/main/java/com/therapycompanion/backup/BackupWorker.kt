package com.therapycompanion.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.therapycompanion.TherapyCompanionApp
import java.io.File
import java.time.LocalDate
import java.util.concurrent.TimeUnit

/**
 * Weekly rolling snapshot worker (NFR-12).
 *
 * Writes a timestamped JSON backup to getExternalFilesDir(null)/backups/.
 * No storage permission required on API 29+.
 * Validates round-trip before pruning old snapshots.
 * Keeps the 4 most recent files; older ones are deleted.
 */
class BackupWorker(
    context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val app = applicationContext as TherapyCompanionApp
        val backupDir = File(applicationContext.getExternalFilesDir(null), "backups")
        backupDir.mkdirs()

        return try {
            val exercises = app.exerciseRepository.getAllExercisesOnce()
            val sessions  = app.sessionRepository.getAllSessionsOnce()
            val checkIns  = app.checkInRepository.getAllCheckInsOnce()
            val settings  = app.userSettingsRepository.getUserSettingsOnce()

            val date = LocalDate.now().toString()
            val backupFile = File(backupDir, "auto_backup_$date.json")

            JsonExporter.export(exercises, sessions, checkIns, settings, backupFile)

            // Validate round-trip before pruning (NFR-12).
            val parsed = JsonExporter.parse(backupFile)
            if (parsed == null) {
                backupFile.delete()
                return Result.retry()
            }

            // Keep last 4 snapshots — prune oldest first.
            backupDir
                .listFiles { f -> f.name.startsWith("auto_backup_") && f.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() }
                ?.drop(4)
                ?.forEach { it.delete() }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "weekly_auto_backup"

        /**
         * Enqueues the weekly backup worker with KEEP policy (does not reset an existing schedule).
         * Safe to call on every app launch — WorkManager deduplicates by name.
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<BackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
