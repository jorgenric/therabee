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
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class CheckInDao_Impl(
  __db: RoomDatabase,
) : CheckInDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfCheckInEntity: EntityInsertAdapter<CheckInEntity>

  private val __updateAdapterOfCheckInEntity: EntityDeleteOrUpdateAdapter<CheckInEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfCheckInEntity = object : EntityInsertAdapter<CheckInEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `check_ins` (`id`,`checked_in_at`,`pain_score`,`energy_score`,`bpi_domain`,`bpi_score`,`free_text`,`dismissed`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CheckInEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.checkedInAt)
        val _tmpPainScore: Int? = entity.painScore
        if (_tmpPainScore == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpPainScore.toLong())
        }
        val _tmpEnergyScore: Int? = entity.energyScore
        if (_tmpEnergyScore == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEnergyScore.toLong())
        }
        val _tmpBpiDomain: String? = entity.bpiDomain
        if (_tmpBpiDomain == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpBpiDomain)
        }
        val _tmpBpiScore: Int? = entity.bpiScore
        if (_tmpBpiScore == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpBpiScore.toLong())
        }
        val _tmpFreeText: String? = entity.freeText
        if (_tmpFreeText == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpFreeText)
        }
        val _tmp: Int = if (entity.dismissed) 1 else 0
        statement.bindLong(8, _tmp.toLong())
      }
    }
    this.__updateAdapterOfCheckInEntity = object : EntityDeleteOrUpdateAdapter<CheckInEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `check_ins` SET `id` = ?,`checked_in_at` = ?,`pain_score` = ?,`energy_score` = ?,`bpi_domain` = ?,`bpi_score` = ?,`free_text` = ?,`dismissed` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: CheckInEntity) {
        statement.bindText(1, entity.id)
        statement.bindLong(2, entity.checkedInAt)
        val _tmpPainScore: Int? = entity.painScore
        if (_tmpPainScore == null) {
          statement.bindNull(3)
        } else {
          statement.bindLong(3, _tmpPainScore.toLong())
        }
        val _tmpEnergyScore: Int? = entity.energyScore
        if (_tmpEnergyScore == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpEnergyScore.toLong())
        }
        val _tmpBpiDomain: String? = entity.bpiDomain
        if (_tmpBpiDomain == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpBpiDomain)
        }
        val _tmpBpiScore: Int? = entity.bpiScore
        if (_tmpBpiScore == null) {
          statement.bindNull(6)
        } else {
          statement.bindLong(6, _tmpBpiScore.toLong())
        }
        val _tmpFreeText: String? = entity.freeText
        if (_tmpFreeText == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpFreeText)
        }
        val _tmp: Int = if (entity.dismissed) 1 else 0
        statement.bindLong(8, _tmp.toLong())
        statement.bindText(9, entity.id)
      }
    }
  }

  public override suspend fun insertCheckIn(checkIn: CheckInEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCheckInEntity.insert(_connection, checkIn)
  }

  public override suspend fun updateCheckIn(checkIn: CheckInEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfCheckInEntity.handle(_connection, checkIn)
  }

  public override fun getAllCheckIns(): Flow<List<CheckInEntity>> {
    val _sql: String = "SELECT * FROM check_ins ORDER BY checked_in_at DESC"
    return createFlow(__db, false, arrayOf("check_ins")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCheckedInAt: Int = getColumnIndexOrThrow(_stmt, "checked_in_at")
        val _columnIndexOfPainScore: Int = getColumnIndexOrThrow(_stmt, "pain_score")
        val _columnIndexOfEnergyScore: Int = getColumnIndexOrThrow(_stmt, "energy_score")
        val _columnIndexOfBpiDomain: Int = getColumnIndexOrThrow(_stmt, "bpi_domain")
        val _columnIndexOfBpiScore: Int = getColumnIndexOrThrow(_stmt, "bpi_score")
        val _columnIndexOfFreeText: Int = getColumnIndexOrThrow(_stmt, "free_text")
        val _columnIndexOfDismissed: Int = getColumnIndexOrThrow(_stmt, "dismissed")
        val _result: MutableList<CheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CheckInEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCheckedInAt: Long
          _tmpCheckedInAt = _stmt.getLong(_columnIndexOfCheckedInAt)
          val _tmpPainScore: Int?
          if (_stmt.isNull(_columnIndexOfPainScore)) {
            _tmpPainScore = null
          } else {
            _tmpPainScore = _stmt.getLong(_columnIndexOfPainScore).toInt()
          }
          val _tmpEnergyScore: Int?
          if (_stmt.isNull(_columnIndexOfEnergyScore)) {
            _tmpEnergyScore = null
          } else {
            _tmpEnergyScore = _stmt.getLong(_columnIndexOfEnergyScore).toInt()
          }
          val _tmpBpiDomain: String?
          if (_stmt.isNull(_columnIndexOfBpiDomain)) {
            _tmpBpiDomain = null
          } else {
            _tmpBpiDomain = _stmt.getText(_columnIndexOfBpiDomain)
          }
          val _tmpBpiScore: Int?
          if (_stmt.isNull(_columnIndexOfBpiScore)) {
            _tmpBpiScore = null
          } else {
            _tmpBpiScore = _stmt.getLong(_columnIndexOfBpiScore).toInt()
          }
          val _tmpFreeText: String?
          if (_stmt.isNull(_columnIndexOfFreeText)) {
            _tmpFreeText = null
          } else {
            _tmpFreeText = _stmt.getText(_columnIndexOfFreeText)
          }
          val _tmpDismissed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDismissed).toInt()
          _tmpDismissed = _tmp != 0
          _item = CheckInEntity(_tmpId,_tmpCheckedInAt,_tmpPainScore,_tmpEnergyScore,_tmpBpiDomain,_tmpBpiScore,_tmpFreeText,_tmpDismissed)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllCheckInsOnce(): List<CheckInEntity> {
    val _sql: String = "SELECT * FROM check_ins ORDER BY checked_in_at DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCheckedInAt: Int = getColumnIndexOrThrow(_stmt, "checked_in_at")
        val _columnIndexOfPainScore: Int = getColumnIndexOrThrow(_stmt, "pain_score")
        val _columnIndexOfEnergyScore: Int = getColumnIndexOrThrow(_stmt, "energy_score")
        val _columnIndexOfBpiDomain: Int = getColumnIndexOrThrow(_stmt, "bpi_domain")
        val _columnIndexOfBpiScore: Int = getColumnIndexOrThrow(_stmt, "bpi_score")
        val _columnIndexOfFreeText: Int = getColumnIndexOrThrow(_stmt, "free_text")
        val _columnIndexOfDismissed: Int = getColumnIndexOrThrow(_stmt, "dismissed")
        val _result: MutableList<CheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CheckInEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCheckedInAt: Long
          _tmpCheckedInAt = _stmt.getLong(_columnIndexOfCheckedInAt)
          val _tmpPainScore: Int?
          if (_stmt.isNull(_columnIndexOfPainScore)) {
            _tmpPainScore = null
          } else {
            _tmpPainScore = _stmt.getLong(_columnIndexOfPainScore).toInt()
          }
          val _tmpEnergyScore: Int?
          if (_stmt.isNull(_columnIndexOfEnergyScore)) {
            _tmpEnergyScore = null
          } else {
            _tmpEnergyScore = _stmt.getLong(_columnIndexOfEnergyScore).toInt()
          }
          val _tmpBpiDomain: String?
          if (_stmt.isNull(_columnIndexOfBpiDomain)) {
            _tmpBpiDomain = null
          } else {
            _tmpBpiDomain = _stmt.getText(_columnIndexOfBpiDomain)
          }
          val _tmpBpiScore: Int?
          if (_stmt.isNull(_columnIndexOfBpiScore)) {
            _tmpBpiScore = null
          } else {
            _tmpBpiScore = _stmt.getLong(_columnIndexOfBpiScore).toInt()
          }
          val _tmpFreeText: String?
          if (_stmt.isNull(_columnIndexOfFreeText)) {
            _tmpFreeText = null
          } else {
            _tmpFreeText = _stmt.getText(_columnIndexOfFreeText)
          }
          val _tmpDismissed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDismissed).toInt()
          _tmpDismissed = _tmp != 0
          _item = CheckInEntity(_tmpId,_tmpCheckedInAt,_tmpPainScore,_tmpEnergyScore,_tmpBpiDomain,_tmpBpiScore,_tmpFreeText,_tmpDismissed)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCheckInById(id: String): CheckInEntity? {
    val _sql: String = "SELECT * FROM check_ins WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCheckedInAt: Int = getColumnIndexOrThrow(_stmt, "checked_in_at")
        val _columnIndexOfPainScore: Int = getColumnIndexOrThrow(_stmt, "pain_score")
        val _columnIndexOfEnergyScore: Int = getColumnIndexOrThrow(_stmt, "energy_score")
        val _columnIndexOfBpiDomain: Int = getColumnIndexOrThrow(_stmt, "bpi_domain")
        val _columnIndexOfBpiScore: Int = getColumnIndexOrThrow(_stmt, "bpi_score")
        val _columnIndexOfFreeText: Int = getColumnIndexOrThrow(_stmt, "free_text")
        val _columnIndexOfDismissed: Int = getColumnIndexOrThrow(_stmt, "dismissed")
        val _result: CheckInEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCheckedInAt: Long
          _tmpCheckedInAt = _stmt.getLong(_columnIndexOfCheckedInAt)
          val _tmpPainScore: Int?
          if (_stmt.isNull(_columnIndexOfPainScore)) {
            _tmpPainScore = null
          } else {
            _tmpPainScore = _stmt.getLong(_columnIndexOfPainScore).toInt()
          }
          val _tmpEnergyScore: Int?
          if (_stmt.isNull(_columnIndexOfEnergyScore)) {
            _tmpEnergyScore = null
          } else {
            _tmpEnergyScore = _stmt.getLong(_columnIndexOfEnergyScore).toInt()
          }
          val _tmpBpiDomain: String?
          if (_stmt.isNull(_columnIndexOfBpiDomain)) {
            _tmpBpiDomain = null
          } else {
            _tmpBpiDomain = _stmt.getText(_columnIndexOfBpiDomain)
          }
          val _tmpBpiScore: Int?
          if (_stmt.isNull(_columnIndexOfBpiScore)) {
            _tmpBpiScore = null
          } else {
            _tmpBpiScore = _stmt.getLong(_columnIndexOfBpiScore).toInt()
          }
          val _tmpFreeText: String?
          if (_stmt.isNull(_columnIndexOfFreeText)) {
            _tmpFreeText = null
          } else {
            _tmpFreeText = _stmt.getText(_columnIndexOfFreeText)
          }
          val _tmpDismissed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDismissed).toInt()
          _tmpDismissed = _tmp != 0
          _result = CheckInEntity(_tmpId,_tmpCheckedInAt,_tmpPainScore,_tmpEnergyScore,_tmpBpiDomain,_tmpBpiScore,_tmpFreeText,_tmpDismissed)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCheckInsInDateRange(start: Long, end: Long): List<CheckInEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM check_ins
        |        WHERE checked_in_at >= ? AND checked_in_at <= ?
        |        ORDER BY checked_in_at ASC
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, start)
        _argIndex = 2
        _stmt.bindLong(_argIndex, end)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCheckedInAt: Int = getColumnIndexOrThrow(_stmt, "checked_in_at")
        val _columnIndexOfPainScore: Int = getColumnIndexOrThrow(_stmt, "pain_score")
        val _columnIndexOfEnergyScore: Int = getColumnIndexOrThrow(_stmt, "energy_score")
        val _columnIndexOfBpiDomain: Int = getColumnIndexOrThrow(_stmt, "bpi_domain")
        val _columnIndexOfBpiScore: Int = getColumnIndexOrThrow(_stmt, "bpi_score")
        val _columnIndexOfFreeText: Int = getColumnIndexOrThrow(_stmt, "free_text")
        val _columnIndexOfDismissed: Int = getColumnIndexOrThrow(_stmt, "dismissed")
        val _result: MutableList<CheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CheckInEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCheckedInAt: Long
          _tmpCheckedInAt = _stmt.getLong(_columnIndexOfCheckedInAt)
          val _tmpPainScore: Int?
          if (_stmt.isNull(_columnIndexOfPainScore)) {
            _tmpPainScore = null
          } else {
            _tmpPainScore = _stmt.getLong(_columnIndexOfPainScore).toInt()
          }
          val _tmpEnergyScore: Int?
          if (_stmt.isNull(_columnIndexOfEnergyScore)) {
            _tmpEnergyScore = null
          } else {
            _tmpEnergyScore = _stmt.getLong(_columnIndexOfEnergyScore).toInt()
          }
          val _tmpBpiDomain: String?
          if (_stmt.isNull(_columnIndexOfBpiDomain)) {
            _tmpBpiDomain = null
          } else {
            _tmpBpiDomain = _stmt.getText(_columnIndexOfBpiDomain)
          }
          val _tmpBpiScore: Int?
          if (_stmt.isNull(_columnIndexOfBpiScore)) {
            _tmpBpiScore = null
          } else {
            _tmpBpiScore = _stmt.getLong(_columnIndexOfBpiScore).toInt()
          }
          val _tmpFreeText: String?
          if (_stmt.isNull(_columnIndexOfFreeText)) {
            _tmpFreeText = null
          } else {
            _tmpFreeText = _stmt.getText(_columnIndexOfFreeText)
          }
          val _tmpDismissed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDismissed).toInt()
          _tmpDismissed = _tmp != 0
          _item = CheckInEntity(_tmpId,_tmpCheckedInAt,_tmpPainScore,_tmpEnergyScore,_tmpBpiDomain,_tmpBpiScore,_tmpFreeText,_tmpDismissed)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getCheckInsInDateRangeFlow(start: Long, end: Long): Flow<List<CheckInEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM check_ins
        |        WHERE checked_in_at >= ? AND checked_in_at <= ?
        |        ORDER BY checked_in_at ASC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("check_ins")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, start)
        _argIndex = 2
        _stmt.bindLong(_argIndex, end)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfCheckedInAt: Int = getColumnIndexOrThrow(_stmt, "checked_in_at")
        val _columnIndexOfPainScore: Int = getColumnIndexOrThrow(_stmt, "pain_score")
        val _columnIndexOfEnergyScore: Int = getColumnIndexOrThrow(_stmt, "energy_score")
        val _columnIndexOfBpiDomain: Int = getColumnIndexOrThrow(_stmt, "bpi_domain")
        val _columnIndexOfBpiScore: Int = getColumnIndexOrThrow(_stmt, "bpi_score")
        val _columnIndexOfFreeText: Int = getColumnIndexOrThrow(_stmt, "free_text")
        val _columnIndexOfDismissed: Int = getColumnIndexOrThrow(_stmt, "dismissed")
        val _result: MutableList<CheckInEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CheckInEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpCheckedInAt: Long
          _tmpCheckedInAt = _stmt.getLong(_columnIndexOfCheckedInAt)
          val _tmpPainScore: Int?
          if (_stmt.isNull(_columnIndexOfPainScore)) {
            _tmpPainScore = null
          } else {
            _tmpPainScore = _stmt.getLong(_columnIndexOfPainScore).toInt()
          }
          val _tmpEnergyScore: Int?
          if (_stmt.isNull(_columnIndexOfEnergyScore)) {
            _tmpEnergyScore = null
          } else {
            _tmpEnergyScore = _stmt.getLong(_columnIndexOfEnergyScore).toInt()
          }
          val _tmpBpiDomain: String?
          if (_stmt.isNull(_columnIndexOfBpiDomain)) {
            _tmpBpiDomain = null
          } else {
            _tmpBpiDomain = _stmt.getText(_columnIndexOfBpiDomain)
          }
          val _tmpBpiScore: Int?
          if (_stmt.isNull(_columnIndexOfBpiScore)) {
            _tmpBpiScore = null
          } else {
            _tmpBpiScore = _stmt.getLong(_columnIndexOfBpiScore).toInt()
          }
          val _tmpFreeText: String?
          if (_stmt.isNull(_columnIndexOfFreeText)) {
            _tmpFreeText = null
          } else {
            _tmpFreeText = _stmt.getText(_columnIndexOfFreeText)
          }
          val _tmpDismissed: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfDismissed).toInt()
          _tmpDismissed = _tmp != 0
          _item = CheckInEntity(_tmpId,_tmpCheckedInAt,_tmpPainScore,_tmpEnergyScore,_tmpBpiDomain,_tmpBpiScore,_tmpFreeText,_tmpDismissed)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCompletedCheckInCountToday(dayStart: Long, dayEnd: Long): Int {
    val _sql: String = """
        |
        |        SELECT COUNT(*) FROM check_ins
        |        WHERE checked_in_at >= ?
        |          AND checked_in_at <= ?
        |          AND dismissed = 0
        |          AND pain_score IS NOT NULL
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, dayStart)
        _argIndex = 2
        _stmt.bindLong(_argIndex, dayEnd)
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCheckInById(id: String) {
    val _sql: String = "DELETE FROM check_ins WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
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
