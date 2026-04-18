package com.therapycompanion.ui.library

import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseDetailScreen(
    exerciseId: String,
    onEdit: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as TherapyCompanionApp
    var exercise by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(exerciseId) {
        exercise = withContext(Dispatchers.IO) {
            app.exerciseRepository.getExerciseById(exerciseId)
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
