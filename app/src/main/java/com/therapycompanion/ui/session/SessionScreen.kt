package com.therapycompanion.ui.session

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    exerciseId: String,
    onCancel: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onStartNext: (exerciseId: String) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val acknowledgmentMessages = stringArrayResource(R.array.acknowledgment_messages).toList()
    val viewModel: SessionViewModel = viewModel(
        factory = SessionViewModel.Factory(
            exerciseId,
            app.exerciseRepository,
            app.sessionRepository,
            acknowledgmentMessages
        )
    )
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val exercise = uiState.exercise ?: return

    if (uiState.showAcknowledgment) {
        AcknowledgmentScreen(
            exerciseName = exercise.name,
            message = uiState.acknowledgmentMessage,
            nextExerciseName = uiState.nextExerciseName,
            onStartNext = { uiState.nextExerciseId?.let(onStartNext) },
            onDone = onDone
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise.bodySystem) },
                navigationIcon = {
                    // X = Pause/cancel — returns Home with no session record written
                    IconButton(onClick = {
                        viewModel.cancelSession()
                        onCancel()
                    }) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_close)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))

            Text(
                text = exercise.name,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${exercise.durationMinutes} min · ${exercise.frequency.displayName}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(24.dp))

            // Optional user-activated countdown timer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.timerStarted) {
                    val minutes = uiState.remainingSeconds / 60
                    val seconds = uiState.remainingSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                }
                OutlinedButton(onClick = { viewModel.toggleTimer() }) {
                    Icon(
                        imageVector = if (uiState.timerActive) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = when {
                            !uiState.timerStarted -> "Start timer"
                            uiState.timerActive -> "Pause"
                            else -> "Resume"
                        }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Exercise image if available
            if (exercise.imageFileName != null) {
                val imageFile = File(context.filesDir, "exercise_images/${exercise.imageFileName}")
                AsyncImage(
                    model = imageFile,
                    contentDescription = stringResource(R.string.cd_exercise_image, exercise.name),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Fit
                )
                Spacer(Modifier.height(24.dp))
            }

            Text(
                text = stringResource(R.string.session_instructions_label),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = exercise.instructions,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            if (!exercise.notes.isNullOrBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.session_notes_label),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = exercise.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.markSkipped()
                        onSkip()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.session_skip))
                }
                Button(
                    onClick = { viewModel.markComplete() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Text(
                        text = stringResource(R.string.session_done),
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AcknowledgmentScreen(
    exerciseName: String,
    message: String,
    nextExerciseName: String?,
    onStartNext: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = exerciseName,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (nextExerciseName != null) {
            Spacer(Modifier.height(40.dp))

            Text(
                text = "Next up",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = nextExerciseName,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            FilledTonalButton(
                onClick = onStartNext,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start now")
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onDone,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to today")
        }
    }
}
