package com.gymbud.app.ui.screens.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatSessionDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onWorkoutClick: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val viewModel: HistoryViewModel = viewModel(
        factory = HistoryViewModel.Factory(app.workoutRepository)
    )
    val history by viewModel.history.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.history_title)) })
        }
    ) { innerPadding ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier.padding(innerPadding).fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(innerPadding).fillMaxSize()
            ) {
                items(items = history, key = { it.first.id }) { (workout, stats) ->
                    HistoryRow(
                        workout = workout,
                        stats = stats,
                        onClick = { onWorkoutClick(workout.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(
    workout: Workout,
    stats: WorkoutStats,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = workout.name.ifBlank { stringResource(R.string.workout_empty_name) },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = formatSessionDateTime(workout.endedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            androidx.compose.foundation.layout.Spacer(Modifier.padding(top = 8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatInline(
                    label = stringResource(R.string.stats_duration),
                    value = formatDurationCompact(stats.durationMillis)
                )
                StatInline(
                    label = stringResource(R.string.stats_sets),
                    value = stats.totalSets.toString()
                )
                StatInline(
                    label = stringResource(R.string.stats_volume),
                    value = "${stats.totalVolumeKg.toInt()} ${stringResource(R.string.workout_kg)}"
                )
            }
        }
    }
}

@Composable
private fun StatInline(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}