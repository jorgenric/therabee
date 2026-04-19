package com.therapycompanion.import

import android.content.Context
import android.net.Uri
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Frequency
import java.nio.charset.Charset
import java.util.UUID

/**
 * Result of parsing a CSV/TSV import file.
 */
sealed class ImportResult {
    /** File parsed successfully — exercises ready for preview/confirmation */
    data class Success(val exercises: List<Exercise>) : ImportResult()
    /** One or more rows had errors — nothing imported yet */
    data class ValidationErrors(val errors: List<RowError>) : ImportResult()
    /** File-level error (encoding, empty, malformed header) */
    data class FileError(val message: String) : ImportResult()
}

data class RowError(
    val row: Int,       // 1-based (row 1 = header, row 2 = first data row)
    val column: String,
    val message: String
) {
    override fun toString() = "Row $row ($column): $message"
}

/**
 * Duplicate resolution strategy per exercise name.
 */
enum class DuplicateStrategy {
    Skip,       // Ignore this exercise — keep existing
    Replace,    // Overwrite existing exercise
    ImportAsNew // Insert with a new UUID (both coexist)
}

/**
 * Parses CSV or TSV exercise import files as specified in §6 of the build instructions.
 *
 * Rules:
 * - Auto-detects CSV vs TSV by file extension
 * - Validates entire file before committing anything (all-or-nothing)
 * - Case-insensitive header matching
 * - UTF-8 required — rejects other encodings
 */
object CsvImporter {

    private val requiredColumns = listOf("name", "body_system", "instructions", "duration", "frequency", "days", "priority")
    private val optionalColumns = listOf("notes", "active")

    /**
     * Parses and validates the file at [uri].
     * Returns a [ImportResult] — never throws.
     */
    fun parse(context: Context, uri: Uri): ImportResult {
        return try {
            val extension = context.contentResolver.getType(uri)
                ?: uri.lastPathSegment?.substringAfterLast('.') ?: "csv"
            val isTsv = extension.contains("tsv", ignoreCase = true) ||
                uri.lastPathSegment?.endsWith(".tsv", ignoreCase = true) == true

            val delimiter = if (isTsv) '\t' else ','

            val stream = context.contentResolver.openInputStream(uri)
                ?: return ImportResult.FileError("Could not open file")

            val bytes = stream.readBytes()
            val text = decodeBytes(bytes)
                ?: return ImportResult.FileError(
                    "Could not decode file. Save your spreadsheet as UTF-8 CSV and try again."
                )
            val lines = text.lines().filter { it.isNotBlank() && !it.trimStart().startsWith("#") }

            if (lines.isEmpty()) return ImportResult.FileError("File is empty.")

            val headerLine = lines.first()
            val headers = parseLine(headerLine, delimiter).map { it.trim().lowercase() }

            // Validate required headers
            val missingHeaders = requiredColumns.filter { it !in headers }
            if (missingHeaders.isNotEmpty()) {
                return ImportResult.FileError(
                    "Missing required columns: ${missingHeaders.joinToString(", ")}. " +
                    "Required: ${requiredColumns.joinToString(", ")}"
                )
            }

            val dataLines = lines.drop(1)
            if (dataLines.isEmpty()) return ImportResult.FileError("File has no data rows (only a header).")

            val errors = mutableListOf<RowError>()
            val exercises = mutableListOf<Exercise>()

            dataLines.forEachIndexed { index, line ->
                val rowNum = index + 2 // row 1 = header, row 2 = first data row
                val cols = parseLine(line, delimiter)
                val row = headers.zip(cols).toMap()

                val result = parseRow(row, rowNum)
                when {
                    result.errors.isNotEmpty() -> errors.addAll(result.errors)
                    result.exercise != null -> exercises.add(result.exercise)
                }
            }

            if (errors.isNotEmpty()) ImportResult.ValidationErrors(errors)
            else ImportResult.Success(exercises)

        } catch (e: Exception) {
            ImportResult.FileError("Unexpected error parsing file: ${e.message}")
        }
    }

