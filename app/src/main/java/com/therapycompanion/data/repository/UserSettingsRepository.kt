package com.therapycompanion.data.repository

import com.therapycompanion.data.db.UserSettingsDao
import com.therapycompanion.data.db.toDomain
import com.therapycompanion.data.db.toEntity
import com.therapycompanion.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(private val dao: UserSettingsDao) {

    /**
     * Returns settings as a Flow — the UI automatically re-renders when settings change.
     * Emits UserSettings.Default if no row exists yet (first launch before seed).
     */
    fun getUserSettings(): Flow<UserSettings> =
        dao.getUserSettings().map { entity -> entity?.toDomain() ?: UserSettings.Default }

    suspend fun getUserSettingsOnce(): UserSettings =
        dao.getUserSettingsOnce()?.toDomain() ?: UserSettings.Default

    /** Inserts default settings row on first launch — no-op if row already exists. */
    suspend fun initializeDefaults() {
        dao.insertDefaultSettings(UserSettings.Default.toEntity())
    }

    suspend fun updateSettings(settings: UserSettings) =
        dao.updateSettings(settings.toEntity())

    suspend fun updateDailyLoad(load: Int) =
        dao.updateDailyLoad(load)

    suspend fun setEasierDayEnabled(enabled: Boolean) =
        dao.setEasierDayEnabled(enabled)

    suspend fun setDisplayName(name: String) =
        dao.setDisplayName(name)

    suspend fun setThemeMode(mode: String) =
        dao.setThemeMode(mode)
}
