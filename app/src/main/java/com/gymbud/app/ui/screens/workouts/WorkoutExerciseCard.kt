package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.ExerciseType
import kotlinx.coroutines.flow.Flow
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseCard(
    workoutExercise: WorkoutExercise,
    app: GymBudApplication,
    onAddSet: () -> Unit,
    onUpdateSet: (WorkoutSet) -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    onRemoveExercise: () -> Unit,
    observeSets: () -> Flow<List<WorkoutSet>>
) {

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    androidx.compose.runtime.LaunchedEffect(workoutExercise.exerciseId) {
        exercise = app.exerciseRepository.getById(workoutExercise.exerciseId)
    }

    val sets by observeSets().collectAsStateWithLifecycle(initialValue = emptyList())

    var menuOpen by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = exercise?.name ?: "…",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null)
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.workout_remove_exercise)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                        onClick = {
                            menuOpen = false
                            onRemoveExercise()
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            SetRowHeader(exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS)

            sets.forEach { set ->
                SetRow(
                    set = set,
                    exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS,
                    onUpdate = onUpdateSet,
                    onDelete = { onDeleteSet(set) }
                )
            }

            Spacer(Modifier.height(4.dp))

            TextButton(onClick = onAddSet, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.workout_add_set))
            }
        }
    }
}

@Composable
private fun SetRowHeader(exerciseType: ExerciseType) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell(text = stringResource(R.string.workout_set), weight = 0.8f)
        HeaderCell(text = stringResource(R.string.workout_previous), weight = 1.6f)
        when (exerciseType) {
            ExerciseType.WEIGHT_REPS -> {
                HeaderCell(text = stringResource(R.string.workout_kg), weight = 1.2f)
                HeaderCell(text = stringResource(R.string.workout_reps), weight = 1.2f)
            }
            ExerciseType.TIME -> {
                HeaderCell(text = stringResource(R.string.workout_time), weight = 2.4f)
            }
        }
        Spacer(Modifier.size(40.dp))
    }
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.HeaderCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.weight(weight)
    )
}

@Composable
private fun SetRow(
    set: WorkoutSet,
    exerciseType: ExerciseType,
    onUpdate: (WorkoutSet) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (set.position + 1).toString(),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(0.8f)
        )

        Text(
            text = "—",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1.6f)
        )

        when (exerciseType) {
            ExerciseType.WEIGHT_REPS -> {
                NumberField(
                    value = set.weightKg?.let { trimZero(it) } ?: "",
                    onValueChange = { newText ->
                        val newKg = newText.toFloatOrNull()
                        onUpdate(set.copy(weightKg = newKg))
                    },
                    modifier = Modifier.weight(1.2f)
                )
                NumberField(
                    value = set.reps?.toString() ?: "",
                    onValueChange = { newText ->
                        val newReps = newText.toIntOrNull()
                        onUpdate(set.copy(reps = newReps))
                    },
                    modifier = Modifier.weight(1.2f)
                )
            }
            ExerciseType.TIME -> {
                NumberField(
                    value = set.durationSeconds?.toString() ?: "",
                    onValueChange = { newText ->
                        val newSecs = newText.toIntOrNull()
                        onUpdate(set.copy(durationSeconds = newSecs))
                    },
                    modifier = Modifier.weight(2.4f)
                )
            }
        }

        IconButton(
            onClick = { onUpdate(set.copy(isCompleted = !set.isCompleted)) },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = if (set.isCompleted) {
                    stringResource(R.string.workout_set_completed)
                } else {
                    stringResource(R.string.workout_set_incomplete)
                },
                tint = if (set.isCompleted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier.padding(horizontal = 2.dp)
    )
}

private fun trimZero(value: Float): String {
    val asInt = value.toInt()
    return if (value == asInt.toFloat()) asInt.toString() else value.toString()
}

@Composable
private fun stringResource(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id = id, formatArgs = args)