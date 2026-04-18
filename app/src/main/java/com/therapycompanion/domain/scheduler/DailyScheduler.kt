package com.therapycompanion.domain.scheduler

import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.Locale

/**
 * Pure function — no database access inside.
 * All data is fetched by the ViewModel/Repository and passed in.
 *
 * This is the most functionally complex part of the application.
 * Being a pure function makes it trivially unit-testable without mocking.
 */
object DailyScheduler {

    /**
     * Selects the exercises for today based on the user's library, recent sessions,
     * the current day, daily load setting, and Easier Day mode.
     *
     * Steps:
     * 1. Filter to exercises scheduled for today
     * 2. Filter out frequency-exhausted exercises
     * 3. Sort by priority ASC, then days-since-last-done DESC
     * 4. Apply daily load cap (halved + floored at 1 if easierDay)
     * 5. Ensure body-system diversity
     * 6. Return final list
     *
     * @param allExercises Full active exercise library
     * @param recentSessions Sessions from the past 7 days (must include today)
     * @param todayDayBit DayBits constant for today, e.g. DayBits.WED
     * @param dailyLoad Number of exercises target from UserSettings (1–10)
     * @param easierDay If true, load is halved (floored at 1)
     * @param today Today's LocalDate — injectable for testing
     * @param zoneId Timezone for day boundary calculations — injectable for testing
     */
    fun selectDailyExercises(
        allExercises: List<Exercise>,
        recentSessions: List<Session>,
        todayDayBit: Int,
        dailyLoad: Int,
        easierDay: Boolean = false,
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<Exercise> {
        if (allExercises.isEmpty()) return emptyList()

        val todayStart = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayEnd = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        val weekStart = currentWeekStart(today, zoneId)

        // Only completed sessions count toward frequency
        val completedSessions = recentSessions.filter { it.status == SessionStatus.Completed }

        // Step 1: Filter to exercises scheduled for today
        val scheduledToday = allExercises.filter { it.isScheduledForDay(todayDayBit) }

        // Step 2: Filter out frequency-exhausted exercises
        val eligible = scheduledToday.filter { exercise ->
            !isFrequencyExhausted(exercise, completedSessions, todayStart, todayEnd, weekStart)
        }

        if (eligible.isEmpty()) return emptyList()

        // Step 3: Sort by priority ASC (1 = highest), then days-since-last-done DESC
        val lastSessionByExercise: Map<String, Long> = completedSessions
            .groupBy { it.exerciseId }
            .mapValues { (_, sessions) -> sessions.maxOf { it.startedAt } }

        val sorted = eligible.sortedWith(
            compareBy<Exercise> { it.priority }
                .thenByDescending { exercise ->
                    lastSessionByExercise[exercise.id]?.let { lastMs ->
                        todayStart - lastMs // larger = more days since last done
                    } ?: Long.MAX_VALUE // never done = highest priority within priority level
                }
        )

        // Step 4: Apply daily load cap
        val rawCap = if (easierDay) maxOf(1, dailyLoad / 2) else dailyLoad
        val cap = rawCap.coerceIn(1, sorted.size)

        // Step 5: Body-system diversity pass
        val selected = applyBodySystemDiversity(sorted, cap, eligible)

        return selected
    }

    /**
     * Returns true if the exercise has been done enough times to be excluded today.
     * Only Completed sessions count — Skipped sessions do not.
     */
    private fun isFrequencyExhausted(
        exercise: Exercise,
        completedSessions: List<Session>,
        todayStart: Long,
        todayEnd: Long,
        weekStart: Long
    ): Boolean {
        val sessionsForExercise = completedSessions.filter { it.exerciseId == exercise.id }

        return when (exercise.frequency) {
            Frequency.Daily -> {
                // Exhausted if completed at any point today
                sessionsForExercise.any { it.startedAt in todayStart..todayEnd }
            }
            Frequency.ThreePerWeek -> {
                sessionsForExercise.count { it.startedAt >= weekStart } >= 3
            }
            Frequency.TwoPerWeek -> {
                sessionsForExercise.count { it.startedAt >= weekStart } >= 2
            }
            Frequency.Weekly -> {
                sessionsForExercise.any { it.startedAt >= weekStart }
            }
            Frequency.AsTolerated -> {
                // Never exhausted — always eligible if scheduled for today
                false
            }
        }
    }

    /**
     * Body-system diversity pass:
     * If 2+ exercises from the same body system are in the top-N selection
     * AND there are eligible exercises from underrepresented systems,
     * swap the lower-priority same-system exercise for the best underrepresented one.
     *
     * This prevents a day's plan from being entirely one body system.
     */
    private fun applyBodySystemDiversity(
        sortedEligible: List<Exercise>,
        cap: Int,
        allEligible: List<Exercise>
    ): List<Exercise> {
        val selected = sortedEligible.take(cap).toMutableList()
        val notSelected = allEligible.toMutableSet().also { it.removeAll(selected.toSet()) }

        // Find body systems with 2+ representatives in the selection (case-insensitive)
        val systemCounts = selected.groupBy { it.bodySystem.lowercase() }

        for ((system, exercisesInSystem) in systemCounts) {
            if (exercisesInSystem.size < 2) continue

            // Find the lowest-priority exercise in this system (highest priority number)
            val candidate = exercisesInSystem.maxByOrNull { it.priority } ?: continue

            // Find the best eligible exercise from an underrepresented system
            val underrepresentedSystems = allEligible
                .map { it.bodySystem.lowercase() }
                .toSet()
                .minus(systemCounts.keys)

            val replacement = notSelected
                .filter { it.bodySystem.lowercase() in underrepresentedSystems }
                .minByOrNull { it.priority } // lowest priority number = highest priority

            if (replacement != null) {
                selected.remove(candidate)
                selected.add(replacement)
                notSelected.remove(replacement)
                notSelected.add(candidate)
            }
        }

        // Re-sort the final selection to restore priority order
        return selected.sortedWith(
            compareBy<Exercise> { it.priority }
                .thenBy { it.name }
        )
    }

    /**
     * Returns the UTC epoch ms of the start of the current Mon–Sun week.
     * Week boundaries are Monday 00:00:00 in the device timezone.
     */
    fun currentWeekStart(
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val weekFields = WeekFields.of(Locale.getDefault())
        val startOfWeek = today.with(weekFields.dayOfWeek(), 1) // Monday
        return startOfWeek.atStartOfDay(zoneId).toInstant().toEpochMilli()
    }

    /**
     * Calculates the epoch ms boundaries for today.
     * Used by repositories when passing parameters to DailyScheduler.
     */
    fun todayBoundaries(
        today: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Pair<Long, Long> {
        val start = today.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli() - 1
        return start to end
    }
}
