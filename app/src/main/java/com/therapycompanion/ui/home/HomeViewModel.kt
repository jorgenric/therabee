package com.therapycompanion.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.model.UserSettings
import com.therapycompanion.data.repository.CheckInRepository
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import com.therapycompanion.data.repository.UserSettingsRepository
import com.therapycompanion.domain.scheduler.DailyScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.util.UUID

data class HomeUiState(
    val todaysExercises: List<ExerciseWithStatus> = emptyList(),
    val settings: UserSettings = UserSettings.Default,
    val isLoading: Boolean = true,
    val currentDate: LocalDate = LocalDate.now(),
    val showCheckInPrompt: Boolean = false
)

data class ExerciseWithStatus(
    val exercise: Exercise,
    val status: SessionStatus?,  // null = not yet started today
    val sessionId: String? = null
)

class HomeViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val checkInRepository: CheckInRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var cachedDate: LocalDate? = null
    private var cachedInputExercises: List<Exercise>? = null
    private var cachedDailyLoad: Int? = null
    private var cachedEasierDay: Boolean? = null
    private var cachedExercises: List<Exercise>? = null

    /** Prevents re-prompting within the same app session. */
    private var hasShownCheckInThisSession = false

    init {
        observeData()
    }

    private fun observeData() {
        viewModelScope.launch {
            // Include getAllSessions() in the combine so any session insert/update
            // (completion or skip) automatically triggers a recompute without
            // requiring an explicit refresh call.
            combine(
                exerciseRepository.getActiveExercises(),
                userSettingsRepository.getUserSettings(),
                sessionRepository.getAllSessions()
            ) { exercises, settings, _ -> exercises to settings }
                .collect { (exercises, settings) ->
                    recomputeDailyList(exercises, settings)
                }
        }
    }

    private suspend fun recomputeDailyList(
        allExercises: List<Exercise>,
        settings: UserSettings
    ) {
        val today = LocalDate.now()
        val todayDayBit = DayBits.fromDayOfWeek(today.dayOfWeek)
        val (dayStart, dayEnd) = DailyScheduler.todayBoundaries(today)
        val weekStart = DailyScheduler.currentWeekStart(today)

        val recentSessions = withContext(Dispatchers.IO) {
            sessionRepository.getSessionsInDateRange(start = weekStart, end = dayEnd)
        }

        val scheduled = if (
            today == cachedDate &&
            allExercises == cachedInputExercises &&
            settings.dailyLoad == cachedDailyLoad &&
            settings.easierDayEnabled == cachedEasierDay &&
            cachedExercises != null
        ) {
            cachedExercises!!
        } else {
            DailyScheduler.selectDailyExercises(
                allExercises = allExercises,
                recentSessions = recentSessions,
                todayDayBit = todayDayBit,
                dailyLoad = settings.dailyLoad,
                easierDay = settings.easierDayEnabled,
                today = today
            ).also {
                cachedDate = today
                cachedInputExercises = allExercises
                cachedDailyLoad = settings.dailyLoad
                cachedEasierDay = settings.easierDayEnabled
                cachedExercises = it
            }
        }

        val todaySessions = recentSessions.filter { it.startedAt in dayStart..dayEnd }
        val sessionByExercise = todaySessions.associateBy { it.exerciseId }

        val exercisesWithStatus = scheduled.map { exercise ->
            val session = sessionByExercise[exercise.id]
            ExerciseWithStatus(
                exercise = exercise,
                status = session?.status,
                sessionId = session?.id
            )
        }

        // Show the check-in prompt if enabled, not yet shown this session, and not done today.
        val showPrompt = if (settings.checkInsEnabled && !hasShownCheckInThisSession) {
            withContext(Dispatchers.IO) {
                !checkInRepository.hasCompletedCheckInToday(dayStart, dayEnd)
            }
        } else {
            false
        }
        if (showPrompt) hasShownCheckInThisSession = true

        _uiState.update { state ->
            state.copy(
                todaysExercises = exercisesWithStatus,
                settings = settings,
                isLoading = false,
                currentDate = today,
                showCheckInPrompt = showPrompt
            )
        }
    }

    fun toggleEasierDay(enabled: Boolean) {
        viewModelScope.launch {
            userSettingsRepository.setEasierDayEnabled(enabled)
            cachedDate = null
            cachedInputExercises = null
            cachedDailyLoad = null
            cachedEasierDay = null
            cachedExercises = null
        }
    }

    fun markSkipped(exerciseId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            sessionRepository.insertSession(
                Session(
                    id = UUID.randomUUID().toString(),
                    exerciseId = exerciseId,
                    startedAt = now,
                    completedAt = now,
                    elapsedSeconds = 0,
                    status = SessionStatus.Skipped,
                    notes = null
                )
            )
        }
    }

    fun submitCheckIn(
        painScore: Int,
        energyScore: Int,
        bpiDomain: String?,
        bpiScore: Int?,
        freeText: String?
    ) {
        _uiState.update { it.copy(showCheckInPrompt = false) }
        viewModelScope.launch(Dispatchers.IO) {
            checkInRepository.insertCheckIn(
                CheckIn(
                    id = UUID.randomUUID().toString(),
                    checkedInAt = System.currentTimeMillis(),
                    painScore = painScore,
                    energyScore = energyScore,
                    bpiDomain = bpiDomain,
                    bpiScore = bpiScore,
                    freeText = freeText,
                    dismissed = false
                )
            )
        }
    }

    fun dismissCheckInPrompt() {
        hasShownCheckInThisSession = true
        _uiState.update { it.copy(showCheckInPrompt = false) }
    }

    fun refreshDailyList() {
        cachedDate = null
        cachedInputExercises = null
        cachedDailyLoad = null
        cachedEasierDay = null
        cachedExercises = null
    }

    class Factory(
        private val exerciseRepository: ExerciseRepository,
        private val sessionRepository: SessionRepository,
        private val userSettingsRepository: UserSettingsRepository,
        private val checkInRepository: CheckInRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(
                exerciseRepository,
                sessionRepository,
                userSettingsRepository,
                checkInRepository
            ) as T
    }
}
