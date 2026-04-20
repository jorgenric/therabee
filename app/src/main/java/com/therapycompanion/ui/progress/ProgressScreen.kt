package com.therapycompanion.ui.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import android.widget.Toast
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.CheckIn
import com.therapycompanion.ui.checkin.CheckInBottomSheet
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(contentPadding: PaddingValues) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val viewModel: ProgressViewModel = viewModel(
        factory = ProgressViewModel.Factory(
            app.sessionRepository,
            app.checkInRepository,
            app.exerciseRepository,
            app.userSettingsRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    // Manual check-in sheet — user can open at any time from this screen.
    if (uiState.showManualCheckIn) {
        CheckInBottomSheet(
            onDismiss = { viewModel.dismissManualCheckIn() },
            onSubmit = { painScore, energyScore, bpiDomain, bpiScore, freeText ->
                viewModel.submitManualCheckIn(painScore, energyScore, bpiDomain, bpiScore, freeText)
            }
        )
    }

    // Humble Brag overlay — progress summary card.
    HumbleBragOverlay(
        state = uiState.humbleBragState,
        displayName = uiState.displayName,
        currentStreak = uiState.currentStreak,
        showStreaks = uiState.showStreaks,
        exercisesThisWeek = uiState.exercisesThisWeek,
        lifetimeCompletedCount = uiState.lifetimeCompletedCount,
        bodySystemsLast7Days = uiState.bodySystemsLast7Days,
        onDismiss = { viewModel.dismissHumbleBrag() },
        onRegenerate = { viewModel.generateHumbleBrag() }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.progress_title)) },
            actions = {
                IconButton(onClick = { viewModel.generateHumbleBrag() }) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = "Humble Brag"
                    )
                }
                IconButton(onClick = { viewModel.openManualCheckIn() }) {
                    Icon(
                        Icons.Filled.Mood,
                        contentDescription = "Check in"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // ── Exercises this week ───────────────────────────────────
                item {
                    WeekSummary(count = uiState.exercisesThisWeek)
                }

                // ── Calendar view ────────────────────────────────────────
                item {
                    CalendarSection(
                        selectedMonth = uiState.selectedMonth,
                        sessionDates = viewModel.sessionDatesInMonth(),
                        onPreviousMonth = { viewModel.selectMonth(uiState.selectedMonth.minusMonths(1)) },
                        onNextMonth = { viewModel.selectMonth(uiState.selectedMonth.plusMonths(1)) }
                    )
                }

                // ── Streak (only when enabled in Settings) ───────────────
                if (uiState.showStreaks && uiState.currentStreak > 0) {
                    item {
                        StreakBadge(streak = uiState.currentStreak)
                    }
                    if (isStreakMilestone(uiState.currentStreak)) {
                        item {
                            StreakMilestoneBrag(
                                streak = uiState.currentStreak,
                                onHumbleBrag = { viewModel.generateHumbleBrag() }
                            )
                        }
                    }
                }

                // ── Body system coverage ──────────────────────────────────
                item {
                    BodySystemCoverage(
                        allSystems = uiState.allBodySystems,
                        covered = uiState.coveredBodySystems
                    )
                }

                // ── Pain / Energy trend chart ─────────────────────────────
                item {
                    if (uiState.checkIns.isNotEmpty()) {
                        TrendChart(checkIns = uiState.checkIns)
                    }
                }

                // ── Session history log ───────────────────────────────────
                val history = uiState.sessions
                    .filter { it.status == SessionStatus.Completed || it.status == SessionStatus.Skipped }
                    .sortedByDescending { it.startedAt }

                if (history.isNotEmpty()) {
                    item {
                        Text(
                            "Session History",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(history, key = { it.id }) { session ->
                        SessionHistoryItem(
                            session = session,
                            exerciseName = uiState.exerciseNamesById[session.exerciseId] ?: "Unknown"
                        )
                    }
                }
            }
        }
    }
}

// ── Humble Brag overlay ───────────────────────────────────────────────────────

