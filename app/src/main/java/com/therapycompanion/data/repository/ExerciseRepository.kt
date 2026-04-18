package com.therapycompanion.data.repository

import com.therapycompanion.data.db.ExerciseDao
import com.therapycompanion.data.db.toDomain
import com.therapycompanion.data.db.toEntity
import com.therapycompanion.data.model.Exercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single source of truth for exercise data.
 * ViewModels never access the DAO directly — they go through this repository.
 * If a cloud sync layer is added in v2, it slots in here without touching any ViewModel.
 */
class ExerciseRepository(private val dao: ExerciseDao) {

    fun getAllExercises(): Flow<List<Exercise>> =
        dao.getAllExercises().map { list -> list.map { it.toDomain() } }

    fun getActiveExercises(): Flow<List<Exercise>> =
        dao.getActiveExercises().map { list -> list.map { it.toDomain() } }

    suspend fun getExerciseById(id: String): Exercise? =
        dao.getExerciseById(id)?.toDomain()

    suspend fun getExercisesForDay(dayBit: Int): List<Exercise> =
        dao.getExercisesForDay(dayBit).map { it.toDomain() }

    suspend fun getExercisesByBodySystem(bodySystem: String): List<Exercise> =
        dao.getExercisesByBodySystem(bodySystem).map { it.toDomain() }

    suspend fun getExerciseByName(name: String): Exercise? =
        dao.getExerciseByName(name)?.toDomain()

    suspend fun getActiveExerciseCount(): Int =
        dao.getActiveExerciseCount()

    fun getAllBodySystems(): Flow<List<String>> = dao.getAllBodySystems()

    suspend fun insertExercise(exercise: Exercise) =
        dao.insertExercise(exercise.toEntity())

    suspend fun insertExercises(exercises: List<Exercise>) =
        dao.insertExercises(exercises.map { it.toEntity() })

    suspend fun upsertExercise(exercise: Exercise) =
        dao.upsertExercise(exercise.toEntity())

    suspend fun updateExercise(exercise: Exercise) =
        dao.updateExercise(exercise.toEntity())

    suspend fun deleteExercise(exercise: Exercise) =
        dao.deleteExercise(exercise.toEntity())

    suspend fun deleteExerciseById(id: String) =
        dao.deleteExerciseById(id)

    suspend fun setExerciseActive(id: String, active: Boolean) =
        dao.setExerciseActive(id, active)
}
