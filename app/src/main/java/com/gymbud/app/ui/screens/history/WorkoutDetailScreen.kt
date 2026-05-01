package com.gymbud.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.gymbud.app.ui.util.shareWorkout
import java.io.File

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
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item (key = "date") {
                Text(
                    text = formatSessionDateTime(workout?.endedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            val s = stats
            if (s != null) {
                item (key = "stats"){
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
                            value = "${formatVolumeNumber(stats.totalVolumeKg, unit)} ${
                                stringResource(if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs)
                            }"
                        )
                        if (s.prCount > 0) {
                            DetailStat(
                                label = stringResource(R.string.stats_prs),
                                value = s.prCount.toString(),
                                valueColor = Color(0xFFFFB300)
                            )
                        }
                    }
                }
            }

            val photoPath = workout?.photoPath
            if (photoPath != null && File(photoPath).exists()) {
                item(key = "photo_header") {
                    SectionHeader(stringResource(R.string.detail_section_photo))
                }
                item (key = "photo"){
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
                            .aspectRatio(3f / 4f)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }

            val notes = workout?.notes
            if (!notes.isNullOrBlank()) {
                item(key = "notes_header") {
                    SectionHeader(stringResource(R.string.detail_section_notes))
                }
                item (key = "notes"){
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (blocks.isEmpty()) {
                item(key = "empty"){
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
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
                    SectionHeader(stringResource(R.string.detail_section_exercises))
                }
                items(items = blocks, key = { it.workoutExercise.id }) { block ->
                    ExerciseReadOnlyCard(block = block, unit = unit)
                }
            }
        }
    }
}

@Composable
private fun ExerciseReadOnlyCard(block: ExerciseBlock,  unit: WeightUnit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = block.exercise?.displayName() ?: stringResource(R.string.exercise_deleted_label),
                style = MaterialTheme.typography.titleMedium
            )

            val exerciseNotes = block.workoutExercise.notes
            if (!exerciseNotes.isNullOrBlank()) {
                Text(
                    text = exerciseNotes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(Modifier.height(6.dp))

            val type = block.exercise?.type ?: ExerciseType.WEIGHT_REPS
            val unitSuffix = stringResource(
                if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
            )

            block.sets.forEach { set ->
                val line = when (type) {
                    ExerciseType.WEIGHT_REPS -> {
                        val displayWeight = set.weightKg?.let { kg ->
                            formatWeight(WeightUnit.fromKg(kg, unit))
                        } ?: "—"
                        val reps = set.reps?.toString() ?: "—"
                        "${set.position + 1}.  $displayWeight $unitSuffix × $reps"
                    }
                    ExerciseType.TIME -> {
                        val secs = set.durationSeconds
                        val dur = if (secs != null) {
                            "${secs / 60}:${(secs % 60).toString().padStart(2, '0')}"
                        } else "—"
                        "${set.position + 1}.  $dur"
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (set.isCompleted) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (set.isPR) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFFFFB300).copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "PR",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFB300),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailStat(label: String, value: String, valueColor: Color? = null) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatWeight(value: Float): String {
    val rounded = kotlin.math.round(value * 100) / 100f
    val asInt = rounded.toInt()
    return if (rounded == asInt.toFloat()) asInt.toString() else "%.2f".format(rounded)
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}