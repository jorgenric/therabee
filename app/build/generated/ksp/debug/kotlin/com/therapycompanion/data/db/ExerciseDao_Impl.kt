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
public class ExerciseDao_Impl(
  __db: RoomDatabase,
) : ExerciseDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfExerciseEntity: EntityInsertAdapter<ExerciseEntity>

  private val __insertAdapterOfExerciseEntity_1: EntityInsertAdapter<ExerciseEntity>

  private val __deleteAdapterOfExerciseEntity: EntityDeleteOrUpdateAdapter<ExerciseEntity>

  private val __updateAdapterOfExerciseEntity: EntityDeleteOrUpdateAdapter<ExerciseEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfExerciseEntity = object : EntityInsertAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR ABORT INTO `exercises` (`id`,`name`,`body_system`,`instructions`,`notes`,`duration_minutes`,`frequency`,`scheduled_days`,`priority`,`active`,`image_file_name`,`video_file_name`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.bodySystem)
        statement.bindText(4, entity.instructions)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNotes)
        }
        statement.bindLong(6, entity.durationMinutes.toLong())
        statement.bindText(7, entity.frequency)
        statement.bindLong(8, entity.scheduledDays.toLong())
        statement.bindLong(9, entity.priority.toLong())
        val _tmp: Int = if (entity.active) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        val _tmpImageFileName: String? = entity.imageFileName
        if (_tmpImageFileName == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpImageFileName)
        }
        val _tmpVideoFileName: String? = entity.videoFileName
        if (_tmpVideoFileName == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpVideoFileName)
        }
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.updatedAt)
      }
    }
    this.__insertAdapterOfExerciseEntity_1 = object : EntityInsertAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `exercises` (`id`,`name`,`body_system`,`instructions`,`notes`,`duration_minutes`,`frequency`,`scheduled_days`,`priority`,`active`,`image_file_name`,`video_file_name`,`created_at`,`updated_at`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.bodySystem)
        statement.bindText(4, entity.instructions)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNotes)
        }
        statement.bindLong(6, entity.durationMinutes.toLong())
        statement.bindText(7, entity.frequency)
        statement.bindLong(8, entity.scheduledDays.toLong())
        statement.bindLong(9, entity.priority.toLong())
        val _tmp: Int = if (entity.active) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        val _tmpImageFileName: String? = entity.imageFileName
        if (_tmpImageFileName == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpImageFileName)
        }
        val _tmpVideoFileName: String? = entity.videoFileName
        if (_tmpVideoFileName == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpVideoFileName)
        }
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.updatedAt)
      }
    }
    this.__deleteAdapterOfExerciseEntity = object : EntityDeleteOrUpdateAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "DELETE FROM `exercises` WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindText(1, entity.id)
      }
    }
    this.__updateAdapterOfExerciseEntity = object : EntityDeleteOrUpdateAdapter<ExerciseEntity>() {
      protected override fun createQuery(): String = "UPDATE OR ABORT `exercises` SET `id` = ?,`name` = ?,`body_system` = ?,`instructions` = ?,`notes` = ?,`duration_minutes` = ?,`frequency` = ?,`scheduled_days` = ?,`priority` = ?,`active` = ?,`image_file_name` = ?,`video_file_name` = ?,`created_at` = ?,`updated_at` = ? WHERE `id` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ExerciseEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.name)
        statement.bindText(3, entity.bodySystem)
        statement.bindText(4, entity.instructions)
        val _tmpNotes: String? = entity.notes
        if (_tmpNotes == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpNotes)
        }
        statement.bindLong(6, entity.durationMinutes.toLong())
        statement.bindText(7, entity.frequency)
        statement.bindLong(8, entity.scheduledDays.toLong())
        statement.bindLong(9, entity.priority.toLong())
        val _tmp: Int = if (entity.active) 1 else 0
        statement.bindLong(10, _tmp.toLong())
        val _tmpImageFileName: String? = entity.imageFileName
        if (_tmpImageFileName == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpImageFileName)
        }
        val _tmpVideoFileName: String? = entity.videoFileName
        if (_tmpVideoFileName == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpVideoFileName)
        }
        statement.bindLong(13, entity.createdAt)
        statement.bindLong(14, entity.updatedAt)
        statement.bindText(15, entity.id)
      }
    }
  }

  public override suspend fun insertExercise(exercise: ExerciseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseEntity.insert(_connection, exercise)
  }

  public override suspend fun insertExercises(exercises: List<ExerciseEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseEntity.insert(_connection, exercises)
  }

  public override suspend fun upsertExercise(exercise: ExerciseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfExerciseEntity_1.insert(_connection, exercise)
  }

  public override suspend fun deleteExercise(exercise: ExerciseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __deleteAdapterOfExerciseEntity.handle(_connection, exercise)
  }

  public override suspend fun updateExercise(exercise: ExerciseEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __updateAdapterOfExerciseEntity.handle(_connection, exercise)
  }

  public override fun getAllExercises(): Flow<List<ExerciseEntity>> {
    val _sql: String = "SELECT * FROM exercises ORDER BY priority ASC, name ASC"
    return createFlow(__db, false, arrayOf("exercises")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getActiveExercises(): Flow<List<ExerciseEntity>> {
    val _sql: String = "SELECT * FROM exercises WHERE active = 1 ORDER BY priority ASC, name ASC"
    return createFlow(__db, false, arrayOf("exercises")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExerciseById(id: String): ExerciseEntity? {
    val _sql: String = "SELECT * FROM exercises WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: ExerciseEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExercisesForDay(dayBit: Int): List<ExerciseEntity> {
    val _sql: String = "SELECT * FROM exercises WHERE (scheduled_days & ?) != 0 AND active = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, dayBit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExercisesByBodySystem(bodySystem: String): List<ExerciseEntity> {
    val _sql: String = "SELECT * FROM exercises WHERE body_system = ? AND active = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, bodySystem)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: MutableList<ExerciseEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ExerciseEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getExerciseByName(name: String): ExerciseEntity? {
    val _sql: String = "SELECT * FROM exercises WHERE name = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfName: Int = getColumnIndexOrThrow(_stmt, "name")
        val _columnIndexOfBodySystem: Int = getColumnIndexOrThrow(_stmt, "body_system")
        val _columnIndexOfInstructions: Int = getColumnIndexOrThrow(_stmt, "instructions")
        val _columnIndexOfNotes: Int = getColumnIndexOrThrow(_stmt, "notes")
        val _columnIndexOfDurationMinutes: Int = getColumnIndexOrThrow(_stmt, "duration_minutes")
        val _columnIndexOfFrequency: Int = getColumnIndexOrThrow(_stmt, "frequency")
        val _columnIndexOfScheduledDays: Int = getColumnIndexOrThrow(_stmt, "scheduled_days")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfActive: Int = getColumnIndexOrThrow(_stmt, "active")
        val _columnIndexOfImageFileName: Int = getColumnIndexOrThrow(_stmt, "image_file_name")
        val _columnIndexOfVideoFileName: Int = getColumnIndexOrThrow(_stmt, "video_file_name")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "created_at")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updated_at")
        val _result: ExerciseEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpName: String
          _tmpName = _stmt.getText(_columnIndexOfName)
          val _tmpBodySystem: String
          _tmpBodySystem = _stmt.getText(_columnIndexOfBodySystem)
          val _tmpInstructions: String
          _tmpInstructions = _stmt.getText(_columnIndexOfInstructions)
          val _tmpNotes: String?
          if (_stmt.isNull(_columnIndexOfNotes)) {
            _tmpNotes = null
          } else {
            _tmpNotes = _stmt.getText(_columnIndexOfNotes)
          }
          val _tmpDurationMinutes: Int
          _tmpDurationMinutes = _stmt.getLong(_columnIndexOfDurationMinutes).toInt()
          val _tmpFrequency: String
          _tmpFrequency = _stmt.getText(_columnIndexOfFrequency)
          val _tmpScheduledDays: Int
          _tmpScheduledDays = _stmt.getLong(_columnIndexOfScheduledDays).toInt()
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpActive: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfActive).toInt()
          _tmpActive = _tmp != 0
          val _tmpImageFileName: String?
          if (_stmt.isNull(_columnIndexOfImageFileName)) {
            _tmpImageFileName = null
          } else {
            _tmpImageFileName = _stmt.getText(_columnIndexOfImageFileName)
          }
          val _tmpVideoFileName: String?
          if (_stmt.isNull(_columnIndexOfVideoFileName)) {
            _tmpVideoFileName = null
          } else {
            _tmpVideoFileName = _stmt.getText(_columnIndexOfVideoFileName)
          }
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _result = ExerciseEntity(_tmpId,_tmpName,_tmpBodySystem,_tmpInstructions,_tmpNotes,_tmpDurationMinutes,_tmpFrequency,_tmpScheduledDays,_tmpPriority,_tmpActive,_tmpImageFileName,_tmpVideoFileName,_tmpCreatedAt,_tmpUpdatedAt)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getActiveExerciseCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM exercises WHERE active = 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override fun getAllBodySystems(): Flow<List<String>> {
    val _sql: String = "SELECT DISTINCT body_system FROM exercises WHERE active = 1 ORDER BY body_system ASC"
    return createFlow(__db, false, arrayOf("exercises")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
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

  public override suspend fun deleteExerciseById(id: String) {
    val _sql: String = "DELETE FROM exercises WHERE id = ?"
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

  public override suspend fun setExerciseActive(id: String, active: Boolean) {
    val _sql: String = "UPDATE exercises SET active = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        val _tmp: Int = if (active) 1 else 0
        _stmt.bindLong(_argIndex, _tmp.toLong())
        _argIndex = 2
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
