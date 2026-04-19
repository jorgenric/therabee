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

        fun fromCsvValueOrNull(value: String): Frequency? {
            val v = value.trim()
            // Exact match first
            entries.firstOrNull { it.csvValue.equals(v, ignoreCase = true) }?.let { return it }
            // Aliases
            return when {
                v.matches(Regex("3\\s*[x×]\\s*/?\\s*(week|wk|w)?", RegexOption.IGNORE_CASE))
                    || v.matches(Regex("(3|three)\\s*(times?|x)\\s*(a|per)?\\s*(week|wk)", RegexOption.IGNORE_CASE))
                    -> ThreePerWeek
                v.matches(Regex("2\\s*[x×]\\s*/?\\s*(week|wk|w)?", RegexOption.IGNORE_CASE))
                    || v.matches(Regex("(2|two|twice)\\s*(times?|x)?\\s*(a|per)?\\s*(week|wk)?", RegexOption.IGNORE_CASE))
                    -> TwoPerWeek
                v.matches(Regex("as[\\s\\-]?tolerated|prn", RegexOption.IGNORE_CASE))
                    -> AsTolerated
                v.matches(Regex("1\\s*[x×]\\s*/?\\s*(week|wk|w)?|once\\s*(a|per)?\\s*(week|wk)?", RegexOption.IGNORE_CASE))
                    -> Weekly
                v.matches(Regex("every[\\s\\-]?day|everyday", RegexOption.IGNORE_CASE))
                    -> Daily
                else -> null
            }
        }

        fun fromString(value: String): Frequency =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: fromCsvValue(value)
    }
}
