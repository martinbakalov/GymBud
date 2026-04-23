package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymbud.app.R
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatVolume

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinishWorkoutDialog(
    durationMillis: Long,
    stats: WorkoutStats,
    onSave: (notes: String?) -> Unit,
    onResume: () -> Unit
) {
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onResume,
        title = { Text(stringResource(R.string.finish_dialog_title)) },
        text = {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatSummary(
                        label = stringResource(R.string.stats_duration),
                        value = formatDurationCompact(durationMillis)
                    )
                    StatSummary(
                        label = stringResource(R.string.stats_sets),
                        value = stats.totalSets.toString()
                    )
                    StatSummary(
                        label = stringResource(R.string.stats_volume),
                        value = "${formatVolume(stats.totalVolumeKg)} ${stringResource(R.string.workout_kg)}"
                    )
                }

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.finish_dialog_notes_hint)) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(notes.ifBlank { null }) }) {
                Text(stringResource(R.string.finish_dialog_save))
            }
        },
        dismissButton = {
                TextButton(onClick = onResume) {
                    Text(stringResource(R.string.finish_dialog_resume))
                }

        }
    )
}

@Composable
private fun StatSummary(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}