package com.therapycompanion.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    contentPadding: PaddingValues,
    onImportClick: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    val viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            app,
            app.userSettingsRepository,
            app.exerciseRepository,
            app.sessionRepository,
            app.checkInRepository
        )
    )
    val uiState by viewModel.uiState.collectAsState()
    val settings = uiState.settings

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.backupMessage) {
        val msg = uiState.backupMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        viewModel.dismissBackupMessage()
    }

    // Export: system file picker creates the destination file, returns a URI we write to.
    val today = LocalDate.now().toString()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(it) } }

    // Restore: user picks an existing backup file.
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.restoreFrom(it) } }

    Scaffold(
        modifier = Modifier.padding(contentPadding),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // ── Daily load ─────────────────────────────────────────────────
            item {
                SectionHeader("Exercise Load")
            }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Daily exercises: ${settings.dailyLoad}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = settings.dailyLoad.toFloat(),
                        onValueChange = { viewModel.updateDailyLoad(it.toInt()) },
                        valueRange = 1f..10f,
                        steps = 8
                    )
                    Text(
                        "1 = lightest · 10 = full program",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Divider()
            }

            // ── Notifications ──────────────────────────────────────────────
            item { SectionHeader("Notifications") }

            item {
                NotificationRow(
                    title = stringResource(R.string.settings_morning_reminder),
                    subtitle = settings.morningReminderTime,
                    enabled = settings.morningReminderEnabled,
                    onToggle = viewModel::updateMorningReminderEnabled,
                    onTimeChange = viewModel::updateMorningReminderTime
                )
                Divider()
            }

            item {
                NotificationRow(
                    title = stringResource(R.string.settings_afternoon_checkin),
                    subtitle = settings.afternoonCheckInTime,
                    enabled = settings.afternoonCheckInEnabled,
                    onToggle = viewModel::updateAfternoonCheckInEnabled,
                    onTimeChange = viewModel::updateAfternoonCheckInTime
                )
                Divider()
            }

            item {
                NotificationRow(
                    title = stringResource(R.string.settings_evening_encouragement),
                    subtitle = settings.eveningEncouragementTime,
                    enabled = settings.eveningEncouragementEnabled,
                    onToggle = viewModel::updateEveningEncouragementEnabled,
                    onTimeChange = viewModel::updateEveningEncouragementTime
                )
                Divider()
            }

            // ── Quiet hours ────────────────────────────────────────────────
            item { SectionHeader("Quiet Hours") }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Notifications are silenced during quiet hours.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = settings.quietHoursStart ?: "",
                            onValueChange = { viewModel.updateQuietHoursStart(it.ifBlank { null }) },
                            label = { Text("Start (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Text("  to  ", style = MaterialTheme.typography.bodyMedium)
                        OutlinedTextField(
                            value = settings.quietHoursEnd ?: "",
                            onValueChange = { viewModel.updateQuietHoursEnd(it.ifBlank { null }) },
                            label = { Text("End (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
                Divider()
            }

            // ── Check-ins ──────────────────────────────────────────────────
            item { SectionHeader("Check-Ins") }
            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_check_ins_enabled)) },
                    supportingContent = { Text("FPS-R pain and energy check-in after each session") },
                    trailingContent = {
                        Switch(
                            checked = settings.checkInsEnabled,
                            onCheckedChange = viewModel::updateCheckInsEnabled
                        )
                    }
                )
                Divider()
            }

            // ── Import / Backup ────────────────────────────────────────────
            item { SectionHeader("Data") }
            item {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onImportClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Upload, contentDescription = null)
                        Text(
                            stringResource(R.string.settings_import_exercises),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { exportLauncher.launch("therapy_backup_$today.json") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null)
                        Text(
                            stringResource(R.string.settings_export_backup),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.FileDownload, contentDescription = null)
                        Text(
                            stringResource(R.string.settings_restore_backup),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        stringResource(R.string.settings_backup_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
    )
}

@Composable
private fun NotificationRow(
    title: String,
    subtitle: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium)
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
        if (enabled) {
            OutlinedTextField(
                value = subtitle,
                onValueChange = onTimeChange,
                label = { Text("Time (HH:mm)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = enabled
            )
        }
    }
}
