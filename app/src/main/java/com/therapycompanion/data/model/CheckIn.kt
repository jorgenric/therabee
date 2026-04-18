package com.therapycompanion.data.model

/**
 * Domain model for an FPS-R pain / energy check-in.
 *
 * Pain score follows the FPS-R instrument: 0, 2, 4, 6, 8, or 10.
 * Energy score is 0–10 ascending (0 = no energy, 10 = full energy).
 * BPI domain rotates by day of week.
 */
data class CheckIn(
    val id: String,
    val checkedInAt: Long,
    val painScore: Int?,
    val energyScore: Int?,
    val bpiDomain: String?,
    val bpiScore: Int?,
    val freeText: String?,
    val dismissed: Boolean
) {
    val hasScores: Boolean get() = painScore != null && energyScore != null
}

/** The seven BPI domains that rotate through the week */
object BpiDomains {
    val all = listOf(
        "General Activity",
        "Mood",
        "Walking Ability",
        "Normal Work",
        "Relations with Others",
        "Sleep",
        "Enjoyment of Life"
    )

    /**
     * Returns the BPI domain to ask on a given day (0 = Monday, 6 = Sunday).
     * Rotates through the list so each domain comes up roughly weekly.
     */
    fun forDayOfWeek(dayOfWeek: java.time.DayOfWeek): String =
        all[(dayOfWeek.value - 1) % all.size]
}
