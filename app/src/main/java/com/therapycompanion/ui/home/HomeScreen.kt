package com.therapycompanion.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.SessionStatus
import com.therapycompanion.ui.checkin.CheckInBottomSheet
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    contentPadding: PaddingValues,
    onStartSession: (exerciseId: String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val acknowledgmentMessages = stringArrayResource(R.array.acknowledgment_messages).toList()
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModel.Factory(
            app.exerciseRepository,
            app.sessionRepository,
            app.userSettingsRepository,
            app.checkInRepository,
            acknowledgmentMessages
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    // Show check-in bottom sheet when conditions are met.
    if (uiState.showCheckInPrompt) {
        CheckInBottomSheet(
            onDismiss = { viewModel.dismissCheckInPrompt() },
            onSubmit = { painScore, energyScore, bpiDomain, bpiScore, freeText ->
                viewModel.submitCheckIn(painScore, energyScore, bpiDomain, bpiScore, freeText)
            }
        )
    }

    // Show acknowledgment message as a Snackbar after a session is completed.
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.acknowledgmentMessage) {
        val message = uiState.acknowledgmentMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        viewModel.dismissAcknowledgment()
    }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.home_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = uiState.currentDate.format(
                                DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault())
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDailyList() }) {
                        Icon(
                            Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.cd_refresh)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            EasierDayToggle(
                enabled = uiState.settings.easierDayEnabled,
                onToggle = { viewModel.toggleEasierDay(it) }
            )

            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                uiState.todaysExercises.isEmpty() -> {
                    EmptyTodayState()
                }
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.todaysExercises, key = { it.exercise.id }) { item ->
                            ExerciseCard(
                                item = item,
                                onStart = { onStartSession(item.exercise.id) },
                                onSkip = { viewModel.markSkipped(item.exercise.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EasierDayToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Surface(
        color = if (enabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.easier_day_label),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(R.string.easier_day_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun ExerciseCard(
    item: ExerciseWithStatus,
    onStart: () -> Unit,
    onSkip: () -> Unit
) {
    val isCompleted = item.status == SessionStatus.Completed
    val isSkipped = item.status == SessionStatus.Skipped

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(
            containerColor = when {
                isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                isSkipped -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.exercise.bodySystem} · P${item.exercise.priority}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.exercise.name,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (isCompleted || isSkipped) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${item.exercise.durationMinutes} min · ${item.exercise.frequency.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isCompleted) {
                    Text(
                        text = stringResource(R.string.session_status_completed),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                } else if (isSkipped) {
                    Text(
                        text = stringResource(R.string.session_status_skipped),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isCompleted && !isSkipped) {
                FilledTonalIconButton(
                    onClick = onStart,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        contentDescription = stringResource(R.string.cd_start_exercise, item.exercise.name)
                    )
                }
                IconButton(
                    onClick = onSkip,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.cd_skip_exercise, item.exercise.name),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else if (isCompleted) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTodayState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = stringResource(R.string.home_empty_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_empty_body),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
