package com.therapycompanion.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.data.repository.ExerciseRepository
import com.therapycompanion.data.repository.SessionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class LibraryUiState(
    val allExercises: List<Exercise> = emptyList(),
    /** Filtered + grouped result shown in the list. */
    val groupedExercises: Map<String, List<Exercise>> = emptyMap(),
    /** Distinct body systems — drives the filter chip row. */
    val allBodySystems: List<String> = emptyList(),
    val isLoading: Boolean = true,
    /** All groups collapsed by default (empty set = all collapsed). */
    val expandedSystems: Set<String> = emptySet(),
    val searchQuery: String = "",
    /** null = show all systems; non-null = show only that system. */
    val bodySystemFilter: String? = null,
    val filterNotDoneRecently: Boolean = false,
    /** IDs of exercises completed in the last 7 days. */
    val recentlyDoneIds: Set<String> = emptySet()
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

private const val RECENTLY_DONE_DAYS = 7L

class LibraryViewModel(
    private val exerciseRepository: ExerciseRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        // Observe exercises.
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { exercises ->
                val systems = exercises
                    .map { it.bodySystem.toTitleCase() }
                    .distinct()
                    .sortedBy { it.lowercase() }
                _uiState.update { it.copy(allExercises = exercises, allBodySystems = systems, isLoading = false) }
                _uiState.update { recompute(it) }
            }
        }

        // Observe sessions completed in the last 7 days for the "not done recently" filter.
        viewModelScope.launch {
            val sevenDaysAgo = System.currentTimeMillis() - RECENTLY_DONE_DAYS * 24 * 3600 * 1000
            sessionRepository.getSessionsInDateRangeFlow(sevenDaysAgo, Long.MAX_VALUE).collect { sessions ->
                val recentIds = sessions
                    .filter { it.status == SessionStatus.Completed }
                    .map { it.exerciseId }
                    .toSet()
                _uiState.update { recompute(it.copy(recentlyDoneIds = recentIds)) }
            }
        }
    }

    fun toggleBodySystem(system: String) {
        _uiState.update { state ->
            // Toggle only applies when not in search/filter mode (recompute handles expansion there).
            val expanded = state.expandedSystems.toMutableSet()
            if (system in expanded) expanded.remove(system) else expanded.add(system)
            state.copy(expandedSystems = expanded)
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { recompute(it.copy(searchQuery = query)) }
    }

    fun setBodySystemFilter(system: String?) {
        _uiState.update { recompute(it.copy(bodySystemFilter = system)) }
    }

    fun setNotDoneRecentlyFilter(enabled: Boolean) {
        _uiState.update { recompute(it.copy(filterNotDoneRecently = enabled)) }
    }

    fun clearFilters() {
        _uiState.update { recompute(it.copy(searchQuery = "", bodySystemFilter = null, filterNotDoneRecently = false)) }
    }

    /**
     * Applies search query and active filters to [allExercises], groups the result,
     * and auto-expands matching groups whenever any filter is active.
     */
    private fun recompute(state: LibraryUiState): LibraryUiState {
        val query = state.searchQuery.trim().lowercase()
        val anyFilterActive = query.isNotBlank() || state.bodySystemFilter != null || state.filterNotDoneRecently

        var filtered = state.allExercises

        if (state.bodySystemFilter != null) {
            filtered = filtered.filter { it.bodySystem.equals(state.bodySystemFilter, ignoreCase = true) }
        }
        if (state.filterNotDoneRecently) {
            filtered = filtered.filter { it.id !in state.recentlyDoneIds }
        }
        if (query.isNotBlank()) {
            filtered = filtered.filter { ex ->
                ex.name.lowercase().contains(query) ||
                ex.bodySystem.lowercase().contains(query) ||
                ex.instructions.lowercase().contains(query) ||
                ex.notes?.lowercase()?.contains(query) == true
            }
        }

        val grouped = filtered
            .groupBy { it.bodySystem.toTitleCase() }
            .entries
            .sortedBy { it.key.lowercase() }
            .associate { it.key to it.value }

        // Auto-expand all matching groups when a filter/search is active;
        // otherwise respect the user's manual expand/collapse state.
        val expanded = if (anyFilterActive) grouped.keys.toSet() else state.expandedSystems

        return state.copy(groupedExercises = grouped, expandedSystems = expanded)
    }

    class Factory(
        private val exerciseRepo: ExerciseRepository,
        private val sessionRepo: SessionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            LibraryViewModel(exerciseRepo, sessionRepo) as T
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
