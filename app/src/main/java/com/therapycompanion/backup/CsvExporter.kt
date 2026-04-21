package com.therapycompanion.backup

import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.data.model.Session
import java.io.OutputStream
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Produces read-only CSV exports of session history and check-in history.
 * These are view-only exports intended for use in spreadsheet tools.
 * Images and binary data are excluded.
 */
object CsvExporter {

    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    private fun Long.toReadable(): String = formatter.format(Instant.ofEpochMilli(this))

    private fun String.csvEscape(): String {
        val needsQuoting = contains(',') || contains('"') || contains('\n')
        return if (needsQuoting) "\"${replace("\"", "\"\"")}\"" else this
    }

    /**
     * Writes session history as CSV to [outputStream].
     * Columns: id, exerciseId, date, status, source, elapsedSeconds, notes
     */
    fun exportSessions(sessions: List<Session>, outputStream: OutputStream) {
        val writer = outputStream.writer()
        writer.write("id,exerciseId,date,status,source,elapsedSeconds,notes\n")
        sessions.sortedByDescending { it.startedAt }.forEach { s ->
            writer.write(
                listOf(
                    s.id,
                    s.exerciseId,
                    s.startedAt.toReadable(),
                    s.status.name,
                    s.source,
                    s.elapsedSeconds.toString(),
                    s.notes.orEmpty().csvEscape()
                ).joinToString(",") + "\n"
            )
        }
        writer.flush()
    }

    /**
     * Writes check-in history as CSV to [outputStream].
     * Columns: id, date, painScore, energyScore, bpiDomain, bpiScore, freeText
     */
    fun exportCheckIns(checkIns: List<CheckIn>, outputStream: OutputStream) {
        val writer = outputStream.writer()
        writer.write("id,date,painScore,energyScore,bpiDomain,bpiScore,freeText\n")
        checkIns.sortedByDescending { it.checkedInAt }.forEach { c ->
            writer.write(
                listOf(
                    c.id,
                    c.checkedInAt.toReadable(),
                    c.painScore?.toString().orEmpty(),
                    c.energyScore?.toString().orEmpty(),
                    c.bpiDomain.orEmpty().csvEscape(),
                    c.bpiScore?.toString().orEmpty(),
                    c.freeText.orEmpty().csvEscape()
                ).joinToString(",") + "\n"
            )
        }
        writer.flush()
    }
}