@Composable
private fun HumbleBragOverlay(
    state: HumbleBragState,
    displayName: String,
    currentStreak: Int,
    showStreaks: Boolean,
    exercisesThisWeek: Int,
    lifetimeCompletedCount: Int,
    bodySystemsLast7Days: List<String>,
    onDismiss: () -> Unit,
    onRegenerate: () -> Unit
) {
    if (state !is HumbleBragState.Ready) return

    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = if (displayName.isNotBlank()) "$displayName's Progress" else "Your Progress",
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                // Stats block
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatRow(label = "Sessions this week", value = exercisesThisWeek.toString())
                    StatRow(label = "Lifetime sessions", value = lifetimeCompletedCount.toString())
                    if (showStreaks && currentStreak > 0) {
                        val days = if (currentStreak == 1) "day" else "days"
                        StatRow(label = "Current streak", value = "$currentStreak $days")
                    }
                    if (bodySystemsLast7Days.isNotEmpty()) {
                        StatRow(
                            label = "Body systems this week",
                            value = bodySystemsLast7Days.joinToString(", ")
                        )
                    }
                }

                // Divider
                androidx.compose.material3.HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                // Encouraging phrase
                Text(
                    text = state.phrase,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val copyText = buildString {
                                if (displayName.isNotBlank()) appendLine("$displayName's Progress")
                                else appendLine("My Progress")
                                appendLine("• Sessions this week: $exercisesThisWeek")
                                appendLine("• Lifetime sessions: $lifetimeCompletedCount")
                                if (showStreaks && currentStreak > 0) {
                                    val days = if (currentStreak == 1) "day" else "days"
                                    appendLine("• Streak: $currentStreak $days")
                                }
                                if (bodySystemsLast7Days.isNotEmpty()) {
                                    appendLine("• Body systems: ${bodySystemsLast7Days.joinToString(", ")}")
                                }
                                appendLine()
                                append(state.phrase)
                            }
                            clipboardManager.setText(AnnotatedString(copyText))
                            Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("Copy")
                    }
                    OutlinedButton(
                        onClick = onRegenerate,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text("New quote")
                    }
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Dismiss")
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ── Streak milestone prompt ───────────────────────────────────────────────────

private fun isStreakMilestone(streak: Int): Boolean =
    streak in setOf(3, 7, 14, 21, 30) || (streak > 30 && streak % 30 == 0)

@Composable
private fun StreakMilestoneBrag(streak: Int, onHumbleBrag: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "$streak days — that's worth sharing.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            TextButton(onClick = onHumbleBrag) {
                Text("Brag a little")
            }
        }
    }
}

// ── Calendar ──────────────────────────────────────────────────────────────────

