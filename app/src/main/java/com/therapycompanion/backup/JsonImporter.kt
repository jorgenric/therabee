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
 * Three import strategies (NFR-13):
 *
 *  - Replace: wipe local data first, then insert everything from the file.
 *  - MergeKeepBoth: INSERT IGNORE — existing rows are untouched, new IDs are inserted.
 *  - MergePreferFile: exercises from file win (upsert); session + check-in data is combined
 *    (INSERT IGNORE so duplicates are silently skipped).
 */
enum class MergeStrategy { Replace, MergeKeepBoth, MergePreferFile }

/**
 * Restores all data from a JSON backup file.
 * Refuses files whose schemaVersion exceeds the app's current schema version (NFR-13).
 */
object JsonImporter {

    suspend fun restore(
        file: File,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository,
        strategy: MergeStrategy = MergeStrategy.Replace
    ): RestoreResult {
        val backup = JsonExporter.parse(file)
            ?: return RestoreResult.Error("Could not parse backup file. The file may be corrupted or from an incompatible version.")
        return restoreFromBackup(backup, exerciseRepository, sessionRepository, checkInRepository, userSettingsRepository, strategy)
    }

    suspend fun restore(
        text: String,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository,
        strategy: MergeStrategy = MergeStrategy.Replace
    ): RestoreResult {
        val backup = JsonExporter.parse(text)
            ?: return RestoreResult.Error("Could not parse backup data.")
        return restoreFromBackup(backup, exerciseRepository, sessionRepository, checkInRepository, userSettingsRepository, strategy)
    }

    /** Restores from an already-parsed [BackupData] (e.g., after previewing it). */
    suspend fun restore(
        backup: BackupData,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository,
        strategy: MergeStrategy = MergeStrategy.Replace
    ): RestoreResult = restoreFromBackup(backup, exerciseRepository, sessionRepository, checkInRepository, userSettingsRepository, strategy)

    private suspend fun restoreFromBackup(
        backup: BackupData,
        exerciseRepository: ExerciseRepository,
        sessionRepository: SessionRepository,
        checkInRepository: CheckInRepository,
        userSettingsRepository: UserSettingsRepository,
        strategy: MergeStrategy
    ): RestoreResult {
        // NFR-13: refuse files from a newer schema version.
        if (backup.schemaVersion > JsonExporter.CURRENT_SCHEMA_VERSION) {
            return RestoreResult.Error(
                "This backup was made with a newer version of the app (schema ${backup.schemaVersion}). " +
                "Please update the app before restoring."
            )
        }

        return try {
            var exercisesRestored = 0
            var sessionsRestored = 0
            var checkInsRestored = 0

            when (strategy) {
                MergeStrategy.Replace -> {
                    // Wipe existing data, then insert everything from the backup.
                    checkInRepository.deleteAll()
                    sessionRepository.deleteAll()
                    exerciseRepository.deleteAll()

                    backup.exercises.forEach { eb ->
                        val exercise = eb.toDomain() ?: return@forEach
                        exerciseRepository.insertExercise(exercise)
                        exercisesRestored++
                    }
                    backup.sessions.forEach { sb ->
                        val session = sb.toDomain() ?: return@forEach
                        sessionRepository.insertSession(session)
                        sessionsRestored++
                    }
                    backup.checkIns.forEach { cb ->
                        checkInRepository.insertCheckIn(cb.toDomain())
                        checkInsRestored++
                    }
                }

                MergeStrategy.MergeKeepBoth -> {
                    // INSERT IGNORE — existing rows win, new IDs are added.
                    backup.exercises.forEach { eb ->
                        val exercise = eb.toDomain() ?: return@forEach
                        exerciseRepository.insertExerciseIgnore(exercise)
                        exercisesRestored++
                    }
                    backup.sessions.forEach { sb ->
                        val session = sb.toDomain() ?: return@forEach
                        sessionRepository.insertSessionIgnore(session)
                        sessionsRestored++
                    }
                    backup.checkIns.forEach { cb ->
                        checkInRepository.insertCheckInIgnore(cb.toDomain())
                        checkInsRestored++
                    }
                }

                MergeStrategy.MergePreferFile -> {
                    // Exercises: file wins (upsert).
                    // Sessions + check-ins: combined (INSERT IGNORE to avoid duplicates).
                    backup.exercises.forEach { eb ->
                        val exercise = eb.toDomain() ?: return@forEach
                        exerciseRepository.upsertExercise(exercise)
                        exercisesRestored++
                    }
                    backup.sessions.forEach { sb ->
                        val session = sb.toDomain() ?: return@forEach
                        sessionRepository.insertSessionIgnore(session)
                        sessionsRestored++
                    }
                    backup.checkIns.forEach { cb ->
                        checkInRepository.insertCheckInIgnore(cb.toDomain())
                        checkInsRestored++
                    }
                }
            }

            backup.userSettings?.let { sb ->
                userSettingsRepository.updateSettings(sb.toDomain())
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
            notes = notes,
            source = source
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
        checkInsEnabled = checkInsEnabled,
        showStreaks = showStreaks
    )
}
