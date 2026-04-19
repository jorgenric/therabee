package com.therapycompanion.ui.settings

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.backup.JsonExporter
import com.therapycompanion.backup.JsonImporter
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

data class SettingsUiState(
    val settings: UserSettings = UserSettings.Default,
    val isLoading: Boolean = true,
    val backupMessage: String? = null
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

    init {
        viewModelScope.launch {
            userSettingsRepository.getUserSettings().collect { settings ->
                _uiState.update { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

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

    /**
     * Writes a full JSON backup to the URI provided by the system file picker.
     * The user picks the destination via CreateDocument — we just stream the data there.
     */
    fun exportTo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val exercises = exerciseRepository.getAllExercisesOnce()
                val sessions = sessionRepository.getAllSessionsOnce()
                val checkIns = checkInRepository.getAllCheckInsOnce()
                val settings = userSettingsRepository.getUserSettingsOnce()

                // Write to a temp file first, then stream to the target URI.
                val tmp = File(getApplication<Application>().cacheDir, "backup_tmp.json")
                JsonExporter.export(exercises, sessions, checkIns, settings, tmp)

                getApplication<Application>().contentResolver.openOutputStream(uri)?.use { out ->
                    tmp.inputStream().use { it.copyTo(out) }
                }
                tmp.delete()

                _uiState.update { it.copy(backupMessage = "Backup saved — ${exercises.size} exercises, ${sessions.size} sessions.") }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Export failed: ${e.message}") }
            }
        }
    }

    /**
     * Reads a JSON backup from the URI provided by the system file picker and restores all data.
     */
    fun restoreFrom(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val text = getApplication<Application>().contentResolver
                    .openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: run {
                        _uiState.update { it.copy(backupMessage = "Could not open backup file.") }
                        return@launch
                    }

                val result = JsonImporter.restore(
                    text,
                    exerciseRepository,
                    sessionRepository,
                    checkInRepository,
                    userSettingsRepository
                )

                val message = when (result) {
                    is RestoreResult.Success ->
                        "Restored: ${result.exercisesRestored} exercises, " +
                        "${result.sessionsRestored} sessions, " +
                        "${result.checkInsRestored} check-ins."
                    is RestoreResult.Error -> result.message
                }
                _uiState.update { it.copy(backupMessage = message) }
            } catch (e: Exception) {
                _uiState.update { it.copy(backupMessage = "Restore failed: ${e.message}") }
            }
        }
    }

    fun dismissBackupMessage() {
        _uiState.update { it.copy(backupMessage = null) }
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
