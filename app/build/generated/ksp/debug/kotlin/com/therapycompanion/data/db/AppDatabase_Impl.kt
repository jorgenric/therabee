package com.therapycompanion.`data`.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _exerciseDao: Lazy<ExerciseDao> = lazy {
    ExerciseDao_Impl(this)
  }

  private val _sessionDao: Lazy<SessionDao> = lazy {
    SessionDao_Impl(this)
  }

  private val _checkInDao: Lazy<CheckInDao> = lazy {
    CheckInDao_Impl(this)
  }

  private val _userSettingsDao: Lazy<UserSettingsDao> = lazy {
    UserSettingsDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(3, "bcdd76653c9afdf5ac15b55d29999bb2", "d0aec61519b58f9c4be1d9f1a484bd59") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `exercises` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `body_system` TEXT NOT NULL, `instructions` TEXT NOT NULL, `notes` TEXT, `duration_minutes` INTEGER NOT NULL, `frequency` TEXT NOT NULL, `scheduled_days` INTEGER NOT NULL, `priority` INTEGER NOT NULL, `active` INTEGER NOT NULL, `image_file_name` TEXT, `video_file_name` TEXT, `created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `sessions` (`id` TEXT NOT NULL, `exercise_id` TEXT NOT NULL, `started_at` INTEGER NOT NULL, `completed_at` INTEGER, `elapsed_seconds` INTEGER NOT NULL, `status` TEXT NOT NULL, `notes` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`exercise_id`) REFERENCES `exercises`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_exercise_id` ON `sessions` (`exercise_id`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_started_at` ON `sessions` (`started_at`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_sessions_status` ON `sessions` (`status`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `check_ins` (`id` TEXT NOT NULL, `checked_in_at` INTEGER NOT NULL, `pain_score` INTEGER, `energy_score` INTEGER, `bpi_domain` TEXT, `bpi_score` INTEGER, `free_text` TEXT, `dismissed` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_check_ins_checked_in_at` ON `check_ins` (`checked_in_at`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `user_settings` (`id` INTEGER NOT NULL, `daily_load` INTEGER NOT NULL, `easier_day_enabled` INTEGER NOT NULL, `morning_reminder_enabled` INTEGER NOT NULL, `morning_reminder_time` TEXT NOT NULL, `afternoon_check_in_enabled` INTEGER NOT NULL, `afternoon_check_in_time` TEXT NOT NULL, `evening_encouragement_enabled` INTEGER NOT NULL, `evening_encouragement_time` TEXT NOT NULL, `quiet_hours_start` TEXT, `quiet_hours_end` TEXT, `check_ins_enabled` INTEGER NOT NULL, `show_streaks` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'bcdd76653c9afdf5ac15b55d29999bb2')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `exercises`")
        connection.execSQL("DROP TABLE IF EXISTS `sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `check_ins`")
        connection.execSQL("DROP TABLE IF EXISTS `user_settings`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        connection.execSQL("PRAGMA foreign_keys = ON")
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsExercises: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsExercises.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("name", TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("body_system", TableInfo.Column("body_system", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("instructions", TableInfo.Column("instructions", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("duration_minutes", TableInfo.Column("duration_minutes", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("frequency", TableInfo.Column("frequency", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("scheduled_days", TableInfo.Column("scheduled_days", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("priority", TableInfo.Column("priority", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("active", TableInfo.Column("active", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("image_file_name", TableInfo.Column("image_file_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("video_file_name", TableInfo.Column("video_file_name", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("created_at", TableInfo.Column("created_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsExercises.put("updated_at", TableInfo.Column("updated_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysExercises: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesExercises: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoExercises: TableInfo = TableInfo("exercises", _columnsExercises, _foreignKeysExercises, _indicesExercises)
        val _existingExercises: TableInfo = read(connection, "exercises")
        if (!_infoExercises.equals(_existingExercises)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |exercises(com.therapycompanion.data.db.ExerciseEntity).
              | Expected:
              |""".trimMargin() + _infoExercises + """
              |
              | Found:
              |""".trimMargin() + _existingExercises)
        }
        val _columnsSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("exercise_id", TableInfo.Column("exercise_id", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("started_at", TableInfo.Column("started_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("completed_at", TableInfo.Column("completed_at", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("elapsed_seconds", TableInfo.Column("elapsed_seconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("status", TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSessions.put("notes", TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        _foreignKeysSessions.add(TableInfo.ForeignKey("exercises", "CASCADE", "NO ACTION", listOf("exercise_id"), listOf("id")))
        val _indicesSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesSessions.add(TableInfo.Index("index_sessions_exercise_id", false, listOf("exercise_id"), listOf("ASC")))
        _indicesSessions.add(TableInfo.Index("index_sessions_started_at", false, listOf("started_at"), listOf("ASC")))
        _indicesSessions.add(TableInfo.Index("index_sessions_status", false, listOf("status"), listOf("ASC")))
        val _infoSessions: TableInfo = TableInfo("sessions", _columnsSessions, _foreignKeysSessions, _indicesSessions)
        val _existingSessions: TableInfo = read(connection, "sessions")
        if (!_infoSessions.equals(_existingSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |sessions(com.therapycompanion.data.db.SessionEntity).
              | Expected:
              |""".trimMargin() + _infoSessions + """
              |
              | Found:
              |""".trimMargin() + _existingSessions)
        }
        val _columnsCheckIns: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCheckIns.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("checked_in_at", TableInfo.Column("checked_in_at", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("pain_score", TableInfo.Column("pain_score", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("energy_score", TableInfo.Column("energy_score", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("bpi_domain", TableInfo.Column("bpi_domain", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("bpi_score", TableInfo.Column("bpi_score", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("free_text", TableInfo.Column("free_text", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCheckIns.put("dismissed", TableInfo.Column("dismissed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCheckIns: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCheckIns: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCheckIns.add(TableInfo.Index("index_check_ins_checked_in_at", false, listOf("checked_in_at"), listOf("ASC")))
        val _infoCheckIns: TableInfo = TableInfo("check_ins", _columnsCheckIns, _foreignKeysCheckIns, _indicesCheckIns)
        val _existingCheckIns: TableInfo = read(connection, "check_ins")
        if (!_infoCheckIns.equals(_existingCheckIns)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |check_ins(com.therapycompanion.data.db.CheckInEntity).
              | Expected:
              |""".trimMargin() + _infoCheckIns + """
              |
              | Found:
              |""".trimMargin() + _existingCheckIns)
        }
        val _columnsUserSettings: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsUserSettings.put("id", TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("daily_load", TableInfo.Column("daily_load", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("easier_day_enabled", TableInfo.Column("easier_day_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("morning_reminder_enabled", TableInfo.Column("morning_reminder_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("morning_reminder_time", TableInfo.Column("morning_reminder_time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("afternoon_check_in_enabled", TableInfo.Column("afternoon_check_in_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("afternoon_check_in_time", TableInfo.Column("afternoon_check_in_time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("evening_encouragement_enabled", TableInfo.Column("evening_encouragement_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("evening_encouragement_time", TableInfo.Column("evening_encouragement_time", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("quiet_hours_start", TableInfo.Column("quiet_hours_start", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("quiet_hours_end", TableInfo.Column("quiet_hours_end", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("check_ins_enabled", TableInfo.Column("check_ins_enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsUserSettings.put("show_streaks", TableInfo.Column("show_streaks", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysUserSettings: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesUserSettings: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoUserSettings: TableInfo = TableInfo("user_settings", _columnsUserSettings, _foreignKeysUserSettings, _indicesUserSettings)
        val _existingUserSettings: TableInfo = read(connection, "user_settings")
        if (!_infoUserSettings.equals(_existingUserSettings)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |user_settings(com.therapycompanion.data.db.UserSettingsEntity).
              | Expected:
              |""".trimMargin() + _infoUserSettings + """
              |
              | Found:
              |""".trimMargin() + _existingUserSettings)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "exercises", "sessions", "check_ins", "user_settings")
  }

  public override fun clearAllTables() {
    super.performClear(true, "exercises", "sessions", "check_ins", "user_settings")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ExerciseDao::class, ExerciseDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SessionDao::class, SessionDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(CheckInDao::class, CheckInDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(UserSettingsDao::class, UserSettingsDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun exerciseDao(): ExerciseDao = _exerciseDao.value

  public override fun sessionDao(): SessionDao = _sessionDao.value

  public override fun checkInDao(): CheckInDao = _checkInDao.value

  public override fun userSettingsDao(): UserSettingsDao = _userSettingsDao.value
}
