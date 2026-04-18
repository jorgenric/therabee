package com.therapycompanion.data.model

enum class SessionStatus {
    InProgress,
    Completed,
    Skipped;

    companion object {
        fun fromString(value: String): SessionStatus =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown session status: $value")
    }
}
