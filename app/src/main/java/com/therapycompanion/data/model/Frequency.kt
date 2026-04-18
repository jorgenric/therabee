package com.therapycompanion.data.model

enum class Frequency(val displayName: String, val csvValue: String) {
    Daily("Daily", "Daily"),
    ThreePerWeek("3x / Week", "3xWeek"),
    TwoPerWeek("2x / Week", "2xWeek"),
    AsTolerated("As Tolerated", "AsTolerated"),
    Weekly("Weekly", "Weekly");

    companion object {
        fun fromCsvValue(value: String): Frequency =
            entries.firstOrNull { it.csvValue.equals(value.trim(), ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown frequency: $value")

        fun fromCsvValueOrNull(value: String): Frequency? =
            entries.firstOrNull { it.csvValue.equals(value.trim(), ignoreCase = true) }

        fun fromString(value: String): Frequency =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: fromCsvValue(value)
    }
}
