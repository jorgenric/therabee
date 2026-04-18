package com.therapycompanion.ui.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
import java.util.UUID

data class SessionUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true,
    val elapsedSeconds: Long = 0L,
    val isRunning: Boolean = false,
    val isComplete: Boolean = false,
    val currentSessionId: String? = null
)

class SessionViewModel(
    private val exerciseId: String,
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var sessionStartTime: Long = 0L
    private var sessionId: String = UUID.randomUUID().toString()

    init {
        loadExercise()
    }

    private fun loadExercise() {
        viewModelScope.launch(Dispatchers.IO) {
            val exercise = exerciseRepository.getExerciseById(exerciseId)
            _uiState.update { it.copy(exercise = exercise, isLoading = false) }
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
                    notes = null
                )
            )
        }
        _uiState.update { it.copy(isRunning = true, currentSessionId = sessionId) }
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    state.copy(elapsedSeconds = state.elapsedSeconds + 1)
                }
            }
        }
    }

    fun markComplete() {
        timerJob?.cancel()
        val elapsed = _uiState.value.elapsedSeconds
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
                    notes = null
                )
            )
        }
        _uiState.update { it.copy(isRunning = false, isComplete = true) }
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
                    notes = null
                )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    class Factory(
        private val exerciseId: String,
        private val exerciseRepository: ExerciseRepository,
        private val sessionRepository: SessionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            SessionViewModel(exerciseId, exerciseRepository, sessionRepository) as T
    }
}
