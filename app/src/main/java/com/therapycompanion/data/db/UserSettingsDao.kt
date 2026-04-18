package com.therapycompanion.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {

    /**
     * Returns as Flow so the UI reacts to settings changes instantly.
     * The table always has exactly one row (id = 1).
     */
    @Query("SELECT * FROM user_settings WHERE id = 1")
    fun getUserSettings(): Flow<UserSettingsEntity?>

    @Query("SELECT * FROM user_settings WHERE id = 1")
    suspend fun getUserSettingsOnce(): UserSettingsEntity?

    /**
     * Inserts default settings on first launch.
     * Uses IGNORE strategy so subsequent calls are no-ops if the row already exists.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefaultSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    /**
     * Convenience: update daily load only.
     */
    @Query("UPDATE user_settings SET daily_load = :load WHERE id = 1")
    suspend fun updateDailyLoad(load: Int)

    /**
     * Convenience: toggle easier day.
     */
    @Query("UPDATE user_settings SET easier_day_enabled = :enabled WHERE id = 1")
    suspend fun setEasierDayEnabled(enabled: Boolean)
}
