package com.therapycompanion.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class SessionUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true,
    /** Seconds remaining on the optional countdown timer. Initialised to durationMinutes × 60. */
    val remainingSeconds: Int = 0,
    /** True while the countdown is actively ticking. */
    val timerActive: Boolean = false,
    /** True once the user has started the timer at least once — controls timer visibility. */
    val timerStarted: Boolean = false,
    /** True when Done has been tapped — shows the acknowledgment overlay. */
    val showAcknowledgment: Boolean = false,
    val acknowledgmentMessage: String = "",
    val nextExerciseId: String? = null,
    val nextExerciseName: String? = null,
    val currentSessionId: String? = null
)

class SessionViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository,
    private val acknowledgmentMessages: List<String>
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var sessionId: String = UUID.randomUUID().toString()
    /** Total seconds for the current exercise — set once the exercise loads. */
    private var totalSeconds: Int = 0

    init {
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch(Dispatchers.IO) {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            if (exercise != null) {
                totalSeconds = exercise.durationMinutes * 60
            }
            _uiState.update {
                it.copy(
                    exercise = exercise,
                    isLoading = false,
                    remainingSeconds = totalSeconds
                )
            }
            if (exercise != null) {
                startSession()
            }
        }
    }

    private fun startSession() {
        sessionStartTime = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.insertSession(
                Session(
                    id = sessionId,
                    exerciseId = exerciseId,
                    startedAt = sessionStartTime,
                    completedAt = null,
                    elapsedSeconds = 0,
                    status = SessionStatus.InProgress,
                    notes = null,
                    source = Session.SOURCE_PROMPTED
                )
            )
        }
        _uiState.update { it.copy(currentSessionId = sessionId) }
        // Timer is NOT auto-started — user activates it via toggleTimer().
    }

    /**
     * Starts or pauses the optional countdown timer.
     * The timer counts down from [totalSeconds] and stops automatically at zero.
     */
    fun toggleTimer() {
        if (_uiState.value.timerActive) {
            timerJob?.cancel()
            _uiState.update { it.copy(timerActive = false) }
        } else {
            _uiState.update { it.copy(timerActive = true, timerStarted = true) }
            timerJob?.cancel()
            timerJob = viewModelScope.launch {
                while (true) {
                    delay(1000)
                    val current = _uiState.value.remainingSeconds
                    val next = (current - 1).coerceAtLeast(0)
                    _uiState.update { state ->
                        state.copy(
                            remainingSeconds = next,
                            timerActive = next > 0
                        )
                    }
                    if (next == 0) break
                }
            }
        }
    }

    /**
     * User tapped X (close). Returns to Home without recording any session.
     * The UI should call onCancel() immediately after this to navigate away;
     * the DB delete completes in the background.
     */
    fun cancelSession() {
        timerJob?.cancel()
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.deleteSessionById(sessionId)
        }
    }

    fun markSkipped() {
        timerJob?.cancel()
        val now = System.currentTimeMillis()
        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.updateSession(
                Session(
                    id = sessionId,
                    exerciseId = exerciseId,
                    startedAt = sessionStartTime,
                    completedAt = now,
                    elapsedSeconds = 0,
                    status = SessionStatus.Skipped,
                    notes = null,
                    source = Session.SOURCE_PROMPTED
                )
            )
        }
    }

    fun markComplete() {
        timerJob?.cancel()
        val elapsed = (totalSeconds - _uiState.value.remainingSeconds).toLong().coerceAtLeast(0L)
        val now = System.currentTimeMillis()

        viewModelScope.launch(Dispatchers.IO) {
            sessionRepository.updateSession(
                Session(
                    id = sessionId,
                    exerciseId = exerciseId,
                    startedAt = sessionStartTime,
                    completedAt = now,
                    elapsedSeconds = elapsed,
                    status = SessionStatus.Completed,
                    notes = null,
                    source = Session.SOURCE_PROMPTED
                )
            )
            val next = findNextExercise()
            _uiState.update {
                it.copy(
                    showAcknowledgment = true,
                    acknowledgmentMessage = acknowledgmentMessages.randomOrNull() ?: "Great work!",
                    nextExerciseId = next?.id,
                    nextExerciseName = next?.name
                )
            }
        }
    }

    /**
     * Finds the highest-priority exercise scheduled for today that hasn't been
     * completed yet, excluding the current exercise.
     */
    private suspend fun findNextExercise(): Exercise? {
        val today = LocalDate.now()
        val zoneId = ZoneId.systemDefault()
        val todayDayBit = DayBits.fromDayOfWeek(today.dayOfWeek)
        val dayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val dayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1

        val completedTodayIds = sessionRepository
            .getSessionsInDateRange(dayStart, dayEnd)
            .filter { it.status == SessionStatus.Completed }
            .map { it.exerciseId }
            .toSet()

        return exerciseRepository
            .getExercisesForDay(todayDayBit)
            .filter { it.id != exerciseId }
            .filter { it.id !in completedTodayIds }
            .minByOrNull { it.priority }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val exerciseId: String,
        private val exerciseRepository: ExerciseRepository,
        private val sessionRepository: SessionRepository,
        private val acknowledgmentMessages: List<String>
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(exerciseId, exerciseRepository, sessionRepository, acknowledgmentMessages) as T
    }
}
