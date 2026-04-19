package com.therapycompanion.ui.checkin

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.therapycompanion.R
import com.therapycompanion.data.model.BpiDomains
import java.time.LocalDate

/**
 * FPS-R check-in bottom sheet.
 *
 * Layer 1 (required if user engages): FPS-R pain score + energy score
 * Layer 2 (always optional): BPI domain question + free text
 *
 * Clinical note from §11: FPS-R faces must be NEUTRAL (no tears, no smiling).
 * The six faces are represented here as simple emoji-free descriptions since
 * actual FPS-R vector assets require a separate licensing confirmation step.
 * Replace FpsFace composable with licensed VectorDrawable assets before release.
 *
 * The BPI domain rotates by day of week.
 * Layer 2 is always optional — dismiss with "No thanks" at any point.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (painScore: Int, energyScore: Int, bpiDomain: String?, bpiScore: Int?, freeText: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showLayer2 by remember { mutableStateOf(false) }
    var painScore by remember { mutableIntStateOf(0) }
    var energyScore by remember { mutableIntStateOf(0) }
    var bpiScore by remember { mutableFloatStateOf(5f) }
    var freeText by remember { mutableStateOf("") }

    val bpiDomain = BpiDomains.forDayOfWeek(LocalDate.now().dayOfWeek)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!showLayer2) {
                // ── Layer 1: FPS-R pain + energy ─────────────────────────────
                Text(
                    stringResource(R.string.checkin_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.checkin_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Pain scale — FPS-R
                Text(
                    stringResource(R.string.checkin_pain_label),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(8.dp))
                FpsrScale(selected = painScore, onSelect = { painScore = it })

                Spacer(Modifier.height(24.dp))

                // Energy scale — 6-point illustrated, matching FPS-R structure
                Text(
                    stringResource(R.string.checkin_energy_label),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(Modifier.height(8.dp))
                EnergyScale(selected = energyScore, onSelect = { energyScore = it })

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.checkin_dismiss)) }

                    Button(
                        onClick = { showLayer2 = true },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.checkin_next)) }
                }

                TextButton(
                    onClick = { onSubmit(painScore, energyScore, null, null, null) }
                ) { Text(stringResource(R.string.checkin_skip_layer2)) }

            } else {
                // ── Layer 2: BPI domain (optional) ───────────────────────────
                Text(
                    stringResource(R.string.checkin_layer2_title),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "How much has pain interfered with your $bpiDomain in the past 24 hours?",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(16.dp))

                Text("0 = no interference · 10 = completely interferes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = bpiScore,
                    onValueChange = { bpiScore = it },
                    valueRange = 0f..10f,
                    steps = 9
                )
                Text(
                    bpiScore.toInt().toString(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = freeText,
                    onValueChange = { freeText = it },
                    label = { Text(stringResource(R.string.checkin_free_text_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 5
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = { onSubmit(painScore, energyScore, null, null, null) },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.checkin_no_thanks)) }

                    Button(
                        onClick = {
                            onSubmit(
                                painScore,
                                energyScore,
                                bpiDomain,
                                bpiScore.toInt(),
                                freeText.trim().ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) { Text(stringResource(R.string.checkin_submit)) }
                }
            }
        }
    }
}

/**
 * Energy 6-point scale using a distinct illustration set.
 *
 * Drawables required in res/drawable/:
 *   energy_face_0.png  — no energy (exhausted)
 *   energy_face_2.png  — very low
 *   energy_face_4.png  — low
 *   energy_face_6.png  — moderate
 *   energy_face_8.png  — good
 *   energy_face_10.png — full energy
 *
 * Scale: 0 = no energy, 10 = full energy (ascending, opposite direction from pain).
 * Anchor labels shown below first and last faces only.
 */
@Composable
fun EnergyScale(selected: Int, onSelect: (Int) -> Unit) {
    val faces = listOf(
        0 to "😴",   // No energy
        2 to "😪",   // Very low
        4 to "😑",   // Low
        6 to "🙂",   // Moderate
        8 to "😊",   // Good
        10 to "😄"   // Full energy
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "⚠️ Replace with licensed energy-scale illustrations before release",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            faces.forEach { (score, glyph) ->
                val isSelected = selected == score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(score) }
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(glyph, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }

        // Anchor labels below first and last face only (spec §9)
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "No energy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Full energy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * FPS-R six-face pain scale using dog face drawables.
 *
 * Drawables required in res/drawable/:
 *   pain_face_0.png  — no pain
 *   pain_face_2.png  — very mild
 *   pain_face_4.png  — mild
 *   pain_face_6.png  — moderate
 *   pain_face_8.png  — severe
 *   pain_face_10.png — very severe
 *
 * FPS-R scores: 0, 2, 4, 6, 8, 10 (even numbers only)
 */
@Composable
fun FpsrScale(selected: Int, onSelect: (Int) -> Unit) {
    val faces = listOf(
        0 to "😶",  // No pain — neutral, relaxed
        2 to "😐",  // Very mild
        4 to "🙁",  // Mild
        6 to "😟",  // Moderate
        8 to "😣",  // Severe
        10 to "😫"  // Very severe
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "⚠️ Replace with licensed FPS-R vector assets before clinical use",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            faces.forEach { (score, glyph) ->
                val isSelected = selected == score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelect(score) }
                        .padding(4.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(glyph, style = MaterialTheme.typography.headlineSmall)
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            faces.forEach { (score, _) ->
                Text(
                    score.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (selected == score) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
