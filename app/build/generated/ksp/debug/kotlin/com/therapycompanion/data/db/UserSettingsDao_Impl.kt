package com.therapycompanion.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class UserSettingsDao_Impl(
  __db: RoomDatabase,
) : UserSettingsDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfUserSettingsEntity: EntityInsertAdapter<UserSettingsEntity>

  private val __updateAdapterOfUserSettingsEntity: EntityDeleteOrUpdateAdapter<UserSettingsEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfUserSettingsEntity = object : EntityInsertAdapter<UserSettingsEntity>() {
      protected override fun createQuery(): String = "INSERT OR IGNORE INTO `user_settings` (`id`,`daily_load`,`easier_day_enabled`,`morning_reminder_enabled`,`morning_reminder_time`,`afternoon_check_in_enabled`,`afternoon_check_in_time`,`evening_encouragement_enabled`,`evening_encouragement_time`,`quiet_hours_start`,`quiet_hours_end`,`check_ins_enabled`,`show_streaks`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: UserSettingsEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.dailyLoad.toLong())
        val _tmp: Int = if (entity.easierDayEnabled) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        val _tmp_1: Int = if (entity.morningReminderEnabled) 1 else 0
        statement.bindLong(4, _tmp_1.toLong())
        statement.bindText(5, entity.morningReminderTime)
        val _tmp_2: Int = if (entity.afternoonCheckInEnabled) 1 else 0
        statement.bindLong(6, _tmp_2.toLong())
        statement.bindText(7, entity.afternoonCheckInTime)
        val _tmp_3: Int = if (entity.eveningEncouragementEnabled) 1 else 0
        statement.bindLong(8, _tmp_3.toLong())
        statement.bindText(9, entity.eveningEncouragementTime)
        val _tmpQuietHoursStart: String? = entity.quietHoursStart
        if (_tmpQuietHoursStart == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpQuietHoursStart)
        }
        val _tmpQuietHoursEnd: String? = entity.quietHoursEnd
        if (_tmpQuietHoursEnd == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpQuietHoursEnd)
        }
        val _tmp_4: Int = if (entity.checkInsEnabled) 1 else 0
        statement.bindLong(12, _tmp_4.toLong())
        val _tmp_5: Int = if (entity.showStreaks) 1 else 0
        statement.bindLong(13, _tmp_5.toLong())
      }
    }
    this.__updateAdapterOfUserSettingsEntity = object : EntityDeleteOrUpdateAdapter<UserSettingsEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `user_settings` SET `id` = ?,`daily_load` = ?,`easier_day_enabled` = ?,`morning_reminder_enabled` = ?,`morning_reminder_time` = ?,`afternoon_check_in_enabled` = ?,`afternoon_check_in_time` = ?,`evening_encouragement_enabled` = ?,`evening_encouragement_time` = ?,`quiet_hours_start` = ?,`quiet_hours_end` = ?,`check_ins_enabled` = ?,`show_streaks` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: UserSettingsEntity) {
        statement.bindLong(1, entity.id.toLong())
        statement.bindLong(2, entity.dailyLoad.toLong())
        val _tmp: Int = if (entity.easierDayEnabled) 1 else 0
        statement.bindLong(3, _tmp.toLong())
        val _tmp_1: Int = if (entity.morningReminderEnabled) 1 else 0
        statement.bindLong(4, _tmp_1.toLong())
        statement.bindText(5, entity.morningReminderTime)
        val _tmp_2: Int = if (entity.afternoonCheckInEnabled) 1 else 0
        statement.bindLong(6, _tmp_2.toLong())
        statement.bindText(7, entity.afternoonCheckInTime)
        val _tmp_3: Int = if (entity.eveningEncouragementEnabled) 1 else 0
        statement.bindLong(8, _tmp_3.toLong())
        statement.bindText(9, entity.eveningEncouragementTime)
        val _tmpQuietHoursStart: String? = entity.quietHoursStart
        if (_tmpQuietHoursStart == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpQuietHoursStart)
        }
        val _tmpQuietHoursEnd: String? = entity.quietHoursEnd
        if (_tmpQuietHoursEnd == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpQuietHoursEnd)
        }
        val _tmp_4: Int = if (entity.checkInsEnabled) 1 else 0
        statement.bindLong(12, _tmp_4.toLong())
        val _tmp_5: Int = if (entity.showStreaks) 1 else 0
        statement.bindLong(13, _tmp_5.toLong())
        statement.bindLong(14, entity.id.toLong())
      }
    }
  }

  public override suspend fun insertDefaultSettings(settings: UserSettingsEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfUserSettingsEntity.insert(_connection, settings)
  }

  public override suspend fun updateSettings(settings: UserSettingsEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfUserSettingsEntity.handle(_connection, settings)
  }

  public override fun getUserSettings(): Flow<UserSettingsEntity?> {
    val _sql: String = "SELECT * FROM user_settings WHERE id = 1"
    return createFlow(__db, false, arrayOf("user_settings")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDailyLoad: Int = getColumnIndexOrThrow(_stmt, "daily_load")
        val _columnIndexOfEasierDayEnabled: Int = getColumnIndexOrThrow(_stmt, "easier_day_enabled")
        val _columnIndexOfMorningReminderEnabled: Int = getColumnIndexOrThrow(_stmt, "morning_reminder_enabled")
        val _columnIndexOfMorningReminderTime: Int = getColumnIndexOrThrow(_stmt, "morning_reminder_time")
        val _columnIndexOfAfternoonCheckInEnabled: Int = getColumnIndexOrThrow(_stmt, "afternoon_check_in_enabled")
        val _columnIndexOfAfternoonCheckInTime: Int = getColumnIndexOrThrow(_stmt, "afternoon_check_in_time")
        val _columnIndexOfEveningEncouragementEnabled: Int = getColumnIndexOrThrow(_stmt, "evening_encouragement_enabled")
        val _columnIndexOfEveningEncouragementTime: Int = getColumnIndexOrThrow(_stmt, "evening_encouragement_time")
        val _columnIndexOfQuietHoursStart: Int = getColumnIndexOrThrow(_stmt, "quiet_hours_start")
        val _columnIndexOfQuietHoursEnd: Int = getColumnIndexOrThrow(_stmt, "quiet_hours_end")
        val _columnIndexOfCheckInsEnabled: Int = getColumnIndexOrThrow(_stmt, "check_ins_enabled")
        val _columnIndexOfShowStreaks: Int = getColumnIndexOrThrow(_stmt, "show_streaks")
        val _result: UserSettingsEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDailyLoad: Int
          _tmpDailyLoad = _stmt.getLong(_columnIndexOfDailyLoad).toInt()
          val _tmpEasierDayEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEasierDayEnabled).toInt()
          _tmpEasierDayEnabled = _tmp != 0
          val _tmpMorningReminderEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfMorningReminderEnabled).toInt()
          _tmpMorningReminderEnabled = _tmp_1 != 0
          val _tmpMorningReminderTime: String
          _tmpMorningReminderTime = _stmt.getText(_columnIndexOfMorningReminderTime)
          val _tmpAfternoonCheckInEnabled: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfAfternoonCheckInEnabled).toInt()
          _tmpAfternoonCheckInEnabled = _tmp_2 != 0
          val _tmpAfternoonCheckInTime: String
          _tmpAfternoonCheckInTime = _stmt.getText(_columnIndexOfAfternoonCheckInTime)
          val _tmpEveningEncouragementEnabled: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfEveningEncouragementEnabled).toInt()
          _tmpEveningEncouragementEnabled = _tmp_3 != 0
          val _tmpEveningEncouragementTime: String
          _tmpEveningEncouragementTime = _stmt.getText(_columnIndexOfEveningEncouragementTime)
          val _tmpQuietHoursStart: String?
          if (_stmt.isNull(_columnIndexOfQuietHoursStart)) {
            _tmpQuietHoursStart = null
          } else {
            _tmpQuietHoursStart = _stmt.getText(_columnIndexOfQuietHoursStart)
          }
          val _tmpQuietHoursEnd: String?
          if (_stmt.isNull(_columnIndexOfQuietHoursEnd)) {
            _tmpQuietHoursEnd = null
          } else {
            _tmpQuietHoursEnd = _stmt.getText(_columnIndexOfQuietHoursEnd)
          }
          val _tmpCheckInsEnabled: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfCheckInsEnabled).toInt()
          _tmpCheckInsEnabled = _tmp_4 != 0
          val _tmpShowStreaks: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfShowStreaks).toInt()
          _tmpShowStreaks = _tmp_5 != 0
          _result = UserSettingsEntity(_tmpId,_tmpDailyLoad,_tmpEasierDayEnabled,_tmpMorningReminderEnabled,_tmpMorningReminderTime,_tmpAfternoonCheckInEnabled,_tmpAfternoonCheckInTime,_tmpEveningEncouragementEnabled,_tmpEveningEncouragementTime,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpCheckInsEnabled,_tmpShowStreaks)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getUserSettingsOnce(): UserSettingsEntity? {
    val _sql: String = "SELECT * FROM user_settings WHERE id = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfDailyLoad: Int = getColumnIndexOrThrow(_stmt, "daily_load")
        val _columnIndexOfEasierDayEnabled: Int = getColumnIndexOrThrow(_stmt, "easier_day_enabled")
        val _columnIndexOfMorningReminderEnabled: Int = getColumnIndexOrThrow(_stmt, "morning_reminder_enabled")
        val _columnIndexOfMorningReminderTime: Int = getColumnIndexOrThrow(_stmt, "morning_reminder_time")
        val _columnIndexOfAfternoonCheckInEnabled: Int = getColumnIndexOrThrow(_stmt, "afternoon_check_in_enabled")
        val _columnIndexOfAfternoonCheckInTime: Int = getColumnIndexOrThrow(_stmt, "afternoon_check_in_time")
        val _columnIndexOfEveningEncouragementEnabled: Int = getColumnIndexOrThrow(_stmt, "evening_encouragement_enabled")
        val _columnIndexOfEveningEncouragementTime: Int = getColumnIndexOrThrow(_stmt, "evening_encouragement_time")
        val _columnIndexOfQuietHoursStart: Int = getColumnIndexOrThrow(_stmt, "quiet_hours_start")
        val _columnIndexOfQuietHoursEnd: Int = getColumnIndexOrThrow(_stmt, "quiet_hours_end")
        val _columnIndexOfCheckInsEnabled: Int = getColumnIndexOrThrow(_stmt, "check_ins_enabled")
        val _columnIndexOfShowStreaks: Int = getColumnIndexOrThrow(_stmt, "show_streaks")
        val _result: UserSettingsEntity?
        if (_stmt.step()) {
          val _tmpId: Int
          _tmpId = _stmt.getLong(_columnIndexOfId).toInt()
          val _tmpDailyLoad: Int
          _tmpDailyLoad = _stmt.getLong(_columnIndexOfDailyLoad).toInt()
          val _tmpEasierDayEnabled: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfEasierDayEnabled).toInt()
          _tmpEasierDayEnabled = _tmp != 0
          val _tmpMorningReminderEnabled: Boolean
          val _tmp_1: Int
          _tmp_1 = _stmt.getLong(_columnIndexOfMorningReminderEnabled).toInt()
          _tmpMorningReminderEnabled = _tmp_1 != 0
          val _tmpMorningReminderTime: String
          _tmpMorningReminderTime = _stmt.getText(_columnIndexOfMorningReminderTime)
          val _tmpAfternoonCheckInEnabled: Boolean
          val _tmp_2: Int
          _tmp_2 = _stmt.getLong(_columnIndexOfAfternoonCheckInEnabled).toInt()
          _tmpAfternoonCheckInEnabled = _tmp_2 != 0
          val _tmpAfternoonCheckInTime: String
          _tmpAfternoonCheckInTime = _stmt.getText(_columnIndexOfAfternoonCheckInTime)
          val _tmpEveningEncouragementEnabled: Boolean
          val _tmp_3: Int
          _tmp_3 = _stmt.getLong(_columnIndexOfEveningEncouragementEnabled).toInt()
          _tmpEveningEncouragementEnabled = _tmp_3 != 0
          val _tmpEveningEncouragementTime: String
          _tmpEveningEncouragementTime = _stmt.getText(_columnIndexOfEveningEncouragementTime)
          val _tmpQuietHoursStart: String?
          if (_stmt.isNull(_columnIndexOfQuietHoursStart)) {
            _tmpQuietHoursStart = null
          } else {
            _tmpQuietHoursStart = _stmt.getText(_columnIndexOfQuietHoursStart)
          }
          val _tmpQuietHoursEnd: String?
          if (_stmt.isNull(_columnIndexOfQuietHoursEnd)) {
            _tmpQuietHoursEnd = null
          } else {
            _tmpQuietHoursEnd = _stmt.getText(_columnIndexOfQuietHoursEnd)
          }
          val _tmpCheckInsEnabled: Boolean
          val _tmp_4: Int
          _tmp_4 = _stmt.getLong(_columnIndexOfCheckInsEnabled).toInt()
          _tmpCheckInsEnabled = _tmp_4 != 0
          val _tmpShowStreaks: Boolean
          val _tmp_5: Int
          _tmp_5 = _stmt.getLong(_columnIndexOfShowStreaks).toInt()
          _tmpShowStreaks = _tmp_5 != 0
          _result = UserSettingsEntity(_tmpId,_tmpDailyLoad,_tmpEasierDayEnabled,_tmpMorningReminderEnabled,_tmpMorningReminderTime,_tmpAfternoonCheckInEnabled,_tmpAfternoonCheckInTime,_tmpEveningEncouragementEnabled,_tmpEveningEncouragementTime,_tmpQuietHoursStart,_tmpQuietHoursEnd,_tmpCheckInsEnabled,_tmpShowStreaks)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateDailyLoad(load: Int) {
    val _sql: String = "UPDATE user_settings SET daily_load = ? WHERE id = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, load.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun setEasierDayEnabled(enabled: Boolean) {
    val _sql: String = "UPDATE user_settings SET easier_day_enabled = ? WHERE id = 1"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (enabled) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
