package com.therapycompanion.ui.settings

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.backup.BackupData
import com.therapycompanion.backup.CsvExporter
import com.therapycompanion.backup.JsonExporter
import com.therapycompanion.backup.JsonImporter
import com.therapycompanion.backup.MergeStrategy
import com.therapycompanion.backup.RestoreResult
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.data.repository.CheckInRepository
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import com.therapycompanion.data.repository.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

private const val PREFS_NAME = "backup_prefs"
private const val KEY_LAST_EXPORT_AT = "last_export_at"

/** Days after which we remind the user to back up before an update. */
private const val BACKUP_REMINDER_DAYS = 14L

data class RestorePreview(
    val backup: BackupData,
    val exercises: Int,
    val sessions: Int,
    val checkIns: Int
)

data class SettingsUiState(
    val settings: UserSettings = UserSettings.Default,
    val isLoading: Boolean = true,
    val backupMessage: String? = null,
    /** Non-null while the import strategy picker dialog is shown. */
    val restorePreview: RestorePreview? = null,
    /** True when >14 days since last export AND new data exists since then. */
    val showBackupReminder: Boolean = false,
    /** True after the user types "reset" in the reset dialog. */
    val isResetting: Boolean = false,
    /** Non-null when a share sheet chooser intent is ready to fire. */
    val shareIntent: Intent? = null
)

