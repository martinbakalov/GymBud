package com.gymbud.app.domain.model

data class WorkoutStats(
    val totalSets: Int,
    val totalVolumeKg: Float,
    val durationMillis: Long?
)