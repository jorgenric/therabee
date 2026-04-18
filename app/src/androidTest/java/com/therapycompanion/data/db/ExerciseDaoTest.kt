package com.therapycompanion.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.therapycompanion.data.model.DayBits
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class ExerciseDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: ExerciseDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.exerciseDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun makeEntity(
        id: String = UUID.randomUUID().toString(),
        name: String = "Test Exercise",
        bodySystem: String = "Core",
        scheduledDays: Int = DayBits.ALL,
        active: Boolean = true,
        priority: Int = 2
    ) = ExerciseEntity(
        id = id,
        name = name,
        bodySystem = bodySystem,
        instructions = "Test instructions",
        durationMinutes = 10,
        frequency = "Daily",
        scheduledDays = scheduledDays,
        priority = priority,
        active = active
    )

    @Test
    fun insertAndRetrieve() = runTest {
        val entity = makeEntity(name = "Diaphragm Breathing")
        dao.insertExercise(entity)

        val retrieved = dao.getExerciseById(entity.id)
        assertNotNull(retrieved)
        assertEquals("Diaphragm Breathing", retrieved!!.name)
    }

    @Test
    fun getAllExercisesFlow() = runTest {
        dao.insertExercise(makeEntity(name = "Ex1"))
        dao.insertExercise(makeEntity(name = "Ex2"))

        val all = dao.getAllExercises().first()
        assertEquals(2, all.size)
    }

    @Test
    fun getActiveExercisesExcludesInactive() = runTest {
        dao.insertExercise(makeEntity(name = "Active", active = true))
        dao.insertExercise(makeEntity(name = "Inactive", active = false))

        val active = dao.getActiveExercises().first()
        assertEquals(1, active.size)
        assertEquals("Active", active.first().name)
    }

    @Test
    fun getExercisesForDayBitmask() = runTest {
        val monOnly = makeEntity(name = "MonOnly", scheduledDays = DayBits.MON)
        val allDays = makeEntity(name = "AllDays", scheduledDays = DayBits.ALL)
        val wedOnly = makeEntity(name = "WedOnly", scheduledDays = DayBits.WED)

        dao.insertExercises(listOf(monOnly, allDays, wedOnly))

        val mondayExercises = dao.getExercisesForDay(DayBits.MON)
        assertEquals(2, mondayExercises.size)
        assertTrue(mondayExercises.any { it.name == "MonOnly" })
        assertTrue(mondayExercises.any { it.name == "AllDays" })
        assertTrue(mondayExercises.none { it.name == "WedOnly" })
    }

    @Test
    fun getExercisesForDayExcludesInactive() = runTest {
        val active = makeEntity(name = "Active", scheduledDays = DayBits.ALL, active = true)
        val inactive = makeEntity(name = "Inactive", scheduledDays = DayBits.ALL, active = false)
        dao.insertExercises(listOf(active, inactive))

        val monday = dao.getExercisesForDay(DayBits.MON)
        assertEquals(1, monday.size)
        assertEquals("Active", monday.first().name)
    }

    @Test
    fun updateExercise() = runTest {
        val entity = makeEntity(name = "Original")
        dao.insertExercise(entity)

        val updated = entity.copy(name = "Updated")
        dao.updateExercise(updated)

        val retrieved = dao.getExerciseById(entity.id)
        assertEquals("Updated", retrieved?.name)
    }

    @Test
    fun deleteExerciseById() = runTest {
        val entity = makeEntity()
        dao.insertExercise(entity)
        dao.deleteExerciseById(entity.id)

        assertNull(dao.getExerciseById(entity.id))
    }

    @Test
    fun setExerciseActiveToggle() = runTest {
        val entity = makeEntity(active = true)
        dao.insertExercise(entity)

        dao.setExerciseActive(entity.id, false)
        assertEquals(false, dao.getExerciseById(entity.id)?.active)

        dao.setExerciseActive(entity.id, true)
        assertEquals(true, dao.getExerciseById(entity.id)?.active)
    }

    @Test
    fun getExerciseByName() = runTest {
        val entity = makeEntity(name = "Unique Name")
        dao.insertExercise(entity)

        val found = dao.getExerciseByName("Unique Name")
        assertNotNull(found)

        val notFound = dao.getExerciseByName("Nonexistent")
        assertNull(notFound)
    }

    @Test
    fun activeExerciseCount() = runTest {
        dao.insertExercise(makeEntity(active = true))
        dao.insertExercise(makeEntity(active = true))
        dao.insertExercise(makeEntity(active = false))

        assertEquals(2, dao.getActiveExerciseCount())
    }
}
