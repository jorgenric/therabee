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

    /** Parses a CSV days string into a bitmask.
     *
     *  Accepts:
     *  - `Daily`, `All`, `Every day`, `Everyday` → all 7 days
     *  - `Weekdays` → Mon–Fri
     *  - `Weekends` → Sat–Sun
     *  - Comma-separated day names, any of:
     *      Mon / Monday / M
     *      Tue / Tuesday / Tu
     *      Wed / Wednesday / W
     *      Thu / Thursday / Th / R
     *      Fri / Friday / F
     *      Sat / Saturday / Sa
     *      Sun / Sunday / Su
     */
    fun fromCsvDays(csv: String): Int {
        val trimmed = csv.trim()

        // Whole-value shortcuts
        if (trimmed.matches(Regex("daily|all|every[\\s\\-]?day|everyday", RegexOption.IGNORE_CASE))) return ALL
        if (trimmed.equals("weekdays", ignoreCase = true)) return MON or TUE or WED or THU or FRI
        if (trimmed.equals("weekends", ignoreCase = true)) return SAT or SUN

        return trimmed.split(",").fold(0) { acc, day ->
            acc or when (day.trim().lowercase()) {
                "mon", "monday", "m"            -> MON
                "tue", "tuesday", "tu"          -> TUE
                "wed", "wednesday", "w"         -> WED
                "thu", "thursday", "th", "r"   -> THU
                "fri", "friday", "f"            -> FRI
                "sat", "saturday", "sa"         -> SAT
                "sun", "sunday", "su"           -> SUN
                else -> throw IllegalArgumentException(
                    "Unknown day \"${day.trim()}\". Use Mon/Tue/Wed/Thu/Fri/Sat/Sun (or Daily, Weekdays, Weekends)."
                )
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
