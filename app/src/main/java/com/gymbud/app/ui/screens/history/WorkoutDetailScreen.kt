package com.gymbud.app.ui.screens.history

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.ui.util.displayName
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatSessionDateTime
import com.gymbud.app.ui.util.formatVolumeNumber
import com.gymbud.app.ui.util.formatWorkoutShareSummary
import com.gymbud.app.ui.util.exerciseImageRes
import com.gymbud.app.ui.util.labelRes
import com.gymbud.app.ui.util.shareWorkout
import java.io.File

private val detailAmber = Color(0xFFFFB300)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onExit: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
    val context = LocalContext.current
    val viewModel: WorkoutDetailViewModel = viewModel(
        factory = WorkoutDetailViewModel.Factory(
            workoutId = workoutId,
            workoutRepository = app.workoutRepository,
            exerciseRepository = app.exerciseRepository
        )
    )

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val workout = state.workout
    val stats = state.stats
    val blocks = state.blocks
    val unit by app.preferences.weightUnit
        .collectAsStateWithLifecycle(initialValue = WeightUnit.KG)
    val summaryText = if (workout != null && stats != null) {
        formatWorkoutShareSummary(workout, stats, unit)
    } else null

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = {
                    Text(workout?.name?.ifBlank { stringResource(R.string.workout_empty_name) } ?: "")
                },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (workout != null && summaryText != null) {
                                shareWorkout(context, summaryText, workout.photoPath)
                            }
                        },
                        enabled = workout != null && stats != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = stringResource(R.string.action_share)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item(key = "date") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatSessionDateTime(workout?.endedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (stats != null) {
                item(key = "stats") {
                    val unitSuffix = stringResource(
                        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                label = stringResource(R.string.stats_duration),
                                value = formatDurationCompact(stats.durationMillis),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = stringResource(R.string.stats_sets),
                                value = stats.totalSets.toString(),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            MetricCard(
                                label = stringResource(R.string.stats_volume),
                                value = "${formatVolumeNumber(stats.totalVolumeKg, unit)} $unitSuffix",
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                label = stringResource(R.string.stats_prs),
                                value = stats.prCount.toString(),
                                modifier = Modifier.weight(1f),
                                isPr = true
                            )
                        }
                    }
                }
            }

            val photoPath = workout?.photoPath
            if (photoPath != null && File(photoPath).exists()) {
                item(key = "photo") {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(photoPath))
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }

            val notes = workout?.notes
            if (!notes.isNullOrBlank()) {
                item(key = "notes") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Notes,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = stringResource(R.string.detail_section_notes),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = notes,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            if (blocks.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.workout_no_exercises_yet),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                item(key = "exercises_header") {
                    Text(
                        text = stringResource(R.string.detail_section_exercises),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
                items(items = blocks, key = { it.workoutExercise.id }) { block ->
                    ExerciseReadOnlyCard(block = block, unit = unit)
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isPr: Boolean = false
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = if (isPr) detailAmber else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ExerciseReadOnlyCard(block: ExerciseBlock, unit: WeightUnit) {
    val exercise = block.exercise
    val type = exercise?.type ?: ExerciseType.WEIGHT_REPS
    val unitSuffix = stringResource(
        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
    )
    val exerciseNumber = block.workoutExercise.position + 1

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val thumbPath = exercise?.photoPath
                val thumbImageRes = exerciseImageRes(exercise?.imageSlug)
                val thumbShape = RoundedCornerShape(12.dp)
                when {
                    thumbPath != null && File(thumbPath).exists() -> {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(File(thumbPath))
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(thumbShape)
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    thumbImageRes != null -> {
                        Image(
                            painter = painterResource(id = thumbImageRes),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(thumbShape)
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    else -> {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(thumbShape)
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
                        Spacer(Modifier.width(12.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "#$exerciseNumber",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = exercise?.displayName()
                                ?: stringResource(R.string.exercise_deleted_label),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (exercise != null) {
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = stringResource(exercise.primaryMuscle.labelRes()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            val exerciseNotes = block.workoutExercise.notes
            if (!exerciseNotes.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = exerciseNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(8.dp))

            block.sets.forEach { set ->
                val lineText = when (type) {
                    ExerciseType.WEIGHT_REPS -> {
                        val displayWeight = set.weightKg?.let { kg ->
                            formatWeight(WeightUnit.fromKg(kg, unit))
                        } ?: "—"
                        val reps = set.reps?.toString() ?: "—"
                        "$displayWeight $unitSuffix × $reps"
                    }
                    ExerciseType.TIME -> {
                        val secs = set.durationSeconds
                        if (secs != null) {
                            "${secs / 60}:${(secs % 60).toString().padStart(2, '0')}"
                        } else "—"
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (set.isPR) Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(detailAmber.copy(alpha = 0.10f))
                            else Modifier
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${set.position + 1}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (set.isPR) detailAmber
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(28.dp)
                    )
                    Text(
                        text = lineText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (set.isPR) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (set.isPR) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Box(
                        modifier = Modifier.width(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (set.isPR) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = detailAmber,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatWeight(value: Float): String {
    val rounded = kotlin.math.round(value * 100) / 100f
    val asInt = rounded.toInt()
    return if (rounded == asInt.toFloat()) asInt.toString() else "%.2f".format(rounded)
}