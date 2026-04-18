package com.therapycompanion.backup

import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.data.repository.CheckInRepository
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import com.therapycompanion.data.repository.UserSettingsRepository
import java.io.File

sealed class RestoreResult {
    data class Success(
        val exercisesRestored: Int,
        val sessionsRestored: Int,
        val checkInsRestored: Int
    ) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

/**
 * Restores all data from a JSON backup file.
 * Existing data with matching IDs is replaced (upsert behavior).
 */
object JsonImporter {

    suspend fun restore(
        file: File,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository
    ): RestoreResult {
        val backup = JsonExporter.parse(file)
            ?: return RestoreResult.Error("Could not parse backup file. The file may be corrupted or from an incompatible version.")

        return restoreFromBackup(backup, exerciseRepository, sessionRepository, checkInRepository, userSettingsRepository)
    }

    suspend fun restore(
        text: String,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository
    ): RestoreResult {
        val backup = JsonExporter.parse(text)
            ?: return RestoreResult.Error("Could not parse backup data.")

        return restoreFromBackup(backup, exerciseRepository, sessionRepository, checkInRepository, userSettingsRepository)
    }

    private suspend fun restoreFromBackup(
        backup: BackupData,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository
    ): RestoreResult {
        return try {
            var exercisesRestored = 0
            var sessionsRestored = 0
            var checkInsRestored = 0

            backup.exercises.forEach { eb ->
                val exercise = eb.toDomain() ?: return@forEach
                exerciseRepository.upsertExercise(exercise)
                exercisesRestored++
            }

            backup.sessions.forEach { sb ->
                val session = sb.toDomain() ?: return@forEach
                // Try update first, insert if not exists
                try {
                    sessionRepository.updateSession(session)
                } catch (_: Exception) {
                    try { sessionRepository.insertSession(session) } catch (_: Exception) {}
                }
                sessionsRestored++
            }

            backup.checkIns.forEach { cb ->
                val checkIn = cb.toDomain()
                try {
                    checkInRepository.updateCheckIn(checkIn)
                } catch (_: Exception) {
                    try { checkInRepository.insertCheckIn(checkIn) } catch (_: Exception) {}
                }
                checkInsRestored++
            }

            backup.userSettings?.let { sb ->
                val settings = sb.toDomain()
                userSettingsRepository.updateSettings(settings)
            }

            RestoreResult.Success(exercisesRestored, sessionsRestored, checkInsRestored)
        } catch (e: Exception) {
            RestoreResult.Error("Restore failed: ${e.message}")
        }
    }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private fun ExerciseBackup.toDomain(): Exercise? {
        if (bodySystem.isBlank()) return null
        val freq = try { Frequency.fromString(frequency) } catch (_: Exception) { return null }
        return Exercise(
            id = id,
            name = name,
            bodySystem = bodySystem,
            instructions = instructions,
            notes = notes,
            durationMinutes = durationMinutes,
            frequency = freq,
            scheduledDays = scheduledDays,
            priority = priority,
            active = active,
            imageFileName = imageFileName,
            videoFileName = videoFileName,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    private fun SessionBackup.toDomain(): Session? {
        val st = try { SessionStatus.fromString(status) } catch (_: Exception) { return null }
        return Session(
            id = id,
            exerciseId = exerciseId,
            startedAt = startedAt,
            completedAt = completedAt,
            elapsedSeconds = elapsedSeconds,
            status = st,
            notes = notes
        )
    }

    private fun CheckInBackup.toDomain() = CheckIn(
        id = id,
        checkedInAt = checkedInAt,
        painScore = painScore,
        energyScore = energyScore,
        bpiDomain = bpiDomain,
        bpiScore = bpiScore,
        freeText = freeText,
        dismissed = dismissed
    )

    private fun UserSettingsBackup.toDomain() = UserSettings(
        dailyLoad = dailyLoad,
        easierDayEnabled = easierDayEnabled,
        morningReminderEnabled = morningReminderEnabled,
        morningReminderTime = morningReminderTime,
        afternoonCheckInEnabled = afternoonCheckInEnabled,
        afternoonCheckInTime = afternoonCheckInTime,
        eveningEncouragementEnabled = eveningEncouragementEnabled,
        eveningEncouragementTime = eveningEncouragementTime,
        quietHoursStart = quietHoursStart,
        quietHoursEnd = quietHoursEnd,
        checkInsEnabled = checkInsEnabled
    )
}
