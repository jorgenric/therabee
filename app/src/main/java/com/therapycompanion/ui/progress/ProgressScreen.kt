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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.CheckIn
import java.time.LocalDate
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.progress_title)) },
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
