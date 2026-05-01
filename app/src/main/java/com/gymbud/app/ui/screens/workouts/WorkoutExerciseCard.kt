package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.PreviousSet
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.ui.util.displayName
import com.gymbud.app.ui.util.exerciseImageRes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private val completedGreen = Color(0xFF4CAF50)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutExerciseCard(
    workoutExercise: WorkoutExercise,
    index: Int,
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
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseThumbnail(exercise = exercise)

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${index + 1}. ${exercise?.displayName() ?: stringResource(R.string.exercise_deleted_label)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exercise?.type == ExerciseType.WEIGHT_REPS) {
                        Spacer(Modifier.height(4.dp))
                        UnitTogglePill(unit = unit, onClick = onUnitToggle)
                    }
                }

                Box {
                    IconButton(
                        onClick = { menuOpen = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
            }

            Spacer(Modifier.height(10.dp))

            NotesField(
                initialNotes = workoutExercise.notes.orEmpty(),
                onSave = onUpdateNotes
            )

            Spacer(Modifier.height(10.dp))

            SetRowHeader(
                exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS,
                unit = unit
            )

            sets.forEach { set ->
                key(set.id) {
                    val previousSet by produceState<PreviousSet?>(initialValue = null, set.position) {
                        val exId = workoutExercise.exerciseId
                        value = if (exId != null) previousSetProvider(exId, set.position) else null
                    }
                    val dismissState = rememberSwipeToDismissBoxState(
                        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
                    )
                    val scope = rememberCoroutineScope()
                    var showConfirm by remember { mutableStateOf(false) }

                    LaunchedEffect(dismissState.currentValue) {
                        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                            showConfirm = true
                        }
                    }

                    if (showConfirm) {
                        AlertDialog(
                            onDismissRequest = {
                                showConfirm = false
                                scope.launch { dismissState.reset() }
                            },
                            title = { Text(stringResource(R.string.workout_delete_set_title)) },
                            text = { Text(stringResource(R.string.workout_delete_set_body)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    showConfirm = false
                                    onDeleteSet(set)
                                }) {
                                    Text(stringResource(R.string.action_delete))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = {
                                    showConfirm = false
                                    scope.launch { dismissState.reset() }
                                }) {
                                    Text(stringResource(R.string.action_cancel))
                                }
                            }
                        )
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        enableDismissFromEndToStart = true,
                        backgroundContent = {
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    ) {
                        SetRow(
                            set = set,
                            exerciseType = exercise?.type ?: ExerciseType.WEIGHT_REPS,
                            unit = unit,
                            previousSet = previousSet,
                            onUpdate = onUpdateSet
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            TextButton(
                onClick = onAddSet,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.workout_add_set))
            }
        }
    }
}

@Composable
private fun ExerciseThumbnail(exercise: Exercise?) {
    val shape = RoundedCornerShape(12.dp)
    val imageRes = exerciseImageRes(exercise?.imageSlug)
    val photoPath = exercise?.photoPath

    when {
        photoPath != null -> {
            AsyncImage(
                model = photoPath,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(shape)
            )
        }
        imageRes != null -> {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(shape)
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun UnitTogglePill(unit: WeightUnit, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = when (unit) {
                WeightUnit.KG -> stringResource(R.string.workout_kg)
                WeightUnit.LBS -> stringResource(R.string.workout_lbs)
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SetRowHeader(exerciseType: ExerciseType, unit: WeightUnit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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
                HeaderCell(text = stringResource(R.string.workout_min), weight = 1.2f)
                HeaderCell(text = stringResource(R.string.workout_sec), weight = 1.2f)
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
        fontWeight = FontWeight.Bold,
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
    onUpdate: (WorkoutSet) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (set.isCompleted) completedGreen.copy(alpha = 0.08f) else Color.Transparent
            )
            .padding(vertical = 4.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = (set.position + 1).toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (set.isCompleted) completedGreen
            else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    isCompleted = set.isCompleted,
                    onValueChange = { newText ->
                        val parsed = newText.toFloatOrNull()
                        val newKg = parsed?.let { WeightUnit.toKg(it, unit) }
                        onUpdate(set.copy(weightKg = newKg))
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 2.dp)
                )
                NumberField(
                    value = set.reps?.toString() ?: "",
                    isCompleted = set.isCompleted,
                    onValueChange = { newText ->
                        val newReps = newText.toIntOrNull()
                        onUpdate(set.copy(reps = newReps))
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 2.dp)
                )
            }

            ExerciseType.TIME -> {
                val totalSecs = set.durationSeconds ?: 0
                NumberField(
                    value = if (set.durationSeconds != null) (totalSecs / 60).toString() else "",
                    isCompleted = set.isCompleted,
                    onValueChange = { newText ->
                        val newMin = newText.toIntOrNull() ?: 0
                        val currentSec = (set.durationSeconds ?: 0) % 60
                        val combined = newMin * 60 + currentSec
                        onUpdate(set.copy(durationSeconds = if (combined == 0) null else combined))
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 2.dp)
                )
                NumberField(
                    value = if (set.durationSeconds != null) (totalSecs % 60).toString() else "",
                    isCompleted = set.isCompleted,
                    onValueChange = { newText ->
                        val newSec = (newText.toIntOrNull() ?: 0).coerceIn(0, 59)
                        val currentMin = (set.durationSeconds ?: 0) / 60
                        val combined = currentMin * 60 + newSec
                        onUpdate(set.copy(durationSeconds = if (combined == 0) null else combined))
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .padding(horizontal = 2.dp)
                )
            }
        }

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (set.isCompleted) completedGreen
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onUpdate(set.copy(isCompleted = !set.isCompleted)) },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = if (set.isCompleted) {
                        stringResource(R.string.workout_set_completed)
                    } else {
                        stringResource(R.string.workout_set_incomplete)
                    },
                    tint = if (set.isCompleted) Color.White
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    isCompleted: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localText by remember(value) { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = localText,
        onValueChange = { localText = it },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (localText != value) onValueChange(localText)
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = if (isCompleted) completedGreen
            else MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier.onFocusChanged { state ->
            if (!state.hasFocus && localText != value) {
                onValueChange(localText)
            }
        },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isCompleted) Color.Transparent
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                    .padding(vertical = 8.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                if (localText.isEmpty()) {
                    Text(
                        text = "—",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
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
    val shape = RoundedCornerShape(10.dp)

    BasicTextField(
        value = text,
        onValueChange = { text = it },
        textStyle = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurface
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = Modifier
            .fillMaxWidth()
            .onFocusChanged { state ->
                if (!state.hasFocus && text != initialNotes) {
                    onSave(text.ifBlank { null })
                }
            },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                        shape = shape
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.workout_exercise_notes_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
                innerTextField()
            }
        }
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
                if (secs != null) {
                    val m = secs / 60
                    val s = secs % 60
                    "$m:${s.toString().padStart(2, '0')}"
                } else "—"
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
