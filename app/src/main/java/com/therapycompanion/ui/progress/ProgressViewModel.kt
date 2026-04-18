package com.therapycompanion.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.repository.CheckInRepository
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class ProgressUiState(
    val sessions: List<Session> = emptyList(),
    val checkIns: List<CheckIn> = emptyList(),
    /** Body systems worked this week (string values matching the exercise library). */
    val coveredBodySystems: Set<String> = emptySet(),
    /** All body systems present in the active exercise library — for the coverage widget. */
    val allBodySystems: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1)
)

class ProgressViewModel(
    private val sessionRepository: SessionRepository,
    private val checkInRepository: CheckInRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val zoneId = ZoneId.systemDefault()

    init {
        // Observe body systems live so the coverage widget updates when the library changes.
        viewModelScope.launch {
            exerciseRepository.getAllBodySystems().collect { systems ->
                _uiState.update { it.copy(allBodySystems = systems) }
            }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo = now - 30L * 24 * 3600 * 1000
            val sevenDaysAgo = now - 7L * 24 * 3600 * 1000

            val sessions = sessionRepository.getSessionsInDateRange(thirtyDaysAgo, now)
            val checkIns = checkInRepository.getCheckInsInDateRange(thirtyDaysAgo, now)
            val coveredBodySystems = sessionRepository.getCompletedBodySystemsSince(sevenDaysAgo).toSet()

            _uiState.update {
                it.copy(
                    sessions = sessions,
                    checkIns = checkIns,
                    coveredBodySystems = coveredBodySystems,
                    isLoading = false
                )
            }
        }
    }

    fun selectMonth(month: LocalDate) {
        _uiState.update { it.copy(selectedMonth = month.withDayOfMonth(1)) }
    }

    /** Returns session dates (epoch ms) for the selected calendar month */
    fun sessionDatesInMonth(): Set<LocalDate> {
        val state = _uiState.value
        val monthStart = state.selectedMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEnd = state.selectedMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return state.sessions
            .filter { it.status == SessionStatus.Completed && it.startedAt in monthStart until monthEnd }
            .map {
                java.time.Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate()
            }
            .toSet()
    }

    class Factory(
        private val sessionRepo: SessionRepository,
        private val checkInRepo: CheckInRepository,
        private val exerciseRepo: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProgressViewModel(sessionRepo, checkInRepo, exerciseRepo) as T
    }
}
