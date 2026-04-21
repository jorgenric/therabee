package com.therapycompanion.data.model

enum class SessionStatus {
    InProgress,
    Completed,
    Skipped,
    /** Exercise was performed but not fully completed — logged via "I just did this". */
    Partial;

    companion object {
        fun fromString(value: String): SessionStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown session status: $value")
    }
}
