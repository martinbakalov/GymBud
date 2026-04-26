package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
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
import com.gymbud.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.gymbud.app.domain.model.PreviousSet
import com.gymbud.app.ui.util.displayName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseCard(
    workoutExercise: WorkoutExercise,
    app: GymBudApplication,
    unit: WeightUnit,
    onAddSet: () -> Unit,
    onUpdateSet: (WorkoutSet) -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    onRemoveExercise: () -> Unit,
    onUnitToggle: () -> Unit,
    onUpdateNotes: (String?) -> Unit,
    previousSetProvider: suspend (Long, Int) -> PreviousSet?,
    observeSets: () -> Flow<List<WorkoutSet>>
) {

    var exercise by remember { mutableStateOf<Exercise?>(null) }
    LaunchedEffect(workoutExercise.exerciseId) {
        exercise = workoutExercise.exerciseId?.let {
            app.exerciseRepository.getById(it)
        }
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
                    text = exercise?.displayName() ?: stringResource(R.string.exercise_deleted_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (exercise?.type == ExerciseType.WEIGHT_REPS) {
                    TextButton(onClick = onUnitToggle) {
                        Text(
                            text = when (unit) {
                                WeightUnit.KG -> stringResource(R.string.workout_kg)
                                WeightUnit.LBS -> stringResource(R.string.workout_lbs)
                            }
                        )
                    }
                }
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

            NotesField(
                initialNotes = workoutExercise.notes.orEmpty(),
                onSave = onUpdateNotes
            )

            SetRowHeader(
                exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS,
                unit = unit
            )

            sets.forEach { set ->
                val previousSet by produceState<PreviousSet?>(initialValue = null, set.position) {
                    val exId = workoutExercise.exerciseId
                    value = if (exId != null) previousSetProvider(exId, set.position) else null
                }
                SetRow(
                    set = set,
                    exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS,
                    unit = unit,
                    previousSet = previousSet,
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
private fun SetRowHeader(exerciseType: ExerciseType, unit: WeightUnit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderCell(text = stringResource(R.string.workout_set), weight = 0.8f)
        HeaderCell(text = stringResource(R.string.workout_previous), weight = 1.6f)
        when (exerciseType) {
            ExerciseType.WEIGHT_REPS -> {
                HeaderCell(
                    text = when (unit) {
                        WeightUnit.KG -> stringResource(R.string.workout_kg)
                        WeightUnit.LBS -> stringResource(R.string.workout_lbs)
                    },
                    weight = 1.2f
                )
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
    unit: WeightUnit,
    previousSet: PreviousSet?,
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

        PreviousCell(
            previousSet = previousSet,
            exerciseType = exerciseType,
            unit = unit,
            modifier = Modifier.weight(1.6f)
        )

        when (exerciseType) {
            ExerciseType.WEIGHT_REPS -> {

                val displayValue = set.weightKg?.let { kg ->
                    val inUnit = WeightUnit.fromKg(kg, unit)
                    trimZero(inUnit)
                } ?: ""

                NumberField(
                    value = displayValue,
                    onValueChange = { newText ->
                        val parsed = newText.toFloatOrNull()
                        val newKg = parsed?.let { WeightUnit.toKg(it, unit) }
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
    var localText by remember(value) { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = localText,
        onValueChange = { localText = it },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number,  imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = {
                if (localText != value) onValueChange(localText)
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        modifier = modifier.onFocusChanged { state ->
            if (!state.hasFocus && localText != value) {
                onValueChange(localText)
            }
        }
    )
}

private fun trimZero(value: Float): String {
    val asInt = value.toInt()
    return if (value == asInt.toFloat()) asInt.toString() else value.toString()
}

@Composable
private fun stringResource(id: Int, vararg args: Any): String =
    androidx.compose.ui.res.stringResource(id = id, formatArgs = args)

@Composable
private fun NotesField(
    initialNotes: String,
    onSave: (String?) -> Unit
) {
    var text by remember(initialNotes) { mutableStateOf(initialNotes) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        placeholder = {
            Text(
                text = stringResource(R.string.workout_exercise_notes_hint),
                style = MaterialTheme.typography.bodySmall
            )
        },
        textStyle = MaterialTheme.typography.bodySmall,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .onFocusChanged { state ->
                if (!state.hasFocus && text != initialNotes) {
                    onSave(text.ifBlank { null })
                }
            },
        minLines = 1,
        maxLines = 3
    )
}

@Composable
private fun PreviousCell(
    previousSet: PreviousSet?,
    exerciseType: ExerciseType,
    unit: WeightUnit,
    modifier: Modifier = Modifier
) {
    val text = remember(previousSet, exerciseType, unit) {
        if (previousSet == null) "—"
        else when (exerciseType) {
            ExerciseType.WEIGHT_REPS -> {
                val kg = previousSet.weightKg
                val reps = previousSet.reps
                if (kg != null && reps != null) {
                    val display = WeightUnit.fromKg(kg, unit)
                    "${trimZero(display)} × $reps"
                } else "—"
            }
            ExerciseType.TIME -> {
                val secs = previousSet.durationSeconds
                if (secs != null) "${secs}s" else "—"
            }
        }
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}
