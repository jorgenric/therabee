package com.therapycompanion.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {

    // ── Reads ─────────────────────────────────────────────────────

    @Query("SELECT * FROM sessions ORDER BY started_at DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY started_at DESC")
    suspend fun getAllSessionsOnce(): List<SessionEntity>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: String): SessionEntity?

    /**
     * Sessions within a date range (UTC epoch ms).
     * Powers both the scheduling recurrence check and the progress calendar.
     */
    @Query("""
        SELECT * FROM sessions
        WHERE started_at >= :start AND started_at <= :end
        ORDER BY started_at DESC
    """)
    suspend fun getSessionsInDateRange(start: Long, end: Long): List<SessionEntity>

    @Query("""
        SELECT * FROM sessions
        WHERE started_at >= :start AND started_at <= :end
        ORDER BY started_at DESC
    """)
    fun getSessionsInDateRangeFlow(start: Long, end: Long): Flow<List<SessionEntity>>

    /**
     * Most recent session for a given exercise — used to sort by days-since-last-done.
     */
    @Query("""
        SELECT * FROM sessions
        WHERE exercise_id = :exerciseId
        ORDER BY started_at DESC
        LIMIT 1
    """)
    suspend fun getLastSessionForExercise(exerciseId: String): SessionEntity?

    /**
     * Count of COMPLETED sessions for an exercise within a Mon–Sun week window.
     * Skipped sessions deliberately excluded — they don't count against frequency.
     */
    @Query("""
        SELECT COUNT(*) FROM sessions
        WHERE exercise_id = :exerciseId
          AND status = 'Completed'
          AND started_at >= :weekStart
    """)
    suspend fun getCompletedSessionCountByExerciseInWeek(
        exerciseId: String,
        weekStart: Long
    ): Int

    /**
     * Check if there is a COMPLETED session for an exercise today.
     * Used for Daily frequency exhaustion check.
     */
    @Query("""
        SELECT COUNT(*) FROM sessions
        WHERE exercise_id = :exerciseId
          AND status = 'Completed'
          AND started_at >= :dayStart
          AND started_at <= :dayEnd
    """)
    suspend fun getCompletedSessionCountForExerciseToday(
        exerciseId: String,
        dayStart: Long,
        dayEnd: Long
    ): Int

    /**
     * All sessions for a specific exercise, most recent first.
     * Used in exercise detail / progress view.
     */
    @Query("""
        SELECT * FROM sessions
        WHERE exercise_id = :exerciseId
        ORDER BY started_at DESC
    """)
    fun getSessionsForExercise(exerciseId: String): Flow<List<SessionEntity>>

    /**
     * Distinct body systems that have at least one Completed session since [since].
     * Used by the body-system coverage widget on the Progress screen.
     */
    @Query("""
        SELECT DISTINCT e.body_system
        FROM sessions s
        INNER JOIN exercises e ON s.exercise_id = e.id
        WHERE s.status = 'Completed'
          AND s.started_at >= :since
    """)
    suspend fun getCompletedBodySystemsSince(since: Long): List<String>

    // ── Writes ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSession(session: SessionEntity)

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM sessions WHERE id = :id")
    suspend fun deleteSessionById(id: String)

    /** Merge-keep-both: skips rows whose id already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSessionIgnore(session: SessionEntity)

    @Query("DELETE FROM sessions")
    suspend fun deleteAll()
}
