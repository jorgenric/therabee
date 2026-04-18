package com.therapycompanion.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.data.repository.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val settings: UserSettings = UserSettings.Default,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

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

    class Factory(private val repo: UserSettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SettingsViewModel(repo) as T
    }
}
