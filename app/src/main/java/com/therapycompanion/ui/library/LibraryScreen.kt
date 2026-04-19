package com.therapycompanion.ui.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.data.model.DayBits
import com.therapycompanion.data.model.Exercise

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    contentPadding: PaddingValues,
    onExerciseClick: (String) -> Unit,
    onAddExercise: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val viewModel: LibraryViewModel = viewModel(
        factory = LibraryViewModel.Factory(app.exerciseRepository, app.sessionRepository)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.library_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddExercise) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.cd_add_exercise)
                )
            }
        },
        modifier = Modifier.padding(contentPadding)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ── Search bar ────────────────────────────────────────────────
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::setSearchQuery,
                placeholder = { Text("Search exercises…") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Filter chips ──────────────────────────────────────────────
            if (uiState.allBodySystems.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // "All" chip — clears body-system filter
                    FilterChip(
                        selected = uiState.bodySystemFilter == null && !uiState.filterNotDoneRecently,
                        onClick = viewModel::clearFilters,
                        label = { Text("All") }
                    )

                    // One chip per body system
                    uiState.allBodySystems.forEach { system ->
                        FilterChip(
                            selected = uiState.bodySystemFilter == system,
                            onClick = {
                                viewModel.setBodySystemFilter(
                                    if (uiState.bodySystemFilter == system) null else system
                                )
                            },
                            label = { Text(system) }
                        )
                    }

                    // "Not done recently" chip
                    FilterChip(
                        selected = uiState.filterNotDoneRecently,
                        onClick = { viewModel.setNotDoneRecentlyFilter(!uiState.filterNotDoneRecently) },
                        label = { Text("Not done recently") }
                    )
                }
                Spacer(Modifier.height(4.dp))
            }

            // ── List ──────────────────────────────────────────────────────
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.groupedExercises.isEmpty() -> {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            val hasFilter = uiState.searchQuery.isNotBlank() ||
                                uiState.bodySystemFilter != null ||
                                uiState.filterNotDoneRecently
                            Text(
                                if (hasFilter) "No exercises match" else stringResource(R.string.library_empty_title),
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (hasFilter) "Try a different search or filter."
                                else stringResource(R.string.library_empty_body),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        uiState.groupedExercises.forEach { (system, exercises) ->
                            val isExpanded = system in uiState.expandedSystems
                            item(key = "header_$system") {
                                BodySystemHeader(
                                    system = system,
                                    count = exercises.size,
                                    isExpanded = isExpanded,
                                    onToggle = { viewModel.toggleBodySystem(system) }
                                )
                            }
                            if (isExpanded) {
                                items(exercises, key = { it.id }) { exercise ->
                                    ExerciseListItem(
                                        exercise = exercise,
                                        onClick = { onExerciseClick(exercise.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BodySystemHeader(
    system: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$system ($count)",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Icon(
            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = MaterialTheme.colorScheme.primary
        )
    }
    Divider()
}

@Composable
private fun ExerciseListItem(exercise: Exercise, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(exercise.name) },
        supportingContent = {
            Text(
                "${exercise.frequency.displayName} · ${DayBits.toDisplayString(exercise.scheduledDays)} · P${exercise.priority}",
                style = MaterialTheme.typography.bodySmall
            )
        },
        trailingContent = {
            if (!exercise.active) {
                Text(
                    "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    )
    Divider(modifier = Modifier.padding(start = 16.dp))
}
