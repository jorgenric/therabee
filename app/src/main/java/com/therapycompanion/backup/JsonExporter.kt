package com.therapycompanion.backup

import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.UserSettings
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.OutputStream

/**
 * JSON backup format for the entire app database.
 *
 * Images are NOT included in the backup — only the filename reference is kept.
 * The export UI displays a clear disclaimer about this limitation (§10).
 *
 * All timestamps stored as UTC epoch milliseconds — never formatted strings.
 *
 * `schemaVersion` mirrors the Room database schema version at export time.
 * Importers MUST refuse files whose schemaVersion exceeds the app's current version (NFR-13).
 */
@Serializable
data class BackupData(
    val version: Int = 1,
    val schemaVersion: Int = JsonExporter.CURRENT_SCHEMA_VERSION,
    val exportedAt: Long = System.currentTimeMillis(),
    val exercises: List<ExerciseBackup>,
    val sessions: List<SessionBackup>,
    val checkIns: List<CheckInBackup>,
    val userSettings: UserSettingsBackup?
)

@Serializable
data class ExerciseBackup(
    val id: String,
    val name: String,
    val bodySystem: String,
    val instructions: String,
    val notes: String?,
    val durationMinutes: Int,
    val frequency: String,
    val scheduledDays: Int,
    val priority: Int,
    val active: Boolean,
    val imageFileName: String?,
    val videoFileName: String?,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class SessionBackup(
    val id: String,
    val exerciseId: String,
    val startedAt: Long,
    val completedAt: Long?,
    val elapsedSeconds: Long,
    val status: String,
    val notes: String?,
    val source: String = "Prompted"  // default preserves backward compat with old backups
)

@Serializable
data class CheckInBackup(
    val id: String,
    val checkedInAt: Long,
    val painScore: Int?,
    val energyScore: Int?,
    val bpiDomain: String?,
    val bpiScore: Int?,
    val freeText: String?,
    val dismissed: Boolean
)

@Serializable
data class UserSettingsBackup(
    val dailyLoad: Int,
    val easierDayEnabled: Boolean,
    val morningReminderEnabled: Boolean,
    val morningReminderTime: String,
    val afternoonCheckInEnabled: Boolean,
    val afternoonCheckInTime: String,
    val eveningEncouragementEnabled: Boolean,
    val eveningEncouragementTime: String,
    val quietHoursStart: String?,
    val quietHoursEnd: String?,
    val checkInsEnabled: Boolean,
    val showStreaks: Boolean = false   // default preserves backward compat with old backups
)

object JsonExporter {

    /** Must match AppDatabase.DATABASE_VERSION. Bumped whenever schema changes. */
    const val CURRENT_SCHEMA_VERSION = 6

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun buildBackupData(
        exercises: List<Exercise>,
        sessions: List<Session>,
        checkIns: List<CheckIn>,
        settings: UserSettings?
    ) = BackupData(
        exercises = exercises.map { it.toBackup() },
        sessions = sessions.map { it.toBackup() },
        checkIns = checkIns.map { it.toBackup() },
        userSettings = settings?.toBackup()
    )

    fun export(
        exercises: List<Exercise>,
        sessions: List<Session>,
        checkIns: List<CheckIn>,
        settings: UserSettings?,
        outputFile: File
    ) {
        outputFile.writeText(json.encodeToString(buildBackupData(exercises, sessions, checkIns, settings)))
    }

    fun export(
        exercises: List<Exercise>,
        sessions: List<Session>,
        checkIns: List<CheckIn>,
        settings: UserSettings?,
        outputStream: OutputStream
    ) {
        // Do NOT close the stream — the caller owns it and closes it via its own use{} block.
        outputStream.writer().apply {
            write(json.encodeToString(buildBackupData(exercises, sessions, checkIns, settings)))
            flush()
        }
    }

    fun parse(file: File): BackupData? =
        try { json.decodeFromString(file.readText()) } catch (_: Exception) { null }

    fun parse(text: String): BackupData? =
        try { json.decodeFromString(text) } catch (_: Exception) { null }

    // ── Mapping helpers ────────────────────────────────────────────────────────

    private fun Exercise.toBackup() = ExerciseBackup(
        id, name, bodySystem, instructions, notes, durationMinutes,
        frequency.name, scheduledDays, priority, active, imageFileName, videoFileName,
        createdAt, updatedAt
    )

    private fun Session.toBackup() = SessionBackup(
        id, exerciseId, startedAt, completedAt, elapsedSeconds, status.name, notes, source
    )

    private fun CheckIn.toBackup() = CheckInBackup(
        id, checkedInAt, painScore, energyScore, bpiDomain, bpiScore, freeText, dismissed
    )

    private fun UserSettings.toBackup() = UserSettingsBackup(
        dailyLoad, easierDayEnabled, morningReminderEnabled, morningReminderTime,
        afternoonCheckInEnabled, afternoonCheckInTime, eveningEncouragementEnabled,
        eveningEncouragementTime, quietHoursStart, quietHoursEnd, checkInsEnabled, showStreaks
    )
}