class SettingsViewModel(
    application: Application,
    private val userSettingsRepository: UserSettingsRepository,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val checkInRepository: CheckInRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    init {
        viewModelScope.launch {
            userSettingsRepository.getUserSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
        checkBackupReminder()
    }

    // ── Settings persistence ───────────────────────────────────────────────────

    private fun save(updated: UserSettings) {
        viewModelScope.launch(Dispatchers.IO) {
            userSettingsRepository.updateSettings(updated)
        }
    }

    fun updateDailyLoad(load: Int) = save(_uiState.value.settings.copy(dailyLoad = load))
    fun updateMorningReminderEnabled(enabled: Boolean) = save(_uiState.value.settings.copy(morningReminderEnabled = enabled))
    fun updateMorningReminderTime(time: String) = save(_uiState.value.settings.copy(morningReminderTime = time))
    fun updateAfternoonCheckInEnabled(enabled: Boolean) = save(_uiState.value.settings.copy(afternoonCheckInEnabled = enabled))
    fun updateAfternoonCheckInTime(time: String) = save(_uiState.value.settings.copy(afternoonCheckInTime = time))
    fun updateEveningEncouragementEnabled(enabled: Boolean) = save(_uiState.value.settings.copy(eveningEncouragementEnabled = enabled))
    fun updateEveningEncouragementTime(time: String) = save(_uiState.value.settings.copy(eveningEncouragementTime = time))
    fun updateQuietHoursStart(time: String?) = save(_uiState.value.settings.copy(quietHoursStart = time))
    fun updateQuietHoursEnd(time: String?) = save(_uiState.value.settings.copy(quietHoursEnd = time))
    fun updateCheckInsEnabled(enabled: Boolean) = save(_uiState.value.settings.copy(checkInsEnabled = enabled))
    fun updateShowStreaks(enabled: Boolean) = save(_uiState.value.settings.copy(showStreaks = enabled))
    fun updateDisplayName(name: String) = save(_uiState.value.settings.copy(displayName = name))
    fun updateThemeMode(mode: String) = save(_uiState.value.settings.copy(themeMode = mode))

    // ── Export (file picker destination) ──────────────────────────────────────

    /**
     * Writes a full JSON backup to the URI provided by the system file picker.
     */
    fun exportTo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exercises = exerciseRepository.getAllExercisesOnce()
                val sessions  = sessionRepository.getAllSessionsOnce()
                val checkIns  = checkInRepository.getAllCheckInsOnce()
                val settings  = userSettingsRepository.getUserSettingsOnce()

                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    JsonExporter.export(exercises, sessions, checkIns, settings, out)
                }

                recordExportTime()
                _uiState.update {
                    it.copy(
                        backupMessage = "Backup saved — ${exercises.size} exercises, ${sessions.size} sessions.",
                        showBackupReminder = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Export failed: ${e.message}") }
            }
        }
    }

    // ── Export via share sheet (NFR-10) ────────────────────────────────────────

    /**
     * Writes a full JSON backup to the local backups dir AND populates [shareIntent]
     * for the UI to fire via `context.startActivity(shareIntent)`.
     */
    fun exportAndShare() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val app = getApplication<Application>()
                val exercises = exerciseRepository.getAllExercisesOnce()
                val sessions  = sessionRepository.getAllSessionsOnce()
                val checkIns  = checkInRepository.getAllCheckInsOnce()
                val settings  = userSettingsRepository.getUserSettingsOnce()

                // Write local copy to external files dir (no permission needed API 29+).
                val backupDir = File(app.getExternalFilesDir(null), "backups")
                backupDir.mkdirs()
                val today = LocalDate.now().toString()
                val backupFile = File(backupDir, "therapy_backup_$today.json")
                JsonExporter.export(exercises, sessions, checkIns, settings, backupFile)

                val uri = FileProvider.getUriForFile(
                    app,
                    "${app.packageName}.fileprovider",
                    backupFile
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "Therapy Companion backup $today")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                recordExportTime()
                _uiState.update {
                    it.copy(
                        shareIntent = Intent.createChooser(intent, "Share backup"),
                        showBackupReminder = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Share export failed: ${e.message}") }
            }
        }
    }

    // ── Restore — preview ──────────────────────────────────────────────────────

    /**
     * Reads the backup file and populates [restorePreview] for the strategy picker dialog.
     * Does not write to the database yet.
     */
    fun previewRestore(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = getApplication<Application>().contentResolver
                    .openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: run {
                        _uiState.update { it.copy(backupMessage = "Could not open backup file.") }
                        return@launch
                    }

                val backup = JsonExporter.parse(text)
                    ?: run {
                        _uiState.update { it.copy(backupMessage = "Could not parse backup data.") }
                        return@launch
                    }

                if (backup.schemaVersion > JsonExporter.CURRENT_SCHEMA_VERSION) {
                    _uiState.update {
                        it.copy(
                            backupMessage = "This backup was made with a newer app version " +
                                "(schema ${backup.schemaVersion}). Please update the app first."
                        )
                    }
                    return@launch
                }

                val preview = RestorePreview(
                    backup = backup,
                    exercises = backup.exercises.size,
                    sessions  = backup.sessions.size,
                    checkIns  = backup.checkIns.size
                )
                _uiState.update { it.copy(restorePreview = preview) }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Preview failed: ${e.message}") }
            }
        }
    }

    fun dismissRestorePreview() {
        _uiState.update { it.copy(restorePreview = null) }
    }

    // ── Restore — execute ──────────────────────────────────────────────────────

    fun confirmRestore(strategy: MergeStrategy) {
        val backup = _uiState.value.restorePreview?.backup ?: return
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(restorePreview = null) }
            val result = JsonImporter.restore(
                backup,
                exerciseRepository,
                sessionRepository,
                checkInRepository,
                userSettingsRepository,
                strategy
            )
            val message = when (result) {
                is RestoreResult.Success ->
                    "Restored: ${result.exercisesRestored} exercises, " +
                    "${result.sessionsRestored} sessions, " +
                    "${result.checkInsRestored} check-ins."
                is RestoreResult.Error -> result.message
            }
            _uiState.update { it.copy(backupMessage = message) }
        }
    }

    // ── Reset progress ─────────────────────────────────────────────────────────

    /**
     * Wipes all session and check-in history. Exercises and settings are preserved.
     * The caller must export a backup first (UI enforces this).
     */
    fun resetProgress() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isResetting = true) }
            try {
                checkInRepository.deleteAll()
                sessionRepository.deleteAll()
                _uiState.update {
                    it.copy(
                        isResetting = false,
                        backupMessage = "Progress reset. All sessions and check-ins deleted."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isResetting = false, backupMessage = "Reset failed: ${e.message}")
                }
            }
        }
    }

    // ── CSV exports ────────────────────────────────────────────────────────────

    fun exportSessionsCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sessions = sessionRepository.getAllSessionsOnce()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    CsvExporter.exportSessions(sessions, out)
                }
                _uiState.update { it.copy(backupMessage = "Session history exported (${sessions.size} rows).") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "CSV export failed: ${e.message}") }
            }
        }
    }

    fun exportCheckInsCsv(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val checkIns = checkInRepository.getAllCheckInsOnce()
                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    CsvExporter.exportCheckIns(checkIns, out)
                }
                _uiState.update { it.copy(backupMessage = "Check-in history exported (${checkIns.size} rows).") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "CSV export failed: ${e.message}") }
            }
        }
    }

    // ── Backup reminder ────────────────────────────────────────────────────────

    fun dismissBackupReminder() {
        recordExportTime() // treat dismissal as acknowledgment
        _uiState.update { it.copy(showBackupReminder = false) }
    }

    fun dismissBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
    }

    fun consumeShareIntent() {
        _uiState.update { it.copy(shareIntent = null) }
    }

    private fun recordExportTime() {
        prefs.edit().putLong(KEY_LAST_EXPORT_AT, System.currentTimeMillis()).apply()
    }

    private fun checkBackupReminder() {
        viewModelScope.launch(Dispatchers.IO) {
            val lastExport = prefs.getLong(KEY_LAST_EXPORT_AT, 0L)
            if (lastExport == 0L) return@launch // never exported — no nag on first launch

            val daysSince = (System.currentTimeMillis() - lastExport) / (86_400_000L)
            if (daysSince < BACKUP_REMINDER_DAYS) return@launch

            // Only show reminder if there is actually new data since last export.
            val newSessions = sessionRepository.getAllSessionsOnce()
                .any { it.startedAt > lastExport }
            val newCheckIns = checkInRepository.getAllCheckInsOnce()
                .any { it.checkedInAt > lastExport }

            if (newSessions || newCheckIns) {
                _uiState.update { it.copy(showBackupReminder = true) }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val userSettingsRepository: UserSettingsRepository,
        private val exerciseRepository: ExerciseRepository,
        private val sessionRepository: SessionRepository,
        private val checkInRepository: CheckInRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(
                application,
                userSettingsRepository,
                exerciseRepository,
                sessionRepository,
                checkInRepository
            ) as T
    }
}

