package com.therapycompanion.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.repository.ExerciseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LibraryUiState(
    /** Exercises grouped by title-cased body system, sorted alphabetically. */
    val groupedExercises: Map<String, List<Exercise>> = emptyMap(),
    val isLoading: Boolean = true,
    /** Which body system headers are currently expanded. Defaults to all open on first load. */
    val expandedSystems: Set<String> = emptySet()
)

data class ExerciseEditUiState(
    val exercise: Exercise? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val savedSuccessfully: Boolean = false,

    // Edit form fields
    val name: String = "",
    val bodySystem: String = "",
    val instructions: String = "",
    val notes: String = "",
    val durationMinutes: String = "10",
    val frequency: Frequency = Frequency.Daily,
    val scheduledDays: Int = DayBits.ALL,
    val priority: Int = 2,
    val active: Boolean = true,
    val imageFileName: String? = null,

    /** All body systems already in the library — used to populate autocomplete suggestions. */
    val allBodySystems: List<String> = emptyList(),

    val nameError: String? = null,
    val bodySystemError: String? = null
)

// ── LibraryViewModel ──────────────────────────────────────────────────────────

class LibraryViewModel(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { exercises ->
                val grouped = exercises
                    .groupBy { it.bodySystem.toTitleCase() }
                    .entries
                    .sortedBy { it.key.lowercase() }
                    .associate { it.key to it.value }

                _uiState.update { current ->
                    // On first load, expand all groups by default.
                    val expanded = if (current.expandedSystems.isEmpty()) grouped.keys.toSet()
                                   else current.expandedSystems
                    current.copy(groupedExercises = grouped, isLoading = false, expandedSystems = expanded)
                }
            }
        }
    }

    fun toggleBodySystem(system: String) {
        _uiState.update { state ->
            val expanded = state.expandedSystems.toMutableSet()
            if (system in expanded) expanded.remove(system) else expanded.add(system)
            state.copy(expandedSystems = expanded)
        }
    }

    class Factory(private val repo: ExerciseRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LibraryViewModel(repo) as T
    }
}

// ── ExerciseEditViewModel ─────────────────────────────────────────────────────

class ExerciseEditViewModel(
    private val exerciseId: String?,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseEditUiState())
    val uiState: StateFlow<ExerciseEditUiState> = _uiState.asStateFlow()

    init {
        // Observe all body systems so the autocomplete list stays live.
        viewModelScope.launch {
            exerciseRepository.getAllBodySystems().collect { systems ->
                _uiState.update { it.copy(allBodySystems = systems) }
            }
        }

        if (exerciseId != null) {
            loadExercise(exerciseId)
        } else {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private fun loadExercise(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val exercise = exerciseRepository.getExerciseById(id)
            _uiState.update { state ->
                if (exercise != null) {
                    state.copy(
                        exercise = exercise,
                        isLoading = false,
                        name = exercise.name,
                        bodySystem = exercise.bodySystem,
                        instructions = exercise.instructions,
                        notes = exercise.notes ?: "",
                        durationMinutes = exercise.durationMinutes.toString(),
                        frequency = exercise.frequency,
                        scheduledDays = exercise.scheduledDays,
                        priority = exercise.priority,
                        active = exercise.active,
                        imageFileName = exercise.imageFileName
                    )
                } else {
                    state.copy(isLoading = false)
                }
            }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value, nameError = null) }
    fun updateBodySystem(value: String) = _uiState.update { it.copy(bodySystem = value, bodySystemError = null) }
    fun updateInstructions(value: String) = _uiState.update { it.copy(instructions = value) }
    fun updateNotes(value: String) = _uiState.update { it.copy(notes = value) }
    fun updateDuration(value: String) = _uiState.update { it.copy(durationMinutes = value) }
    fun updateFrequency(value: Frequency) = _uiState.update { it.copy(frequency = value) }
    fun updateScheduledDays(value: Int) = _uiState.update { it.copy(scheduledDays = value) }
    fun updatePriority(value: Int) = _uiState.update { it.copy(priority = value) }
    fun updateActive(value: Boolean) = _uiState.update { it.copy(active = value) }
    fun updateImageFileName(value: String?) = _uiState.update { it.copy(imageFileName = value) }

    fun save() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }
        if (state.bodySystem.isBlank()) {
            _uiState.update { it.copy(bodySystemError = "Body system is required") }
            hasError = true
        }
        if (hasError) return

        // Normalize body system: trim and title-case each word.
        val normalizedBodySystem = state.bodySystem.toTitleCase()

        val duration = state.durationMinutes.toIntOrNull() ?: 10
        val now = System.currentTimeMillis()

        val existing = state.exercise
        val exercise = if (existing != null) {
            existing.copy(
                name = state.name.trim(),
                bodySystem = normalizedBodySystem,
                instructions = state.instructions.trim(),
                notes = state.notes.trim().ifBlank { null },
                durationMinutes = duration,
                frequency = state.frequency,
                scheduledDays = state.scheduledDays,
                priority = state.priority,
                active = state.active,
                imageFileName = state.imageFileName,
                updatedAt = now
            )
        } else {
            Exercise(
                id = UUID.randomUUID().toString(),
                name = state.name.trim(),
                bodySystem = normalizedBodySystem,
                instructions = state.instructions.trim(),
                notes = state.notes.trim().ifBlank { null },
                durationMinutes = duration,
                frequency = state.frequency,
                scheduledDays = state.scheduledDays,
                priority = state.priority,
                active = state.active,
                imageFileName = state.imageFileName,
                videoFileName = null,
                createdAt = now,
                updatedAt = now
            )
        }

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch(Dispatchers.IO) {
            if (existing != null) {
                exerciseRepository.updateExercise(exercise)
            } else {
                exerciseRepository.insertExercise(exercise)
            }
            _uiState.update { it.copy(isSaving = false, savedSuccessfully = true) }
        }
    }

    class Factory(
        private val exerciseId: String?,
        private val repo: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ExerciseEditViewModel(exerciseId, repo) as T
    }
}

// ── String extension ──────────────────────────────────────────────────────────

/** Trims and title-cases each space-separated word: "lower extremity" → "Lower Extremity". */
fun String.toTitleCase(): String =
    trim().split(Regex("\\s+"))
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercaseChar() } }
