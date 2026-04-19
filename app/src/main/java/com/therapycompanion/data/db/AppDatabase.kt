package com.therapycompanion.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room database — version 1.
 *
 * MIGRATION POLICY: Never use fallbackToDestructiveMigration() in production.
 * Add an explicit Migration object for every schema change. The exercise library
 * may be populated with dozens of exercises before development is complete;
 * destructive migration would force full re-entry of user data.
 *
 * Future migrations are added as:
 *   .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
 */
@Database(
    entities = [
        ExerciseEntity::class,
        SessionEntity::class,
        CheckInEntity::class,
        UserSettingsEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun exerciseDao(): ExerciseDao
    abstract fun sessionDao(): SessionDao
    abstract fun checkInDao(): CheckInDao
    abstract fun userSettingsDao(): UserSettingsDao

    companion object {
        private const val DATABASE_NAME = "therapy_companion.db"

        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }

        /**
         * Version 1 → 2: body_system column was already stored as TEXT in SQLite;
         * no structural SQL change is needed. This migration records the removal of
         * the BodySystem enum constraint so Room's schema version is consistent.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Column type was already TEXT NOT NULL — no SQL needed.
            }
        }

        /**
         * Version 2 → 3: adds show_streaks to user_settings.
         * DEFAULT 0 ensures the existing row is set to false (off by default).
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE user_settings ADD COLUMN show_streaks INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        private fun buildDatabase(context: Context): AppDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }
}
