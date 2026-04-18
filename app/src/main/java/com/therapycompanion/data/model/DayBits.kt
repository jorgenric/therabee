package com.therapycompanion.data.model

/**
 * Bitmask constants for scheduled days.
 * Stored as a single Int in the database — efficient for queries and CSV import.
 * Example: Mon + Wed + Fri = 1 + 4 + 16 = 21
 */
object DayBits {
    const val MON = 1   // 0000001
    const val TUE = 2   // 0000010
    const val WED = 4   // 0000100
    const val THU = 8   // 0001000
    const val FRI = 16  // 0010000
    const val SAT = 32  // 0100000
    const val SUN = 64  // 1000000
    const val ALL = 127 // 1111111 — every day

    /** Returns the DayBits constant for the given java.time.DayOfWeek */
    fun fromDayOfWeek(dayOfWeek: java.time.DayOfWeek): Int = when (dayOfWeek) {
        java.time.DayOfWeek.MONDAY    -> MON
        java.time.DayOfWeek.TUESDAY   -> TUE
        java.time.DayOfWeek.WEDNESDAY -> WED
        java.time.DayOfWeek.THURSDAY  -> THU
        java.time.DayOfWeek.FRIDAY    -> FRI
        java.time.DayOfWeek.SATURDAY  -> SAT
        java.time.DayOfWeek.SUNDAY    -> SUN
    }

    /** Parses a CSV days string like "Mon,Wed,Fri" or "Daily" into a bitmask */
    fun fromCsvDays(csv: String): Int {
        val trimmed = csv.trim()
        if (trimmed.equals("daily", ignoreCase = true)) return ALL

        return trimmed.split(",").fold(0) { acc, day ->
            acc or when (day.trim().lowercase()) {
                "mon", "monday"    -> MON
                "tue", "tuesday"   -> TUE
                "wed", "wednesday" -> WED
                "thu", "thursday"  -> THU
                "fri", "friday"    -> FRI
                "sat", "saturday"  -> SAT
                "sun", "sunday"    -> SUN
                else -> throw IllegalArgumentException("Unknown day abbreviation: ${day.trim()}")
            }
        }
    }

    /** Converts a bitmask back to a human-readable string like "Mon, Wed, Fri" */
    fun toDisplayString(bitmask: Int): String {
        if (bitmask == ALL) return "Daily"
        val days = buildList {
            if (bitmask and MON != 0) add("Mon")
            if (bitmask and TUE != 0) add("Tue")
            if (bitmask and WED != 0) add("Wed")
            if (bitmask and THU != 0) add("Thu")
            if (bitmask and FRI != 0) add("Fri")
            if (bitmask and SAT != 0) add("Sat")
            if (bitmask and SUN != 0) add("Sun")
        }
        return days.joinToString(", ")
    }
}