@Composable
private fun CalendarSection(
    selectedMonth: LocalDate,
    sessionDates: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())

    Column {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
            }
            Text(
                text = selectedMonth.format(monthFormatter),
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
            }
        }

        // Day headers
        val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // Calendar grid
        val firstDay = selectedMonth.withDayOfMonth(1)
        val startOffset = (firstDay.dayOfWeek.value - 1) // Monday = 0
        val daysInMonth = selectedMonth.lengthOfMonth()
        val totalCells = startOffset + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayNum = row * 7 + col - startOffset + 1
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..daysInMonth) {
                            val date = selectedMonth.withDayOfMonth(dayNum)
                            val hasSession = date in sessionDates
                            val isToday = date == LocalDate.now()

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        when {
                                            hasSession -> MaterialTheme.colorScheme.primary
                                            isToday -> MaterialTheme.colorScheme.primaryContainer
                                            else -> Color.Transparent
                                        },
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = dayNum.toString(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        hasSession -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Streak badge ─────────────────────────────────────────────────────────────

@Composable
private fun StreakBadge(streak: Int) {
    val days = if (streak == 1) "day" else "days"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "🔥",
            style = MaterialTheme.typography.headlineSmall
        )
        Column {
            Text(
                text = "$streak $days in a row",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Keep it going — one day at a time.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Body system coverage ──────────────────────────────────────────────────────

@Composable
private fun BodySystemCoverage(allSystems: List<String>, covered: Set<String>) {
    if (allSystems.isEmpty()) return
    Column {
        Text(
            "Body Systems This Week",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        allSystems.forEach { system ->
            val isCovered = covered.any { it.equals(system, ignoreCase = true) }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            if (isCovered) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
                Text(
                    text = "  $system",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCovered) MaterialTheme.colorScheme.onSurface
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Trend chart — native Canvas, no third-party library ──────────────────────

@Composable
private fun TrendChart(checkIns: List<CheckIn>) {
    val painColor = MaterialTheme.colorScheme.error
    val energyColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Column {
        Text(
            "Pain & Energy (30 days)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))

        // Legend
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            LegendItem(color = painColor, label = "Pain")
            LegendItem(color = energyColor, label = "Energy")
        }

        Spacer(Modifier.height(8.dp))

        val scored = checkIns.filter { it.hasScores }.sortedBy { it.checkedInAt }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
        ) {
            if (scored.size < 2) return@Canvas

            val w = size.width
            val h = size.height
            val padding = 8.dp.toPx()
            val chartW = w - padding * 2
            val chartH = h - padding * 2

            // Grid lines at 0, 5, 10
            listOf(0f, 5f, 10f).forEach { value ->
                val y = padding + chartH - (value / 10f) * chartH
                drawLine(gridColor, Offset(padding, y), Offset(w - padding, y), strokeWidth = 1.dp.toPx())
            }

            fun xFor(index: Int) = padding + (index.toFloat() / (scored.size - 1)) * chartW
            fun yFor(score: Int) = padding + chartH - (score.toFloat() / 10f) * chartH

            // Draw pain line
            val painPath = Path()
            scored.forEachIndexed { i, ci ->
                val x = xFor(i)
                val y = yFor(ci.painScore ?: 0)
                if (i == 0) painPath.moveTo(x, y) else painPath.lineTo(x, y)
            }
            drawPath(painPath, painColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // Draw energy line
            val energyPath = Path()
            scored.forEachIndexed { i, ci ->
                val x = xFor(i)
                val y = yFor(ci.energyScore ?: 0)
                if (i == 0) energyPath.moveTo(x, y) else energyPath.lineTo(x, y)
            }
            drawPath(energyPath, energyColor, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))

            // Dots
            scored.forEachIndexed { i, ci ->
                drawCircle(painColor, radius = 4.dp.toPx(), center = Offset(xFor(i), yFor(ci.painScore ?: 0)))
                drawCircle(energyColor, radius = 4.dp.toPx(), center = Offset(xFor(i), yFor(ci.energyScore ?: 0)))
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            " $label",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Week summary ──────────────────────────────────────────────────────────────

@Composable
private fun WeekSummary(count: Int) {
    val exercises = if (count == 1) "exercise" else "exercises"
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = "$exercises this week",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = if (count == 0) "Nothing logged yet — you've got this."
                       else "Keep the momentum going.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Session history item ──────────────────────────────────────────────────────

@Composable
private fun SessionHistoryItem(session: Session, exerciseName: String) {
    val zoneId = ZoneId.systemDefault()
    val date = Instant.ofEpochMilli(session.startedAt)
        .atZone(zoneId)
        .toLocalDate()
    val dateLabel = date.format(DateTimeFormatter.ofPattern("MMM d · EEE", Locale.getDefault()))

    val isCompleted = session.status == SessionStatus.Completed
    val durationLabel = if (isCompleted && session.elapsedSeconds > 0) {
        val mins = session.elapsedSeconds / 60
        val secs = session.elapsedSeconds % 60
        if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
    } else {
        null
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exerciseName,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = if (durationLabel != null) "$dateLabel · $durationLabel"
                       else dateLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = if (isCompleted) "Done" else "Skipped",
            style = MaterialTheme.typography.labelSmall,
            color = if (isCompleted) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
