package com.gymbud.app.model

data class WorkoutStats(
    val totalSets: Int,
    val totalVolumeKg: Float,
    val durationMillis: Long?,
    val prCount: Int = 0,
    val dominantMuscle: MuscleGroup? = null
)
