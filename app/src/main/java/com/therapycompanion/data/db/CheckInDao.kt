package com.therapycompanion.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CheckInDao {

    // ── Reads ─────────────────────────────────────────────────────

    @Query("SELECT * FROM check_ins ORDER BY checked_in_at DESC")
    fun getAllCheckIns(): Flow<List<CheckInEntity>>

    @Query("SELECT * FROM check_ins WHERE id = :id")
    suspend fun getCheckInById(id: String): CheckInEntity?

    /**
     * Check-ins within a date range — powers the FPS-R trend chart on Progress screen.
     */
    @Query("""
        SELECT * FROM check_ins
        WHERE checked_in_at >= :start AND checked_in_at <= :end
        ORDER BY checked_in_at ASC
    """)
    suspend fun getCheckInsInDateRange(start: Long, end: Long): List<CheckInEntity>

    @Query("""
        SELECT * FROM check_ins
        WHERE checked_in_at >= :start AND checked_in_at <= :end
        ORDER BY checked_in_at ASC
    """)
    fun getCheckInsInDateRangeFlow(start: Long, end: Long): Flow<List<CheckInEntity>>

    /**
     * Check if the user has already completed a check-in today (not dismissed).
     * Used to enforce the once-per-day maximum.
     */
    @Query("""
        SELECT COUNT(*) FROM check_ins
        WHERE checked_in_at >= :dayStart
          AND checked_in_at <= :dayEnd
          AND dismissed = 0
          AND pain_score IS NOT NULL
    """)
    suspend fun getCompletedCheckInCountToday(dayStart: Long, dayEnd: Long): Int

    // ── Writes ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertCheckIn(checkIn: CheckInEntity)

    @Update
    suspend fun updateCheckIn(checkIn: CheckInEntity)

    @Query("DELETE FROM check_ins WHERE id = :id")
    suspend fun deleteCheckInById(id: String)
}
