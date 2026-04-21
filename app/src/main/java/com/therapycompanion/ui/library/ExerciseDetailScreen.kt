package com.therapycompanion.ui.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.data.model.Session
import com.therapycompanion.data.model.SessionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val scope = rememberCoroutineScope()
    var exercise by remember { mutableStateOf<Exercise?>(null) }

    // "I just did this" form state
    var logDate by remember { mutableStateOf(LocalDate.now()) }
    var isPartial by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var logSaved by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseId) {
        exercise = withContext(Dispatchers.IO) {
            app.exerciseRepository.getExerciseById(exerciseId)
        }
    }

    // Navigate back shortly after the saved confirmation appears
    LaunchedEffect(logSaved) {
        if (logSaved) {
            delay(1200)
            onBack()
        }
    }

    // Date picker — Material3 DatePickerDialog
    if (showDatePicker) {
        val todayUtcMillis = logDate
            .atStartOfDay(ZoneId.of("UTC"))
            .toInstant()
            .toEpochMilli()
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = todayUtcMillis,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis() + 86_400_000L
                override fun isSelectableYear(year: Int): Boolean =
                    year <= LocalDate.now().year
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        logDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.of("UTC"))
                            .toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.cd_edit_exercise)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        exercise?.let { ex ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(8.dp))

                if (ex.imageFileName != null) {
                    val file = File(context.filesDir, "exercise_images/${ex.imageFileName}")
                    AsyncImage(
                        model = file,
                        contentDescription = stringResource(R.string.cd_exercise_image, ex.name),
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9f),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(16.dp))
                }

                DetailRow(label = "Body System", value = ex.bodySystem)
                DetailRow(label = "Frequency", value = ex.frequency.displayName)
                DetailRow(label = "Scheduled Days", value = DayBits.toDisplayString(ex.scheduledDays))
                DetailRow(label = "Duration", value = "${ex.durationMinutes} min")
                DetailRow(label = "Priority", value = ex.priority.toString())
                DetailRow(label = "Active", value = if (ex.active) "Yes" else "No")

                Spacer(Modifier.height(16.dp))
                Text("Instructions", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text(ex.instructions, style = MaterialTheme.typography.bodyLarge)

                if (!ex.notes.isNullOrBlank()) {
                    Spacer(Modifier.height(16.dp))
                    Text("Notes", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        ex.notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                // ── "I just did this" card ────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "I just did this",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(12.dp))

                        // Date row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Date performed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = logDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            TextButton(onClick = { showDatePicker = true }) {
                                Text("Change")
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Done / Partial toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (isPartial) "Partial completion" else "Fully completed",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (isPartial) "Toggle off if you finished the whole exercise" else "Toggle on if you only did part of it",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPartial,
                                onCheckedChange = { isPartial = it }
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        if (logSaved) {
                            Text(
                                text = "Logged!",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            Button(
                                onClick = {
                                    scope.launch(Dispatchers.IO) {
                                        val performedAt = logDate
                                            .atStartOfDay(ZoneId.systemDefault())
                                            .toInstant()
                                            .toEpochMilli()
                                        val status = if (isPartial) SessionStatus.Partial else SessionStatus.Completed
                                        app.sessionRepository.insertSession(
                                            Session(
                                                id = UUID.randomUUID().toString(),
                                                exerciseId = exerciseId,
                                                startedAt = performedAt,
                                                completedAt = performedAt,
                                                elapsedSeconds = 0,
                                                status = status,
                                                notes = null,
                                                source = Session.SOURCE_ADHOC
                                            )
                                        )
                                        withContext(Dispatchers.Main) {
                                            logSaved = true
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save")
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
