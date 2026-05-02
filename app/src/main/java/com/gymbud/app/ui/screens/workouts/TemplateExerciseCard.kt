package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.ui.util.displayName
import com.gymbud.app.ui.util.exerciseImageRes
import com.gymbud.app.ui.util.labelRes
import kotlinx.coroutines.flow.Flow
import java.io.File

@Composable
fun TemplateExerciseCard(
    workoutExercise: WorkoutExercise,
    app: GymBudApplication,
    unit: WeightUnit,
    onAddSet: () -> Unit,
    onDeleteSet: (WorkoutSet) -> Unit,
    onUpdateSet: (WorkoutSet) -> Unit,
    onRemoveExercise: () -> Unit,
    observeSets: () -> Flow<List<WorkoutSet>>
) {
    var exercise by remember { mutableStateOf<Exercise?>(null) }

    LaunchedEffect(workoutExercise.exerciseId) {
        exercise = workoutExercise.exerciseId?.let {
            app.exerciseRepository.getById(it)
        }
    }

    val ex = exercise
    var localUnit by remember { mutableStateOf(unit) }
    val sets = observeSets().collectAsState(initial = emptyList()).value
    val type = ex?.type ?: ExerciseType.WEIGHT_REPS
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExerciseThumb(exercise = ex)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = ex?.displayName()
                            ?: stringResource(R.string.exercise_deleted_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    if (ex != null) {
                        Text(
                            text = stringResource(ex.primaryMuscle.labelRes()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (type == ExerciseType.WEIGHT_REPS) {
                    CardUnitToggle(
                        unit = localUnit,
                        onToggle = {
                            localUnit = if (localUnit == WeightUnit.KG) WeightUnit.LBS else WeightUnit.KG
                        }
                    )
                    Spacer(Modifier.width(4.dp))
                }
                IconButton(onClick = onRemoveExercise) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.workout_remove_exercise),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.template_set_header),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(44.dp),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = if (type == ExerciseType.TIME) stringResource(R.string.stats_duration)
                               else stringResource(if (localUnit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    if (type == ExerciseType.WEIGHT_REPS) {
                        Text(
                            text = stringResource(R.string.template_reps_header),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.width(40.dp))
                }

                sets.forEach { set ->
                    TemplateSetRow(
                        set = set,
                        type = type,
                        unit = localUnit,
                        onUpdate = onUpdateSet,
                        onDelete = { onDeleteSet(set) }
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddSet)
                        .drawBehind {
                            val stroke = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(10f, 8f), 0f
                                )
                            )
                            drawRoundRect(
                                color = primaryColor.copy(alpha = 0.4f),
                                cornerRadius = CornerRadius(12.dp.toPx()),
                                style = stroke
                            )
                        }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.workout_add_set),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TemplateSetRow(
    set: WorkoutSet,
    type: ExerciseType,
    unit: WeightUnit,
    onUpdate: (WorkoutSet) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${set.position + 1}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.width(8.dp))

        when (type) {
            ExerciseType.WEIGHT_REPS -> {
                val displayWeight = set.weightKg?.let { formatTemplateWeight(WeightUnit.fromKg(it, unit)) } ?: ""
                TemplateNumberField(
                    value = displayWeight,
                    placeholder = "—",
                    isDecimal = true,
                    onCommit = { text ->
                        val parsed = text.toFloatOrNull()
                        val kg = if (parsed != null) WeightUnit.toKg(parsed, unit) else null
                        onUpdate(set.copy(weightKg = kg))
                    },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                TemplateNumberField(
                    value = set.reps?.toString() ?: "",
                    placeholder = "—",
                    isDecimal = false,
                    onCommit = { text ->
                        onUpdate(set.copy(reps = text.toIntOrNull()))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            ExerciseType.TIME -> {
                val totalSecs = set.durationSeconds ?: 0
                val minText = if (set.durationSeconds != null) (totalSecs / 60).toString() else ""
                val secText = if (set.durationSeconds != null) (totalSecs % 60).toString() else ""
                TemplateNumberField(
                    value = minText,
                    placeholder = "mm",
                    isDecimal = false,
                    onCommit = { text ->
                        val newMin = text.toIntOrNull() ?: 0
                        val curSec = (set.durationSeconds ?: 0) % 60
                        val combined = newMin * 60 + curSec
                        onUpdate(set.copy(durationSeconds = if (combined == 0) null else combined))
                    },
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = ":",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 2.dp)
                )
                TemplateNumberField(
                    value = secText,
                    placeholder = "ss",
                    isDecimal = false,
                    onCommit = { text ->
                        val newSec = (text.toIntOrNull() ?: 0).coerceIn(0, 59)
                        val curMin = (set.durationSeconds ?: 0) / 60
                        val combined = curMin * 60 + newSec
                        onUpdate(set.copy(durationSeconds = if (combined == 0) null else combined))
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun TemplateNumberField(
    value: String,
    placeholder: String,
    isDecimal: Boolean,
    onCommit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var localText by remember(value) { mutableStateOf(value) }
    val focusManager = LocalFocusManager.current

    BasicTextField(
        value = localText,
        onValueChange = { raw ->
            val filtered = if (isDecimal) filterTemplateDecimal(raw)
                           else raw.filter { it.isDigit() }
            localText = filtered
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (localText != value) onCommit(localText)
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        modifier = modifier.onFocusChanged { state ->
            if (!state.hasFocus && localText != value) onCommit(localText)
        },
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                if (localText.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun ExerciseThumb(exercise: Exercise?) {
    val shape = RoundedCornerShape(12.dp)
    val imageRes = exerciseImageRes(exercise?.imageSlug)
    val photoPath = exercise?.photoPath
    val size = 64.dp

    when {
        photoPath != null && File(photoPath).exists() -> {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(File(photoPath))
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape)
            )
        }
        imageRes != null -> {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape)
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun CardUnitToggle(unit: WeightUnit, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .clickable(onClick = onToggle)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (unit == WeightUnit.KG) "KG" else "LBS",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private fun formatTemplateWeight(value: Float): String {
    val rounded = kotlin.math.round(value * 100) / 100f
    val asInt = rounded.toInt()
    return if (rounded == asInt.toFloat()) asInt.toString() else "%.2f".format(rounded)
}

private fun filterTemplateDecimal(input: String): String {
    if (input.isEmpty()) return input
    val cleaned = StringBuilder()
    var hasDecimal = false
    var afterDecimalCount = 0
    for (ch in input) {
        when {
            ch.isDigit() -> {
                if (!hasDecimal || afterDecimalCount < 2) {
                    cleaned.append(ch)
                    if (hasDecimal) afterDecimalCount++
                }
            }
            ch == '.' && !hasDecimal -> {
                hasDecimal = true
                cleaned.append(ch)
            }
        }
    }
    return cleaned.toString()
}
