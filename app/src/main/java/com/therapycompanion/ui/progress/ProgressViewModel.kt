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
import com.therapycompanion.data.repository.UserSettingsRepository
import com.therapycompanion.domain.scheduler.DailyScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class ProgressUiState(
    val sessions: List<Session> = emptyList(),
    val checkIns: List<CheckIn> = emptyList(),
    val coveredBodySystems: Set<String> = emptySet(),
    val allBodySystems: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val selectedMonth: LocalDate = LocalDate.now().withDayOfMonth(1),
    /** Consecutive-day streak (with one grace day allowed). 0 = no streak. */
    val currentStreak: Int = 0,
    /** Mirrors UserSettings.showStreaks — drives streak widget visibility. */
    val showStreaks: Boolean = false,
    /** Count of completed sessions in the current Mon–Sun week. */
    val exercisesThisWeek: Int = 0,
    /** exerciseId → name lookup for the session history log. */
    val exerciseNamesById: Map<String, String> = emptyMap()
)

class ProgressViewModel(
    private val sessionRepository: SessionRepository,
    private val checkInRepository: CheckInRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private val zoneId = ZoneId.systemDefault()

    init {
        viewModelScope.launch {
            exerciseRepository.getAllBodySystems().collect { systems ->
                _uiState.update { it.copy(allBodySystems = systems) }
            }
        }
        viewModelScope.launch {
            userSettingsRepository.getUserSettings().collect { settings ->
                _uiState.update { it.copy(showStreaks = settings.showStreaks) }
            }
        }
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            val thirtyDaysAgo  = now - 30L  * 24 * 3600 * 1000
            val sevenDaysAgo   = now -  7L  * 24 * 3600 * 1000
            // Load a full year for the calendar and streak — same query, wider window.
            val oneYearAgo     = now - 365L * 24 * 3600 * 1000

            val today     = LocalDate.now()
            val weekStart = DailyScheduler.currentWeekStart(today, zoneId)

            val sessions  = sessionRepository.getSessionsInDateRange(oneYearAgo, now)
            val checkIns  = checkInRepository.getCheckInsInDateRange(thirtyDaysAgo, now)
            val covered   = sessionRepository.getCompletedBodySystemsSince(sevenDaysAgo).toSet()
            val streak    = computeStreak(sessions, today)

            val exercisesThisWeek = sessions.count {
                it.status == SessionStatus.Completed && it.startedAt >= weekStart
            }

            val exerciseNames = exerciseRepository.getAllExercisesOnce()
                .associate { it.id to it.name }

            _uiState.update {
                it.copy(
                    sessions = sessions,
                    checkIns = checkIns,
                    coveredBodySystems = covered,
                    currentStreak = streak,
                    exercisesThisWeek = exercisesThisWeek,
                    exerciseNamesById = exerciseNames,
                    isLoading = false
                )
            }
        }
    }

    fun selectMonth(month: LocalDate) {
        _uiState.update { it.copy(selectedMonth = month.withDayOfMonth(1)) }
    }

    fun sessionDatesInMonth(): Set<LocalDate> {
        val state = _uiState.value
        val monthStart = state.selectedMonth.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val monthEnd   = state.selectedMonth.plusMonths(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

        return state.sessions
            .filter { it.status == SessionStatus.Completed && it.startedAt in monthStart until monthEnd }
            .map { Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate() }
            .toSet()
    }

    /**
     * Counts consecutive days ending today (or yesterday) that have at least one
     * completed session. One gap of exactly one day is forgiven (grace day).
     *
     * Examples:
     *   Mon✓ Tue✓ Wed– Thu✓ Fri✓ (today) → streak = 4 (Wed is the grace day)
     *   Mon✓ Tue– Wed– Thu✓ Fri✓ (today) → streak = 2 (two consecutive gaps break it)
     */
    private fun computeStreak(sessions: List<Session>, today: LocalDate): Int {
        val completedDates = sessions
            .filter { it.status == SessionStatus.Completed }
            .map { Instant.ofEpochMilli(it.startedAt).atZone(zoneId).toLocalDate() }
            .toSet()

        if (completedDates.isEmpty()) return 0

        // Streak must be anchored to today or yesterday (one grace day at the tail).
        val anchor = when {
            today in completedDates           -> today
            today.minusDays(1) in completedDates -> today.minusDays(1)
            else                              -> return 0
        }

        var streak         = 0
        var current        = anchor
        var graceDayUsed   = false

        while (true) {
            when {
                current in completedDates -> {
                    streak++
                    current = current.minusDays(1)
                }
                !graceDayUsed -> {
                    graceDayUsed = true
                    current = current.minusDays(1)
                }
                else -> break
            }
        }

        return streak
    }

    class Factory(
        private val sessionRepo: SessionRepository,
        private val checkInRepo: CheckInRepository,
        private val exerciseRepo: ExerciseRepository,
        private val userSettingsRepo: UserSettingsRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ProgressViewModel(sessionRepo, checkInRepo, exerciseRepo, userSettingsRepo) as T
    }
}
