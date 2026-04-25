package com.gymbud.app.ui.screens.history

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.gymbud.app.GymBudApplication
import com.gymbud.app.R
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.ui.util.formatDurationCompact
import com.gymbud.app.ui.util.formatSessionDateTime
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutDetailScreen(
    workoutId: Long,
    onExit: () -> Unit
) {
    val app = LocalContext.current.applicationContext as GymBudApplication
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
                            value = "${s.totalVolumeKg.toInt()} ${stringResource(R.string.workout_kg)}"
                        )
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
                    ExerciseReadOnlyCard(block)
                }
            }
        }
    }
}

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

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}