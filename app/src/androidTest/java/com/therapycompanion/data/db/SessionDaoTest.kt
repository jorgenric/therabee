package com.therapycompanion.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.therapycompanion.data.model.DayBits
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class SessionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var exerciseDao: ExerciseDao
    private lateinit var sessionDao: SessionDao

    private val testZone = ZoneOffset.UTC
    private val today = LocalDate.of(2024, 1, 15) // Monday
    private val todayStart = today.atStartOfDay(testZone).toInstant().toEpochMilli()
    private val todayEnd = today.plusDays(1).atStartOfDay(testZone).toInstant().toEpochMilli() - 1
    private val weekStart = today.atStartOfDay(testZone).toInstant().toEpochMilli() // Monday = start of week

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        exerciseDao = db.exerciseDao()
        sessionDao = db.sessionDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeExercise(id: String = UUID.randomUUID().toString()) = ExerciseEntity(
        id = id,
        name = "Exercise $id",
        bodySystem = "Core",
        instructions = "instructions",
        durationMinutes = 10,
        frequency = "Daily",
        scheduledDays = DayBits.ALL,
        priority = 2
    )

    private fun makeSession(
        exerciseId: String,
        startedAtMs: Long,
        status: String = "Completed"
    ) = SessionEntity(
        id = UUID.randomUUID().toString(),
        exerciseId = exerciseId,
        startedAt = startedAtMs,
        status = status
    )

    private fun ms(daysOffset: Long = 0, hourOffset: Int = 8): Long =
        todayStart + daysOffset * 86_400_000L + hourOffset * 3_600_000L

    @Test
    fun insertAndRetrieve() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)

        val session = makeSession(ex.id, ms())
        sessionDao.insertSession(session)

        val retrieved = sessionDao.getSessionById(session.id)
        assertNotNull(retrieved)
        assertEquals(ex.id, retrieved!!.exerciseId)
    }

    @Test
    fun getSessionsInDateRange() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)

        val inRange = makeSession(ex.id, ms())
        val outOfRange = makeSession(ex.id, ms(-2)) // 2 days ago
        sessionDao.insertSession(inRange)
        sessionDao.insertSession(outOfRange)

        val results = sessionDao.getSessionsInDateRange(todayStart, todayEnd)
        assertEquals(1, results.size)
        assertEquals(inRange.id, results.first().id)
    }

    @Test
    fun getLastSessionForExercise() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)

        val older = makeSession(ex.id, ms(hourOffset = 6))
        val newer = makeSession(ex.id, ms(hourOffset = 10))
        sessionDao.insertSession(older)
        sessionDao.insertSession(newer)

        val last = sessionDao.getLastSessionForExercise(ex.id)
        assertEquals(newer.id, last?.id)
    }

    @Test
    fun getLastSessionForExerciseReturnsNullIfNone() = runTest {
        val result = sessionDao.getLastSessionForExercise("nonexistent-id")
        assertNull(result)
    }

    @Test
    fun getCompletedSessionCountByExerciseInWeekCountsOnlyCompleted() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)

        val completed1 = makeSession(ex.id, ms(), status = "Completed")
        val completed2 = makeSession(ex.id, ms(hourOffset = 9), status = "Completed")
        val skipped = makeSession(ex.id, ms(hourOffset = 10), status = "Skipped")
        sessionDao.insertSession(completed1)
        sessionDao.insertSession(completed2)
        sessionDao.insertSession(skipped)

        val count = sessionDao.getCompletedSessionCountByExerciseInWeek(ex.id, weekStart)
        assertEquals(2, count)
    }

    @Test
    fun getCompletedSessionCountForExerciseTodayOnlyCounts() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)

        val today = makeSession(ex.id, ms(), status = "Completed")
        val yesterday = makeSession(ex.id, ms(-1), status = "Completed")
        sessionDao.insertSession(today)
        sessionDao.insertSession(yesterday)

        val count = sessionDao.getCompletedSessionCountForExerciseToday(
            ex.id, todayStart, todayEnd
        )
        assertEquals(1, count)
    }

    @Test
    fun getCompletedBodySystemsSince() = runTest {
        val ex1 = makeExercise().copy(bodySystem = "Core")
        val ex2 = makeExercise().copy(bodySystem = "Balance")
        val ex3 = makeExercise().copy(bodySystem = "Core") // duplicate system

        exerciseDao.insertExercise(ex1)
        exerciseDao.insertExercise(ex2)
        exerciseDao.insertExercise(ex3)

        sessionDao.insertSession(makeSession(ex1.id, ms(), "Completed"))
        sessionDao.insertSession(makeSession(ex2.id, ms(), "Completed"))
        sessionDao.insertSession(makeSession(ex3.id, ms(), "Completed"))

        val systems = sessionDao.getCompletedBodySystemsSince(todayStart)
        assertEquals(2, systems.size) // Core and Balance, deduplicated
        assert(systems.contains("Core"))
        assert(systems.contains("Balance"))
    }

    @Test
    fun sessionDeletedWhenExerciseDeleted() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)
        val session = makeSession(ex.id, ms())
        sessionDao.insertSession(session)

        exerciseDao.deleteExerciseById(ex.id) // CASCADE should remove session

        val retrieved = sessionDao.getSessionById(session.id)
        assertNull(retrieved)
    }

    @Test
    fun updateSession() = runTest {
        val ex = makeExercise()
        exerciseDao.insertExercise(ex)
        val session = makeSession(ex.id, ms(), status = "InProgress")
        sessionDao.insertSession(session)

        val completed = session.copy(status = "Completed", elapsedSeconds = 600)
        sessionDao.updateSession(completed)

        val retrieved = sessionDao.getSessionById(session.id)
        assertEquals("Completed", retrieved?.status)
        assertEquals(600, retrieved?.elapsedSeconds)
    }
}
