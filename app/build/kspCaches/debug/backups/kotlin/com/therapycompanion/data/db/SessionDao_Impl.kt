package com.therapycompanion.`data`.db

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
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
public class SessionDao_Impl(
  __db: RoomDatabase,
) : SessionDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSessionEntity: EntityInsertAdapter<SessionEntity>

  private val __updateAdapterOfSessionEntity: EntityDeleteOrUpdateAdapter<SessionEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSessionEntity = object : EntityInsertAdapter<SessionEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `sessions` (`id`,`exercise_id`,`started_at`,`completed_at`,`elapsed_seconds`,`status`,`notes`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindLong(3, entity.startedAt)
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpCompletedAt)
        }
        statement.bindLong(5, entity.elapsedSeconds)
        statement.bindText(6, entity.status)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNotes)
        }
      }
    }
    this.__updateAdapterOfSessionEntity = object : EntityDeleteOrUpdateAdapter<SessionEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `sessions` SET `id` = ?,`exercise_id` = ?,`started_at` = ?,`completed_at` = ?,`elapsed_seconds` = ?,`status` = ?,`notes` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: SessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.exerciseId)
        statement.bindLong(3, entity.startedAt)
        val _tmpCompletedAt: Long? = entity.completedAt
        if (_tmpCompletedAt == null) {
          statement.bindNull(4)
        } else {
          statement.bindLong(4, _tmpCompletedAt)
        }
        statement.bindLong(5, entity.elapsedSeconds)
        statement.bindText(6, entity.status)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpNotes)
        }
        statement.bindText(8, entity.id)
      }
    }
  }

  public override suspend fun insertSession(session: SessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSessionEntity.insert(_connection, session)
  }

  public override suspend fun updateSession(session: SessionEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfSessionEntity.handle(_connection, session)
  }

  public override fun getAllSessions(): Flow<List<SessionEntity>> {
    val _sql: String = "SELECT * FROM sessions ORDER BY started_at DESC"
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllSessionsOnce(): List<SessionEntity> {
    val _sql: String = "SELECT * FROM sessions ORDER BY started_at DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSessionById(id: String): SessionEntity? {
    val _sql: String = "SELECT * FROM sessions WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getSessionsInDateRange(start: Long, end: Long): List<SessionEntity> {
    val _sql: String = """
        |
        |        SELECT * FROM sessions
        |        WHERE started_at >= ? AND started_at <= ?
        |        ORDER BY started_at DESC
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
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSessionsInDateRangeFlow(start: Long, end: Long): Flow<List<SessionEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM sessions
        |        WHERE started_at >= ? AND started_at <= ?
        |        ORDER BY started_at DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, start)
        _argIndex = 2
        _stmt.bindLong(_argIndex, end)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getLastSessionForExercise(exerciseId: String): SessionEntity? {
    val _sql: String = """
        |
        |        SELECT * FROM sessions
        |        WHERE exercise_id = ?
        |        ORDER BY started_at DESC
        |        LIMIT 1
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: SessionEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _result = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCompletedSessionCountByExerciseInWeek(exerciseId: String, weekStart: Long): Int {
    val _sql: String = """
        |
        |        SELECT COUNT(*) FROM sessions
        |        WHERE exercise_id = ?
        |          AND status = 'Completed'
        |          AND started_at >= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, weekStart)
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

  public override suspend fun getCompletedSessionCountForExerciseToday(
    exerciseId: String,
    dayStart: Long,
    dayEnd: Long,
  ): Int {
    val _sql: String = """
        |
        |        SELECT COUNT(*) FROM sessions
        |        WHERE exercise_id = ?
        |          AND status = 'Completed'
        |          AND started_at >= ?
        |          AND started_at <= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, dayStart)
        _argIndex = 3
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

  public override fun getSessionsForExercise(exerciseId: String): Flow<List<SessionEntity>> {
    val _sql: String = """
        |
        |        SELECT * FROM sessions
        |        WHERE exercise_id = ?
        |        ORDER BY started_at DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("sessions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, exerciseId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfExerciseId: Int = getColumnIndexOrThrow(_stmt, "exercise_id")
        val _columnIndexOfStartedAt: Int = getColumnIndexOrThrow(_stmt, "started_at")
        val _columnIndexOfCompletedAt: Int = getColumnIndexOrThrow(_stmt, "completed_at")
        val _columnIndexOfElapsedSeconds: Int = getColumnIndexOrThrow(_stmt, "elapsed_seconds")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _result: MutableList<SessionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SessionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpExerciseId: String
          _tmpExerciseId = _stmt.getText(_columnIndexOfExerciseId)
          val _tmpStartedAt: Long
          _tmpStartedAt = _stmt.getLong(_columnIndexOfStartedAt)
          val _tmpCompletedAt: Long?
          if (_stmt.isNull(_columnIndexOfCompletedAt)) {
            _tmpCompletedAt = null
          } else {
            _tmpCompletedAt = _stmt.getLong(_columnIndexOfCompletedAt)
          }
          val _tmpElapsedSeconds: Long
          _tmpElapsedSeconds = _stmt.getLong(_columnIndexOfElapsedSeconds)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          _item = SessionEntity(_tmpId,_tmpExerciseId,_tmpStartedAt,_tmpCompletedAt,_tmpElapsedSeconds,_tmpStatus,_tmpNotes)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getCompletedBodySystemsSince(since: Long): List<String> {
    val _sql: String = """
        |
        |        SELECT DISTINCT e.body_system
        |        FROM sessions s
        |        INNER JOIN exercises e ON s.exercise_id = e.id
        |        WHERE s.status = 'Completed'
        |          AND s.started_at >= ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, since)
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSessionById(id: String) {
    val _sql: String = "DELETE FROM sessions WHERE id = ?"
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
