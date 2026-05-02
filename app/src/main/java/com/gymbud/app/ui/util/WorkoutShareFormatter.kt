package com.gymbud.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.model.WeightUnit
import com.gymbud.app.model.WorkoutStats

@Composable
fun formatWorkoutShareSummary(
    workout: Workout,
    stats: WorkoutStats,
    unit: WeightUnit
): String {
    val name = workout.name.ifBlank { stringResource(R.string.workout_empty_name) }
    val date = formatSessionDateTime(workout.endedAt)
    val duration = formatDurationCompact(stats.durationMillis)
    val sets = stats.totalSets
    val unitSuffix = stringResource(
        if (unit == WeightUnit.KG) R.string.workout_kg else R.string.workout_lbs
    )
    val volumeText = "${formatVolumeNumber(stats.totalVolumeKg, unit)} $unitSuffix"

    return buildString {
        appendLine("💪 $name")
        appendLine(date)
        appendLine()
        appendLine("⏱ $duration")
        appendLine("📋 $sets ${stringResource(R.string.stats_sets).lowercase()}")
        appendLine("🏋️ $volumeText")
        if (stats.prCount > 0) {
            appendLine("⭐ ${"${stats.prCount} ${stringResource(R.string.stats_prs)}"}")
        }
    }.trim()
}