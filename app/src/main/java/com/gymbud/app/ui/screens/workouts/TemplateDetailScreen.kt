package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateDetailScreen(
    templateId: Long,
    onStartWorkout: (Long) -> Unit,
    onAddExercisesClick: () -> Unit,
    onExit: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication

    val templateVm: TemplateEditorViewModel = viewModel(
        factory = TemplateEditorViewModel.Factory(
            templateId = templateId,
            repository = app.workoutRepository,
        )
    )

    val workoutsVm: WorkoutsViewModel = viewModel(
        factory = WorkoutsViewModel.Factory(
            repository = app.workoutRepository,
            exerciseRepository = app.exerciseRepository,
            profileRepository = app.profileRepository
        )
    )

    var templateName by remember { mutableStateOf("") }
    LaunchedEffect(templateId) {
        templateName = app.workoutRepository.getWorkout(templateId)?.name.orEmpty()
    }
    val exercises by templateVm.exercises.collectAsStateWithLifecycle()
    val activeWorkout by workoutsVm.activeWorkout.collectAsStateWithLifecycle()

    var pendingStart by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = templateName.ifBlank { stringResource(R.string.workout_empty_name) }
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
            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.template_no_exercises_yet),
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
                        TemplateExerciseCard(
                            workoutExercise = we,
                            app = app,
                            onAddSet = { templateVm.addSet(we.id) },
                            onDeleteSet = templateVm::deleteSet,
                            onRemoveExercise = { templateVm.removeExercise(we) },
                            observeSets = { templateVm.observeSets(we.id) }
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
                onClick = {
                    if (activeWorkout != null) {
                        pendingStart = true
                    } else {
                        workoutsVm.startFromTemplate(templateId, onStarted = onStartWorkout)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.template_start_workout))
            }
        }
    }

    if (pendingStart) {
        val active = activeWorkout
        ActiveWorkoutBlockerDialog(
            onResume = {
                pendingStart = false
                active?.let { onStartWorkout(it.id) }
            },
            onDiscardAndStart = {
                pendingStart = false
                workoutsVm.discardActiveAndStartFromTemplate(
                    templateId,
                    onStarted = onStartWorkout
                )
            },
            onCancel = { pendingStart = false }
        )
    }
}