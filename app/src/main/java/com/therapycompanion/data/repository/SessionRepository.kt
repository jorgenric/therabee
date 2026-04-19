package com.therapycompanion.data.repository

import com.therapycompanion.data.db.SessionDao
import com.therapycompanion.data.db.toDomain
import com.therapycompanion.data.db.toEntity
import com.therapycompanion.data.model.Session
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionRepository(private val dao: SessionDao) {

    fun getAllSessions(): Flow<List<Session>> =
        dao.getAllSessions().map { list -> list.map { it.toDomain() } }

    suspend fun getAllSessionsOnce(): List<Session> =
        dao.getAllSessionsOnce().map { it.toDomain() }

    suspend fun getSessionById(id: String): Session? =
        dao.getSessionById(id)?.toDomain()

    suspend fun getSessionsInDateRange(start: Long, end: Long): List<Session> =
        dao.getSessionsInDateRange(start, end).map { it.toDomain() }

    fun getSessionsInDateRangeFlow(start: Long, end: Long): Flow<List<Session>> =
        dao.getSessionsInDateRangeFlow(start, end).map { list -> list.map { it.toDomain() } }

    suspend fun getLastSessionForExercise(exerciseId: String): Session? =
        dao.getLastSessionForExercise(exerciseId)?.toDomain()

    suspend fun getCompletedSessionCountByExerciseInWeek(
        exerciseId: String,
        weekStart: Long
    ): Int = dao.getCompletedSessionCountByExerciseInWeek(exerciseId, weekStart)

    suspend fun getCompletedSessionCountForExerciseToday(
        exerciseId: String,
        dayStart: Long,
        dayEnd: Long
    ): Int = dao.getCompletedSessionCountForExerciseToday(exerciseId, dayStart, dayEnd)

    fun getSessionsForExercise(exerciseId: String): Flow<List<Session>> =
        dao.getSessionsForExercise(exerciseId).map { list -> list.map { it.toDomain() } }

    suspend fun getCompletedBodySystemsSince(since: Long): List<String> =
        dao.getCompletedBodySystemsSince(since)

    suspend fun insertSession(session: Session) =
        dao.insertSession(session.toEntity())

    suspend fun updateSession(session: Session) =
        dao.updateSession(session.toEntity())

    suspend fun deleteSessionById(id: String) =
        dao.deleteSessionById(id)
}
