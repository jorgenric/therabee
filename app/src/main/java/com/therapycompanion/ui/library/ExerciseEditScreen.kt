package com.therapycompanion.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Frequency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExerciseEditScreen(
    exerciseId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val viewModel: ExerciseEditViewModel = viewModel(
        factory = ExerciseEditViewModel.Factory(exerciseId, app.exerciseRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onSaved()
    }

    // Photo picker — no storage permission needed on Android 13+
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val targetExerciseId = exerciseId ?: "new_${System.currentTimeMillis()}"
            val destFile = File(context.filesDir, "exercise_images/$targetExerciseId.jpg")
            destFile.parentFile?.mkdirs()

            try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    destFile.outputStream().use { output -> input.copyTo(output) }
                }
                viewModel.updateImageFileName(destFile.name)
            } catch (_: Exception) { /* IO error — ignore, image not updated */ }
        }
    }

    val title = if (exerciseId == null)
        stringResource(R.string.exercise_edit_title_new)
    else
        stringResource(R.string.exercise_edit_title_edit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Name
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text(stringResource(R.string.exercise_field_name)) },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Body System — free text with autocomplete from existing library values
            BodySystemAutocomplete(
                value = uiState.bodySystem,
                allSuggestions = uiState.allBodySystems,
                onValueChange = viewModel::updateBodySystem,
                isError = uiState.bodySystemError != null,
                supportingText = uiState.bodySystemError?.let { { Text(it) } }
            )

            // Frequency dropdown
            FrequencyDropdown(
                selected = uiState.frequency,
                onSelected = viewModel::updateFrequency
            )

            // Scheduled days chips
            Text("Scheduled Days", style = MaterialTheme.typography.labelLarge)
            DayChips(
                selectedDays = uiState.scheduledDays,
                onDaysChanged = viewModel::updateScheduledDays
            )

            // Duration
            OutlinedTextField(
                value = uiState.durationMinutes,
                onValueChange = viewModel::updateDuration,
                label = { Text(stringResource(R.string.exercise_field_duration)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Priority
            Text("Priority: ${uiState.priority}", style = MaterialTheme.typography.labelLarge)
            Slider(
                value = uiState.priority.toFloat(),
                onValueChange = { viewModel.updatePriority(it.toInt()) },
                valueRange = 1f..3f,
                steps = 1
            )
            Text(
                "1 = highest (always included first) · 3 = lowest",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Instructions
            OutlinedTextField(
                value = uiState.instructions,
                onValueChange = viewModel::updateInstructions,
                label = { Text(stringResource(R.string.exercise_field_instructions)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 8
            )

            // Notes (optional)
            OutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text(stringResource(R.string.exercise_field_notes)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            // Active toggle
            Column {
                Text("Active", style = MaterialTheme.typography.labelLarge)
                Switch(
                    checked = uiState.active,
                    onCheckedChange = viewModel::updateActive
                )
            }

            // Photo
            Column {
                Text("Exercise Photo", style = MaterialTheme.typography.labelLarge)
                if (uiState.imageFileName != null) {
                    Text(
                        "Photo: ${uiState.imageFileName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                ) {
                    Text(
                        if (uiState.imageFileName == null)
                            stringResource(R.string.exercise_add_photo)
                        else
                            stringResource(R.string.exercise_change_photo)
                    )
                }
            }

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isSaving
            ) {
                Text(stringResource(R.string.exercise_save))
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

/**
 * Body system text field with autocomplete suggestions drawn from the existing library.
 *
 * - Accepts any free-text input — suggestions are for convenience only.
 * - Suggestions are filtered case-insensitively as the user types.
 * - Exact matches are excluded from the suggestion list (already typed correctly).
 * - Selecting a suggestion replaces the field value and closes the menu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BodySystemAutocomplete(
    value: String,
    allSuggestions: List<String>,
    onValueChange: (String) -> Unit,
    isError: Boolean = false,
    supportingText: (@Composable () -> Unit)? = null
) {
    val suggestions = remember(value, allSuggestions) {
        if (value.isBlank()) emptyList()
        else allSuggestions.filter {
            it.contains(value.trim(), ignoreCase = true) &&
            !it.equals(value.trim(), ignoreCase = true)
        }
    }
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text("Body System") },
            isError = isError,
            supportingText = supportingText,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            singleLine = true
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FrequencyDropdown(selected: Frequency, onSelected: (Frequency) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Frequency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Frequency.entries.forEach { freq ->
                DropdownMenuItem(
                    text = { Text(freq.displayName) },
                    onClick = { onSelected(freq); expanded = false }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DayChips(selectedDays: Int, onDaysChanged: (Int) -> Unit) {
    val days = listOf(
        "Mon" to DayBits.MON,
        "Tue" to DayBits.TUE,
        "Wed" to DayBits.WED,
        "Thu" to DayBits.THU,
        "Fri" to DayBits.FRI,
        "Sat" to DayBits.SAT,
        "Sun" to DayBits.SUN
    )
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        days.forEach { (label, bit) ->
            FilterChip(
                selected = (selectedDays and bit) != 0,
                onClick = {
                    val newDays = if ((selectedDays and bit) != 0) {
                        selectedDays and bit.inv()
                    } else {
                        selectedDays or bit
                    }
                    onDaysChanged(newDays)
                },
                label = { Text(label) }
            )
        }
    }
}
