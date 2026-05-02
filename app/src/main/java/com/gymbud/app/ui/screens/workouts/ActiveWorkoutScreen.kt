package com.gymbud.app.ui.screens.workouts

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.notifications.NotificationHelper.cancelWorkoutInProgress
import com.gymbud.app.notifications.NotificationHelper.showWorkoutInProgress
import com.gymbud.app.ui.util.formatDurationMinutes
import com.gymbud.app.ui.util.formatDurationTicking
import com.gymbud.app.ui.util.formatVolumeNumber
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val statAmber = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActiveWorkoutScreen(
    workoutId: Long,
    onExit: () -> Unit,
    onFinish: () -> Unit,
    onAddExercisesClick: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val context = LocalContext.current
    val viewModel: ActiveWorkoutViewModel = viewModel(
        factory = ActiveWorkoutViewModel.Factory(
            workoutId = workoutId,
            repository = app.workoutRepository,
            exerciseRepository = app.exerciseRepository,
            preferences = app.preferences
        )
    )
    val workout by viewModel.workout.collectAsStateWithLifecycle()
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val globalUnit by viewModel.globalUnit.collectAsStateWithLifecycle(initialValue = WeightUnit.KG)
    var showDiscardDialog by remember { mutableStateOf(false) }
    var nowMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val startedAt = workout?.startedAt
    val elapsedMillis = if (startedAt != null) (nowMillis - startedAt).coerceAtLeast(0L) else 0L

    LaunchedEffect(workout?.startedAt) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val workoutNotifEnabled by app.preferences.workoutNotificationsEnabled
        .collectAsStateWithLifecycle(initialValue = true)

    val defaultWorkoutName = stringResource(R.string.workout_empty_name)

    LaunchedEffect(workout?.id, nowMillis / 60000, workoutNotifEnabled) {
        val w = workout ?: return@LaunchedEffect
        if (workoutNotifEnabled) {
            showWorkoutInProgress(
                context = context,
                workoutName = w.name.ifBlank { defaultWorkoutName },
                elapsedText = formatDurationMinutes(elapsedMillis)
            )
        } else {
            cancelWorkoutInProgress(context)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = workout?.name.orEmpty().ifBlank {
                            stringResource(R.string.workout_empty_name)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    Button(
                        onClick = onFinish,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.workout_finish))
                    }
                }
            )
        }
    ) { innerPadding ->
        val focusManager = LocalFocusManager.current
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager.clearFocus() })
                }
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)
        ) {
            StatsStrip(
                durationMillis = elapsedMillis,
                sets = stats.totalSets,
                volumeKg = stats.totalVolumeKg,
                unit = globalUnit,
                prCount = stats.prCount
            )

            Spacer(Modifier.height(16.dp))

            if (exercises.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.workout_no_exercises_yet),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val localExercises: SnapshotStateList<com.gymbud.app.data.local.entity.WorkoutExercise> =
                    remember { exercises.toMutableStateList() }
                LaunchedEffect(exercises) {
                    if (localExercises.map { it.id } != exercises.map { it.id }) {
                        localExercises.clear()
                        localExercises.addAll(exercises)
                    }
                }
                val lazyListState = rememberLazyListState()
                val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
                    localExercises.add(to.index, localExercises.removeAt(from.index))
                }
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(items = localExercises, key = { _, it -> it.id }) { index, we ->
                        ReorderableItem(reorderState, key = we.id) {
                            val exerciseUnit = if (we.exerciseId != null) {
                                viewModel.unitForExercise(we.exerciseId, globalUnit)
                                    .collectAsStateWithLifecycle(initialValue = globalUnit).value
                            } else {
                                globalUnit
                            }

                            WorkoutExerciseCard(
                                workoutExercise = we,
                                index = index,
                                app = app,
                                unit = exerciseUnit,
                                onAddSet = { viewModel.addSet(we.id) },
                                onUpdateSet = { set -> viewModel.updateSet(set, we.exerciseId) },
                                onDeleteSet = viewModel::deleteSet,
                                onRemoveExercise = { viewModel.removeExercise(we) },
                                onUnitToggle = {
                                    we.exerciseId?.let { viewModel.toggleUnitForExercise(it, exerciseUnit) }
                                },
                                onUpdateNotes = { notes ->
                                    viewModel.updateExerciseNotes(we.id, notes)
                                },
                                previousSetProvider = { exerciseId, position ->
                                    viewModel.previousSetFor(exerciseId, position)
                                },
                                personalBestProvider = { exerciseId ->
                                    viewModel.personalBestForExercise(exerciseId)
                                },
                                observeSets = { viewModel.observeSets(we.id) },
                                onReorderSets = { viewModel.persistSetOrder(it) },
                                dragHandle = {
                                    Icon(
                                        imageVector = Icons.Default.DragHandle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .draggableHandle(
                                                onDragStopped = {
                                                    viewModel.persistExerciseOrder(localExercises.toList())
                                                }
                                            )
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .drawBehind {
                        drawRoundRect(
                            color = primaryColor.copy(alpha = 0.4f),
                            style = Stroke(
                                width = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(
                                    floatArrayOf(14.dp.toPx(), 6.dp.toPx())
                                )
                            ),
                            cornerRadius = CornerRadius(16.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(16.dp))
                    .background(primaryColor.copy(alpha = 0.05f))
                    .clickable(onClick = onAddExercisesClick),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.workout_add_exercise),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = primaryColor
                    )
                }
            }

            TextButton(
                onClick = { showDiscardDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = stringResource(R.string.workout_discard),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    if (showDiscardDialog) {
        DiscardWorkoutDialog(
            onConfirm = {
                showDiscardDialog = false
                cancelWorkoutInProgress(context)
                viewModel.discard(onDiscarded = onExit)
            },
            onCancel = { showDiscardDialog = false }
        )
    }
}

@Composable
private fun StatsStrip(
    durationMillis: Long,
    sets: Int,
    volumeKg: Float,
    unit: WeightUnit,
    prCount: Int
) {
    val unitSuffix = stringResource(
        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        StatCard(
            label = stringResource(R.string.stats_duration),
            value = formatDurationTicking(durationMillis),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.stats_sets),
            value = sets.toString(),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.stats_volume),
            value = "${formatVolumeNumber(volumeKg, unit)} $unitSuffix",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = stringResource(R.string.stats_prs),
            value = prCount.toString(),
            isPr = true,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isPr: Boolean = false
) {
    val labelColor = if (isPr) statAmber else MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = if (isPr) statAmber else MaterialTheme.colorScheme.onSurface
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = labelColor,
                letterSpacing = 0.6.sp,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = valueColor,
                maxLines = 1,
                fontSize = 15.sp
            )
        }

    }
}
