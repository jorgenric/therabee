package com.therapycompanion.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room entity for FPS-R pain / energy check-ins.
 *
 * Kept as a separate table (not embedded in Session) so the schema can
 * grow independently to accommodate expanded symptom tracking in v2+.
 *
 * Pain score follows the FPS-R instrument: 0, 2, 4, 6, 8, or 10.
 * Energy score is a simple 0–10 scale, ascending = more energy.
 *
 * The BPI (Brief Pain Inventory) domain rotates by day of week.
 * Layer 2 is always optional — dismissed may be true with null scores.
 */
@Entity(
    tableName = "check_ins",
    indices = [Index(value = ["checked_in_at"])]
)
data class CheckInEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    /** UTC epoch ms when the check-in was initiated */
    @ColumnInfo(name = "checked_in_at")
    val checkedInAt: Long = System.currentTimeMillis(),

    /** FPS-R pain score: 0, 2, 4, 6, 8, or 10. Null if dismissed before scoring. */
    @ColumnInfo(name = "pain_score")
    val painScore: Int? = null,

    /** Energy level 0–10. Null if dismissed before scoring. */
    @ColumnInfo(name = "energy_score")
    val energyScore: Int? = null,

    /** Which BPI domain was asked on this day (rotates by day of week) */
    @ColumnInfo(name = "bpi_domain")
    val bpiDomain: String? = null,

    /** BPI score for the domain, 0–10. Null if user skipped layer 2. */
    @ColumnInfo(name = "bpi_score")
    val bpiScore: Int? = null,

    /** Free-text note from layer 2. Null if user skipped. */
    @ColumnInfo(name = "free_text")
    val freeText: String? = null,

    /** True if user dismissed the check-in without scoring */
    @ColumnInfo(name = "dismissed")
    val dismissed: Boolean = false
)