    /**
     * Exports a blank template CSV with one example row to the app's files directory.
     * Returns the File path on success, null on failure.
     */
    fun exportTemplate(context: Context): java.io.File? {
        return try {
            val file = java.io.File(context.filesDir, "exercise_import_template.csv")
            file.writeText(buildString {
                appendLine("# body_system: any label you like (e.g. Respiratory, Lower Extremity, Core). No fixed list.")
                appendLine("# duration: whole or decimal minutes — 5, 7.5, 10 (decimals rounded up)")
                appendLine("# frequency: Daily | 3xWeek | 2xWeek | AsTolerated | Weekly")
                appendLine("#   also accepted: 3x/Week, twice a week, as tolerated, once a week, every day, etc.")
                appendLine("# days: Daily | Weekdays | Weekends | or comma-separated: Mon,Wed,Fri")
                appendLine("#   abbreviations: M Tu W Th F Sa Su  (or full names: Monday, Tuesday, ...)")
                appendLine("# priority: 1 (essential) | 2 (important) | 3 (supplemental)")
                appendLine("#   also accepted: high | medium | low")
                appendLine("# active: true/false — defaults to true if omitted")
                appendLine("name,body_system,instructions,duration,frequency,days,priority,notes,active")
                appendLine("Diaphragm Breathing,Respiratory,\"Breathe in slowly through your nose for 4 counts, hold for 2, exhale for 6.\",5,Daily,Daily,1,Perform seated or supine,true")
                appendLine("Hip Flexor Stretch,Lower Extremity,Hold the stretch for 30 seconds each side.,3,3xWeek,\"Mon,Wed,Fri\",2,,true")
                appendLine("Quad Sets,Lower Extremity,Tighten quad and hold for 10 seconds.,5,2xWeek,Weekdays,high,Only if pain below 5/10,true")
            })
            file
        } catch (_: Exception) { null }
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private data class RowResult(
        val exercise: Exercise? = null,
        val errors: List<RowError> = emptyList()
    )

    private fun parseRow(row: Map<String, String>, rowNum: Int): RowResult {
        val errors = mutableListOf<RowError>()

        fun String.field(): String? = row[this]?.trim()?.ifBlank { null }

        // Name
        val name = "name".field()
        if (name == null) errors.add(RowError(rowNum, "name", "Required — cannot be blank"))
        else if (name.length > 100) errors.add(RowError(rowNum, "name", "Must be 100 characters or fewer"))

        // Body system — any non-empty string up to 100 characters is valid
        val bodySystemStr = "body_system".field()
        val bodySystem = when {
            bodySystemStr == null ->
                null.also { errors.add(RowError(rowNum, "body_system", "Required — cannot be blank")) }
            bodySystemStr.length > 100 ->
                null.also { errors.add(RowError(rowNum, "body_system", "Must be 100 characters or fewer")) }
            else -> bodySystemStr
        }

        // Instructions
        val instructions = "instructions".field()
        if (instructions == null) errors.add(RowError(rowNum, "instructions", "Required — cannot be blank"))

        // Duration — accepts integers or decimals (rounded up to nearest minute)
        val durationStr = "duration".field()
        val duration = if (durationStr == null) {
            errors.add(RowError(rowNum, "duration", "Required — cannot be blank"))
            null
        } else {
            val asDouble = durationStr.toDoubleOrNull()
            when {
                asDouble == null ->
                    null.also { errors.add(RowError(rowNum, "duration", "\"$durationStr\" is not a valid number")) }
                asDouble < 1.0 ->
                    null.also { errors.add(RowError(rowNum, "duration", "Must be at least 1 minute")) }
                else -> Math.ceil(asDouble).toInt()
            }
        }

        // Frequency
        val frequencyStr = "frequency".field()
        val frequency = if (frequencyStr == null) {
            errors.add(RowError(rowNum, "frequency", "Required — cannot be blank"))
            null
        } else {
            Frequency.fromCsvValueOrNull(frequencyStr) ?: run {
                errors.add(RowError(rowNum, "frequency",
                    "\"$frequencyStr\" is not valid. Must be one of: Daily, 3xWeek, 2xWeek, AsTolerated, Weekly"))
                null
            }
        }

        // Days
        val daysStr = "days".field()
        val scheduledDays = if (daysStr == null) {
            errors.add(RowError(rowNum, "days", "Required — cannot be blank"))
            null
        } else {
            try {
                DayBits.fromCsvDays(daysStr)
            } catch (e: Exception) {
                errors.add(RowError(rowNum, "days", e.message ?: "Invalid days format"))
                null
            }
        }

        // Priority — accepts 1/2/3 or text labels
        val priorityStr = "priority".field()
        val priority = if (priorityStr == null) {
            errors.add(RowError(rowNum, "priority", "Required — cannot be blank"))
            null
        } else {
            val asInt = priorityStr.toIntOrNull()
            when {
                asInt != null && asInt in 1..3 -> asInt
                asInt != null ->
                    null.also { errors.add(RowError(rowNum, "priority", "Must be 1, 2, or 3")) }
                priorityStr.matches(Regex("high|essential|must", RegexOption.IGNORE_CASE)) -> 1
                priorityStr.matches(Regex("med(ium)?|important|should", RegexOption.IGNORE_CASE)) -> 2
                priorityStr.matches(Regex("low|supplemental|nice", RegexOption.IGNORE_CASE)) -> 3
                else ->
                    null.also { errors.add(RowError(rowNum, "priority",
                        "\"$priorityStr\" is not valid. Use 1, 2, or 3 (or: high/medium/low)")) }
            }
        }

        // Optional fields
        val notes = "notes".field()
        val activeStr = row["active"]?.trim()?.lowercase()
        val active = when (activeStr) {
            "false", "0", "no" -> false
            else -> true // defaults to true if absent or any other value
        }

        if (errors.isNotEmpty()) return RowResult(errors = errors)

        val now = System.currentTimeMillis()
        return RowResult(
            exercise = Exercise(
                id = UUID.randomUUID().toString(),
                name = name!!,
                bodySystem = bodySystem!!,
                instructions = instructions!!,
                notes = notes,
                durationMinutes = duration!!,
                frequency = frequency!!,
                scheduledDays = scheduledDays!!,
                priority = priority!!,
                active = active,
                imageFileName = null,
                videoFileName = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }

    /**
     * Parses a single CSV/TSV line respecting quoted fields.
     * Handles commas inside double-quoted fields.
     */
    private fun parseLine(line: String, delimiter: Char): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false

        var i = 0
        while (i < line.length) {
            val ch = line[i]
            when {
                ch == '"' && !inQuotes -> inQuotes = true
                ch == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"') // escaped quote
                        i++
                    } else {
                        inQuotes = false
                    }
                }
                ch == delimiter && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(ch)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    /**
     * Attempts to decode [bytes] as text, trying encodings in order:
     *  1. UTF-8 (with optional BOM stripped)
     *  2. UTF-16 (with BOM)
     *  3. Windows-1252 / ISO-8859-1 (common Excel export encoding)
     *
     * Returns null only if all attempts fail (unlikely for any real text file).
     */
    private fun decodeBytes(bytes: ByteArray): String? {
        // Strip UTF-8 BOM (EF BB BF) if present
        val (data, startOffset) = if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) Pair(bytes, 3) else Pair(bytes, 0)

        // 1. Try UTF-8
        if (isValidUtf8(data, startOffset)) {
            return String(data, startOffset, data.size - startOffset, Charsets.UTF_8)
        }

        // 2. Try UTF-16 (BOM required: FE FF or FF FE)
        if (bytes.size >= 2 && (
            (bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte()) ||
            (bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte())
        )) {
            return try { String(bytes, Charsets.UTF_16) } catch (_: Exception) { null }
        }

        // 3. Fall back to Windows-1252 (superset of ISO-8859-1; never throws)
        return String(bytes, Charset.forName("windows-1252"))
    }

    private fun isValidUtf8(bytes: ByteArray, start: Int): Boolean {
        var i = start
        while (i < bytes.size) {
            val b = bytes[i].toInt() and 0xFF
            val extraBytes = when {
                b shr 7 == 0    -> 0  // ASCII
                b shr 5 == 0b110   -> 1
                b shr 4 == 0b1110  -> 2
                b shr 3 == 0b11110 -> 3
                else -> return false  // invalid lead byte
            }
            if (i + extraBytes >= bytes.size) return false
            repeat(extraBytes) { j ->
                if ((bytes[i + 1 + j].toInt() and 0xC0) != 0x80) return false
            }
            i += 1 + extraBytes
        }
        return true
    }
}
