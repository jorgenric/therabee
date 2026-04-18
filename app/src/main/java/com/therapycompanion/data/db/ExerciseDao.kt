package com.therapycompanion.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    // ── Reads ─────────────────────────────────────────────────────

    @Query("SELECT * FROM exercises ORDER BY priority ASC, name ASC")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE active = 1 ORDER BY priority ASC, name ASC")
    fun getActiveExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id")
    suspend fun getExerciseById(id: String): ExerciseEntity?

    /**
     * Returns active exercises scheduled for a given day using the bitmask.
     * Example: to get Monday exercises, pass dayBit = DayBits.MON (= 1).
     */
    @Query("SELECT * FROM exercises WHERE (scheduled_days & :dayBit) != 0 AND active = 1")
    suspend fun getExercisesForDay(dayBit: Int): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE body_system = :bodySystem AND active = 1")
    suspend fun getExercisesByBodySystem(bodySystem: String): List<ExerciseEntity>

    @Query("SELECT * FROM exercises WHERE name = :name LIMIT 1")
    suspend fun getExerciseByName(name: String): ExerciseEntity?

    @Query("SELECT COUNT(*) FROM exercises WHERE active = 1")
    suspend fun getActiveExerciseCount(): Int

    /**
     * All distinct body system values in the active exercise library, sorted alphabetically.
     * Powers the autocomplete suggestion list on the add/edit form and library grouping.
     */
    @Query("SELECT DISTINCT body_system FROM exercises WHERE active = 1 ORDER BY body_system ASC")
    fun getAllBodySystems(): Flow<List<String>>

    // ── Writes ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercise(exercise: ExerciseEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExercises(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExercise(exercise: ExerciseEntity)

    @Update
    suspend fun updateExercise(exercise: ExerciseEntity)

    @Delete
    suspend fun deleteExercise(exercise: ExerciseEntity)

    @Query("DELETE FROM exercises WHERE id = :id")
    suspend fun deleteExerciseById(id: String)

    @Query("UPDATE exercises SET active = :active WHERE id = :id")
    suspend fun setExerciseActive(id: String, active: Boolean)
}
