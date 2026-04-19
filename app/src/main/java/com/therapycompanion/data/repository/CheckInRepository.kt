package com.therapycompanion.data.repository

import com.therapycompanion.data.db.CheckInDao
import com.therapycompanion.data.db.toDomain
import com.therapycompanion.data.db.toEntity
import com.therapycompanion.data.model.CheckIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CheckInRepository(private val dao: CheckInDao) {

    fun getAllCheckIns(): Flow<List<CheckIn>> =
        dao.getAllCheckIns().map { list -> list.map { it.toDomain() } }

    suspend fun getAllCheckInsOnce(): List<CheckIn> =
        dao.getAllCheckInsOnce().map { it.toDomain() }

    suspend fun getCheckInById(id: String): CheckIn? =
        dao.getCheckInById(id)?.toDomain()

    suspend fun getCheckInsInDateRange(start: Long, end: Long): List<CheckIn> =
        dao.getCheckInsInDateRange(start, end).map { it.toDomain() }

    fun getCheckInsInDateRangeFlow(start: Long, end: Long): Flow<List<CheckIn>> =
        dao.getCheckInsInDateRangeFlow(start, end).map { list -> list.map { it.toDomain() } }

    suspend fun getCompletedCheckInCountToday(dayStart: Long, dayEnd: Long): Int =
        dao.getCompletedCheckInCountToday(dayStart, dayEnd)

    suspend fun hasCompletedCheckInToday(dayStart: Long, dayEnd: Long): Boolean =
        getCompletedCheckInCountToday(dayStart, dayEnd) > 0

    suspend fun insertCheckIn(checkIn: CheckIn) =
        dao.insertCheckIn(checkIn.toEntity())

    suspend fun updateCheckIn(checkIn: CheckIn) =
        dao.updateCheckIn(checkIn.toEntity())

    suspend fun deleteCheckInById(id: String) =
        dao.deleteCheckInById(id)
}
