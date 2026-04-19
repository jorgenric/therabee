package com.therapycompanion.ui.settings

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.therapycompanion.R
import com.therapycompanion.TherapyCompanionApp
import com.therapycompanion.backup.MergeStrategy
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

    // Fire share intent when ready.
    LaunchedEffect(uiState.shareIntent) {
        val intent = uiState.shareIntent ?: return@LaunchedEffect
        context.startActivity(intent)
        viewModel.consumeShareIntent()
    }

    // File picker → save JSON backup.
    val today = LocalDate.now().toString()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(it) } }

    // File picker → open JSON backup for preview/restore.
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.previewRestore(it) } }

    // File picker → save session history CSV.
    val sessionCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportSessionsCsv(it) } }

    // File picker → save check-in history CSV.
    val checkInCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri -> uri?.let { viewModel.exportCheckInsCsv(it) } }

    // Reset dialog state.
    var showResetDialog by remember { mutableStateOf(false) }
    var resetText by remember { mutableStateOf("") }
    var hasExportedForReset by remember { mutableStateOf(false) }

    // Restore strategy picker dialog.
    if (uiState.restorePreview != null) {
        RestoreStrategyDialog(
            preview = uiState.restorePreview!!,
            onDismiss = viewModel::dismissRestorePreview,
            onConfirm = { strategy -> viewModel.confirmRestore(strategy) }
        )
    }

    // Reset progress confirmation dialog.
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false; resetText = "" },
            title = { Text("Reset progress") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "This permanently deletes all sessions and check-ins. " +
                        "Exercises and settings are kept.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (!hasExportedForReset) {
                        Text(
                            "Export a backup first to avoid data loss.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        OutlinedButton(
                            onClick = {
                                exportLauncher.launch("therapy_backup_before_reset_$today.json")
                                hasExportedForReset = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Export backup first") }
                    } else {
                        Text(
                            "Type \"reset\" to confirm:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        OutlinedTextField(
                            value = resetText,
                            onValueChange = { resetText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetProgress()
                        showResetDialog = false
                        resetText = ""
                        hasExportedForReset = false
                    },
                    enabled = hasExportedForReset && resetText.trim().lowercase() == "reset",
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Reset") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false; resetText = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

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

            // ── Before-update backup reminder banner ───────────────────────
            if (uiState.showBackupReminder) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Back up before updating",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Text(
                                    "You have new data since your last backup. " +
                                    "Export a backup before installing an app update.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.exportAndShare() }
                                    ) { Text("Export now") }
                                    TextButton(
                                        onClick = { viewModel.dismissBackupReminder() }
                                    ) { Text("Dismiss") }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Profile ────────────────────────────────────────────────────
            item { SectionHeader("Profile") }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = settings.displayName,
                        onValueChange = { viewModel.updateDisplayName(it) },
                        label = { Text("Your name") },
                        placeholder = { Text("Shown in the greeting on Home") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Divider()
            }

            // ── Appearance ─────────────────────────────────────────────────
            item { SectionHeader("Appearance") }
            item {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        "Theme",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    listOf("System", "Light", "Dark").forEach { mode ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.updateThemeMode(mode) }
                            )
                            Text(mode, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Divider()
            }

            // ── Daily load ─────────────────────────────────────────────────
            item { SectionHeader("Exercise Load") }
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

            item {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.settings_show_streaks)) },
                    supportingContent = { Text("Show your consecutive-day exercise streak on Progress") },
                    trailingContent = {
                        Switch(
                            checked = settings.showStreaks,
                            onCheckedChange = viewModel::updateShowStreaks
                        )
                    }
                )
                Divider()
            }

            // ── Data / Backup ──────────────────────────────────────────────
            item { SectionHeader("Data") }
            item {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // CSV import
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

                    // JSON export via share sheet (writes local copy too)
                    Button(
                        onClick = { viewModel.exportAndShare() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text(
                            "Share backup",
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    // JSON export to file picker
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

                    // JSON restore
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

                    Text(
                        stringResource(R.string.settings_backup_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    // CSV exports (view-only)
                    Text(
                        "Export history (CSV, view-only)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { sessionCsvLauncher.launch("sessions_$today.csv") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Sessions") }
                        OutlinedButton(
                            onClick = { checkInCsvLauncher.launch("checkins_$today.csv") },
                            modifier = Modifier.weight(1f)
                        ) { Text("Check-ins") }
                    }

                    Divider()

                    // Reset progress
                    OutlinedButton(
                        onClick = { showResetDialog = true; hasExportedForReset = false },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) { Text("Reset progress…") }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

// ── Restore strategy picker dialog ────────────────────────────────────────────

@Composable
private fun RestoreStrategyDialog(
    preview: RestorePreview,
    onDismiss: () -> Unit,
    onConfirm: (MergeStrategy) -> Unit
) {
    var selected by remember { mutableStateOf(MergeStrategy.Replace) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore backup") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "File contains ${preview.exercises} exercises, " +
                    "${preview.sessions} sessions, ${preview.checkIns} check-ins.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                Text("How should existing data be handled?",
                    style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))

                StrategyRow(
                    label = "Replace — wipe local data, use file",
                    selected = selected == MergeStrategy.Replace,
                    onClick = { selected = MergeStrategy.Replace }
                )
                StrategyRow(
                    label = "Merge — keep both (local wins on conflict)",
                    selected = selected == MergeStrategy.MergeKeepBoth,
                    onClick = { selected = MergeStrategy.MergeKeepBoth }
                )
                StrategyRow(
                    label = "Merge — file exercises win, combine sessions",
                    selected = selected == MergeStrategy.MergePreferFile,
                    onClick = { selected = MergeStrategy.MergePreferFile }
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selected) }) { Text("Restore") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun StrategyRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        RadioButton(selected = selected, onClick = onClick)
        Text(label, style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 4.dp))
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
