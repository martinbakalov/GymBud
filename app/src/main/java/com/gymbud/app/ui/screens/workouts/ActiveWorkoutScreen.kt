package com.gymbud.app.ui.screens.workouts

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
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import kotlinx.coroutines.delay
import androidx.compose.ui.focus.onFocusChanged
import com.gymbud.app.ui.util.formatDurationTicking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: Long,
    onExit: () -> Unit,
    onAddExercisesClick: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val viewModel: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModel.Factory(workoutId, app.workoutRepository)
    )
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(workout?.startedAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val startedAt = workout?.startedAt
    val elapsedMillis = if (startedAt != null) (nowMillis - startedAt).coerceAtLeast(0L) else 0L

    Scaffold(
        topBar = {
            TopAppBar(
                title = {

                    WorkoutNameField(
                        initialName = workout?.name.orEmpty(),
                        onRename = viewModel::rename
                    )
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            StatsStrip(
                durationMillis = elapsedMillis,
                sets = 0,
                volumeKg = 0f
            )

            Spacer(Modifier.height(16.dp))

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
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
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(items = exercises, key = { it.id }) { we ->
                        WorkoutExerciseCard(
                            workoutExercise = we,
                            app = app,
                            onAddSet = { viewModel.addSet(we.id) },
                            onUpdateSet = viewModel::updateSet,
                            onDeleteSet = viewModel::deleteSet,
                            onRemoveExercise = { viewModel.removeExercise(we) },
                            observeSets = { viewModel.observeSets(we.id) }
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onAddExercisesClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.workout_add_exercise))
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = { viewModel.finish(onFinished = onExit) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.workout_finish))
            }
        }
    }
}

@Composable
private fun WorkoutNameField(
    initialName: String,
    onRename: (String) -> Unit
) {

    var text by remember(initialName) { mutableStateOf(initialName) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text(stringResource(R.string.workout_empty_name)) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                if (!state.hasFocus && text != initialName) onRename(text)
            }
    )
}

@Composable
private fun StatsStrip(
    durationMillis: Long,
    sets: Int,
    volumeKg: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatCell(
            label = stringResource(R.string.stats_duration),
            value = formatDurationTicking(durationMillis)
        )
        StatCell(
            label = stringResource(R.string.stats_sets),
            value = sets.toString()
        )
        StatCell(
            label = stringResource(R.string.stats_volume),
            value = "${volumeKg.toInt()} ${stringResource(R.string.workout_kg)}"
        )
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}