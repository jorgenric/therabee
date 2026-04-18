package com.therapycompanion.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.Exercise
import com.therapycompanion.import.CsvImporter
import com.therapycompanion.import.ImportResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private sealed class ImportState {
    object Idle : ImportState()
    object Loading : ImportState()
    data class Preview(val exercises: List<Exercise>) : ImportState()
    data class Errors(val errors: List<com.therapycompanion.import.RowError>) : ImportState()
    data class Complete(val count: Int, val systemCount: Int) : ImportState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val scope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    var importState by remember { mutableStateOf<ImportState>(ImportState.Idle) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult

        importState = ImportState.Loading
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                CsvImporter.parse(context, uri)
            }
            importState = when (result) {
                is ImportResult.Success -> ImportState.Preview(result.exercises)
                is ImportResult.ValidationErrors -> ImportState.Errors(result.errors)
                is ImportResult.FileError -> {
                    snackbarState.showSnackbar(result.message)
                    ImportState.Idle
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.import_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            when (val state = importState) {
                is ImportState.Idle -> {
                    Text(
                        stringResource(R.string.import_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_choose_file))
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val file = CsvImporter.exportTemplate(context)
                                withContext(Dispatchers.Main) {
                                    if (file != null) {
                                        snackbarState.showSnackbar("Template saved: ${file.name}")
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_download_template))
                    }
                }

                is ImportState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ImportState.Preview -> {
                    val systemCount = state.exercises.map { it.bodySystem }.toSet().size
                    Text(
                        "Found ${state.exercises.size} exercises across $systemCount body systems.",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.exercises) { ex ->
                            Text(
                                "• ${ex.name} (${ex.bodySystem})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { importState = ImportState.Idle },
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancel") }
                        Button(
                            onClick = {
                                scope.launch {
                                    withContext(Dispatchers.IO) {
                                        // Check for duplicates and insert new exercises
                                        var inserted = 0
                                        state.exercises.forEach { exercise ->
                                            val existing = app.exerciseRepository.getExerciseByName(exercise.name)
                                            if (existing == null) {
                                                app.exerciseRepository.insertExercise(exercise)
                                                inserted++
                                            }
                                            // Duplicates are skipped by default — user can edit individually
                                        }
                                        val systemCount = state.exercises.map { it.bodySystem }.toSet().size
                                        withContext(Dispatchers.Main) {
                                            importState = ImportState.Complete(inserted, systemCount)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(stringResource(R.string.import_confirm)) }
                    }
                }

                is ImportState.Errors -> {
                    Text(
                        "Fix these errors and try again:",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(state.errors) { error ->
                            Text(
                                error.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { importState = ImportState.Idle },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Try Again") }
                }

                is ImportState.Complete -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "Imported ${state.count} exercises across ${state.systemCount} body systems.",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = onBack) { Text("Done") }
                    }
                }
            }
        }
    }
}
