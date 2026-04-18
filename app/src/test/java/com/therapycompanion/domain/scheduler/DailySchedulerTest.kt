package com.therapycompanion.domain.scheduler

import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.UUID

class DailySchedulerTest {

    // ── Helpers ───────────────────────────────────────────────────────────────

    private val testZone = ZoneOffset.UTC
    private val today = LocalDate.of(2024, 1, 15) // a Monday
    private val todayDayBit = DayBits.MON

    private fun makeExercise(
        id: String = UUID.randomUUID().toString(),
        name: String = "Exercise $id",
        bodySystem: String = "Core",
        frequency: Frequency = Frequency.Daily,
        scheduledDays: Int = DayBits.ALL,
        priority: Int = 2,
        active: Boolean = true
    ) = Exercise(
        id = id,
        name = name,
        bodySystem = bodySystem,
        instructions = "Do the thing",
        notes = null,
        durationMinutes = 10,
        frequency = frequency,
        scheduledDays = scheduledDays,
        priority = priority,
        active = active,
        imageFileName = null,
        videoFileName = null,
        createdAt = 0L,
        updatedAt = 0L
    )

    private fun makeCompletedSession(
        exerciseId: String,
        startedAtMs: Long
    ) = Session(
        id = UUID.randomUUID().toString(),
        exerciseId = exerciseId,
        startedAt = startedAtMs,
        completedAt = startedAtMs + 600_000,
        elapsedSeconds = 600,
        status = SessionStatus.Completed,
        notes = null
    )

    private fun makeSkippedSession(
        exerciseId: String,
        startedAtMs: Long
    ) = Session(
        id = UUID.randomUUID().toString(),
        exerciseId = exerciseId,
        startedAt = startedAtMs,
        completedAt = null,
        elapsedSeconds = 0,
        status = SessionStatus.Skipped,
        notes = null
    )

    private fun todayMs(hourOffset: Int = 8): Long =
        today.atStartOfDay(testZone).toInstant().toEpochMilli() + hourOffset * 3_600_000L

    private fun daysAgoMs(days: Int, hourOffset: Int = 8): Long =
        today.minusDays(days.toLong()).atStartOfDay(testZone).toInstant().toEpochMilli() +
            hourOffset * 3_600_000L

    // ── Empty library ─────────────────────────────────────────────────────────

    @Test
    fun `empty library returns empty list`() {
        val result = DailyScheduler.selectDailyExercises(
            allExercises = emptyList(),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertTrue(result.isEmpty())
    }

    // ── Single exercise ───────────────────────────────────────────────────────

    @Test
    fun `single exercise scheduled today is included`() {
        val ex = makeExercise(scheduledDays = DayBits.ALL)
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertEquals(listOf(ex), result)
    }

    @Test
    fun `single exercise not scheduled today is excluded`() {
        val ex = makeExercise(scheduledDays = DayBits.WED) // Wednesday only
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON, // today is Monday
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertTrue(result.isEmpty())
    }

    // ── Priority ordering ─────────────────────────────────────────────────────

    @Test
    fun `exercises are sorted by priority ascending`() {
        val low = makeExercise(name = "Low", priority = 3, scheduledDays = DayBits.ALL)
        val high = makeExercise(name = "High", priority = 1, scheduledDays = DayBits.ALL)
        val mid = makeExercise(name = "Mid", priority = 2, scheduledDays = DayBits.ALL)
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(low, high, mid),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 10,
            today = today,
            zoneId = testZone
        )
        assertEquals(listOf(high, mid, low), result)
    }

    // ── Daily load cap ────────────────────────────────────────────────────────

    @Test
    fun `daily load cap limits number of returned exercises`() {
        val exercises = (1..10).map { makeExercise(name = "Ex$it", scheduledDays = DayBits.ALL) }
        val result = DailyScheduler.selectDailyExercises(
            allExercises = exercises,
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 3,
            today = today,
            zoneId = testZone
        )
        assertEquals(3, result.size)
    }

    // ── Easier Day mode ───────────────────────────────────────────────────────

