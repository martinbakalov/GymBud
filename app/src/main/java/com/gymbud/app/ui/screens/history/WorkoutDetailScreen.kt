package com.gymbud.app.ui.screens.history

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatSessionDateTime
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onExit: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val repo = app.workoutRepository

    var workout by remember { mutableStateOf<Workout?>(null) }
    var stats by remember { mutableStateOf<WorkoutStats?>(null) }
    var blocks by remember { mutableStateOf<List<ExerciseBlock>>(emptyList()) }

    LaunchedEffect(workoutId) {
        workout = repo.getWorkout(workoutId)
        stats = repo.statsFor(workoutId)

        val weList = repo.observeExercisesForWorkout(workoutId).first()

        blocks = weList.map { we ->
            val setList = repo.observeSetsForWorkoutExercise(we.id).first()
            val ex = app.exerciseRepository.getById(we.exerciseId)
            ExerciseBlock(
                workoutExercise = we,
                exercise = ex,
                sets = setList
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(workout?.name?.ifBlank { stringResource(R.string.workout_empty_name) } ?: "")
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = formatSessionDateTime(workout?.endedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            val s = stats
            if (s != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DetailStat(
                        label = stringResource(R.string.stats_duration),
                        value = formatDurationCompact(s.durationMillis)
                    )
                    DetailStat(
                        label = stringResource(R.string.stats_sets),
                        value = s.totalSets.toString()
                    )
                    DetailStat(
                        label = stringResource(R.string.stats_volume),
                        value = "${s.totalVolumeKg.toInt()} ${stringResource(R.string.workout_kg)}"
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (blocks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.workout_no_exercises_yet),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = blocks, key = { it.workoutExercise.id }) { block ->
                        ExerciseReadOnlyCard(block)
                    }
                }
            }
        }
    }
}

private data class ExerciseBlock(
    val workoutExercise: WorkoutExercise,
    val exercise: Exercise?,
    val sets: List<WorkoutSet>
)

@Composable
private fun ExerciseReadOnlyCard(block: ExerciseBlock) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = block.exercise?.name ?: "—",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(6.dp))
            val type = block.exercise?.type ?: ExerciseType.WEIGHT_REPS
            block.sets.forEach { set ->
                val line = when (type) {
                    ExerciseType.WEIGHT_REPS -> {
                        val kg = set.weightKg?.let { trimZero(it) } ?: "—"
                        val reps = set.reps?.toString() ?: "—"
                        "${set.position + 1}.  $kg ${stringResourceKg()} × $reps"
                    }
                    ExerciseType.TIME -> {
                        val dur = set.durationSeconds?.let { "${it}s" } ?: "—"
                        "${set.position + 1}.  $dur"
                    }
                }
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (set.isCompleted) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun stringResourceKg(): String = stringResource(R.string.workout_kg)

@Composable
private fun DetailStat(label: String, value: String) {
    Column {
        Text(text = value, style = MaterialTheme.typography.titleLarge)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun trimZero(value: Float): String {
    val asInt = value.toInt()
    return if (value == asInt.toFloat()) asInt.toString() else value.toString()
}