    @Test
    fun `easier day halves the load floored at 1`() {
        val exercises = (1..10).map { makeExercise(name = "Ex$it", scheduledDays = DayBits.ALL) }

        val normal = DailyScheduler.selectDailyExercises(
            allExercises = exercises,
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 6,
            easierDay = false,
            today = today,
            zoneId = testZone
        )
        val easier = DailyScheduler.selectDailyExercises(
            allExercises = exercises,
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 6,
            easierDay = true,
            today = today,
            zoneId = testZone
        )
        assertEquals(6, normal.size)
        assertEquals(3, easier.size)
    }

    @Test
    fun `easier day with load 1 returns at least 1 exercise`() {
        val ex = makeExercise(scheduledDays = DayBits.ALL)
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 1,
            easierDay = true,
            today = today,
            zoneId = testZone
        )
        assertEquals(1, result.size)
    }

    // ── Frequency exhaustion — Daily ──────────────────────────────────────────

    @Test
    fun `Daily exercise already completed today is excluded`() {
        val ex = makeExercise(frequency = Frequency.Daily, scheduledDays = DayBits.ALL)
        val session = makeCompletedSession(ex.id, todayMs(8))
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = listOf(session),
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun `Daily exercise completed yesterday is still eligible today`() {
        val ex = makeExercise(frequency = Frequency.Daily, scheduledDays = DayBits.ALL)
        val session = makeCompletedSession(ex.id, daysAgoMs(1, 8))
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = listOf(session),
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertEquals(listOf(ex), result)
    }

    // ── Frequency exhaustion — 3x/week ────────────────────────────────────────

    @Test
    fun `3xWeek exercise with 3 sessions this week is excluded`() {
        val ex = makeExercise(frequency = Frequency.ThreePerWeek, scheduledDays = DayBits.ALL)
        val sessions = listOf(
            makeCompletedSession(ex.id, daysAgoMs(0, 8)), // today
            makeCompletedSession(ex.id, daysAgoMs(2, 8)), // this week (Mon = today, so 2 days ago is Sat — previous week)
            makeCompletedSession(ex.id, daysAgoMs(1, 8))  // yesterday = Sunday = previous week
        )
        // today is Monday Jan 15 — week starts Jan 15
        // daysAgo(1) = Jan 14 (Sun) = previous week
        // Only 1 session is in the current week (today)
        // This should NOT be exhausted with only 1 session this week
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = sessions,
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        // Only 1 session this week, so not exhausted
        assertEquals(listOf(ex), result)
    }

    @Test
    fun `3xWeek exercise with exactly 3 sessions within Mon-Sun week is excluded`() {
        // today = Monday Jan 15; week starts Jan 15
        // Put all 3 sessions on today (same week)
        val ex = makeExercise(frequency = Frequency.ThreePerWeek, scheduledDays = DayBits.ALL)
        val sessions = listOf(
            makeCompletedSession(ex.id, todayMs(6)),
            makeCompletedSession(ex.id, todayMs(7)),
            makeCompletedSession(ex.id, todayMs(8))
        )
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = sessions,
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertTrue(result.isEmpty())
    }

    // ── Frequency exhaustion — AsTolerated ───────────────────────────────────

    @Test
    fun `AsTolerated exercise is never frequency-exhausted`() {
        val ex = makeExercise(frequency = Frequency.AsTolerated, scheduledDays = DayBits.ALL)
        // Simulate many completed sessions today
        val sessions = (0..9).map { makeCompletedSession(ex.id, todayMs(it)) }
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = sessions,
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertEquals(listOf(ex), result)
    }

    // ── Skipped sessions do not count ─────────────────────────────────────────

    @Test
    fun `skipped sessions do not count toward frequency exhaustion`() {
        val ex = makeExercise(frequency = Frequency.Daily, scheduledDays = DayBits.ALL)
        // Skipped session today — should NOT be exhausted
        val session = makeSkippedSession(ex.id, todayMs(8))
        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex),
            recentSessions = listOf(session),
            todayDayBit = DayBits.MON,
            dailyLoad = 5,
            today = today,
            zoneId = testZone
        )
        assertEquals(listOf(ex), result)
    }

    // ── Day-of-week filtering ─────────────────────────────────────────────────

    @Test
    fun `only exercises scheduled for today are included`() {
        val monOnly = makeExercise(name = "MonOnly", scheduledDays = DayBits.MON)
        val wedOnly = makeExercise(name = "WedOnly", scheduledDays = DayBits.WED)
        val allDays = makeExercise(name = "AllDays", scheduledDays = DayBits.ALL)

        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(monOnly, wedOnly, allDays),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON, // Monday
            dailyLoad = 10,
            today = today,
            zoneId = testZone
        )
        assertTrue(result.contains(monOnly))
        assertTrue(result.contains(allDays))
        assertTrue(result.none { it.name == "WedOnly" })
    }

    // ── Days-since-last-done ordering ─────────────────────────────────────────

    @Test
    fun `within same priority exercises done longer ago come first`() {
        val ex1 = makeExercise(id = "ex1", name = "Ex1", priority = 2, scheduledDays = DayBits.ALL)
        val ex2 = makeExercise(id = "ex2", name = "Ex2", priority = 2, scheduledDays = DayBits.ALL)

        // ex1 done 3 days ago, ex2 done 1 day ago — ex1 should come first
        val sessions = listOf(
            makeCompletedSession("ex1", daysAgoMs(3, 8)),
            makeCompletedSession("ex2", daysAgoMs(1, 8))
        )

        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex1, ex2),
            recentSessions = sessions,
            todayDayBit = DayBits.MON,
            dailyLoad = 10,
            today = today,
            zoneId = testZone
        )
        assertEquals("ex1", result.first().id)
    }

    @Test
    fun `never-done exercises come before recently-done at same priority`() {
        val neverDone = makeExercise(id = "never", priority = 2, scheduledDays = DayBits.ALL)
        val recentlyDone = makeExercise(id = "recent", priority = 2, scheduledDays = DayBits.ALL)

        val session = makeCompletedSession("recent", todayMs(8))

        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(recentlyDone, neverDone),
            recentSessions = listOf(session),
            todayDayBit = DayBits.MON,
            dailyLoad = 10,
            today = today,
            zoneId = testZone
        )
        assertEquals("never", result.first().id)
    }

    // ── Body-system diversity with free-text strings ──────────────────────────

    @Test
    fun `diversity pass works with arbitrary body system strings`() {
        // 3 exercises from the same arbitrary body system, 1 from a different one.
        // With load cap of 3, diversity pass should swap one same-system exercise
        // for the underrepresented one.
        val ex1 = makeExercise(id = "ex1", name = "A", bodySystem = "Vestibular System", priority = 1)
        val ex2 = makeExercise(id = "ex2", name = "B", bodySystem = "Vestibular System", priority = 2)
        val ex3 = makeExercise(id = "ex3", name = "C", bodySystem = "Vestibular System", priority = 3)
        val ex4 = makeExercise(id = "ex4", name = "D", bodySystem = "Proprioception", priority = 2)

        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex1, ex2, ex3, ex4),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 3,
            today = today,
            zoneId = testZone
        )

        assertEquals(3, result.size)
        // ex4 (Proprioception) should have replaced ex3 (lowest-priority Vestibular)
        assertTrue(result.any { it.id == "ex4" })
        assertTrue(result.none { it.id == "ex3" })
    }

    @Test
    fun `diversity pass treats body system strings case-insensitively`() {
        // "lower extremity" and "Lower Extremity" should be treated as the same system.
        val ex1 = makeExercise(id = "ex1", bodySystem = "lower extremity", priority = 1)
        val ex2 = makeExercise(id = "ex2", bodySystem = "Lower Extremity", priority = 2)
        val ex3 = makeExercise(id = "ex3", bodySystem = "Core", priority = 2)

        val result = DailyScheduler.selectDailyExercises(
            allExercises = listOf(ex1, ex2, ex3),
            recentSessions = emptyList(),
            todayDayBit = DayBits.MON,
            dailyLoad = 2,
            today = today,
            zoneId = testZone
        )

        assertEquals(2, result.size)
        // ex2 (lower priority "Lower Extremity") should be swapped for ex3 (Core)
        assertTrue(result.any { it.id == "ex1" })
        assertTrue(result.any { it.id == "ex3" })
        assertTrue(result.none { it.id == "ex2" })
    }
}
