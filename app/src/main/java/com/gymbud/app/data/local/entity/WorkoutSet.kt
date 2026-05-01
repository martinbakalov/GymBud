package com.gymbud.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sets",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutExercise::class,
            parentColumns = ["id"],
            childColumns = ["workoutExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["workoutExerciseId"])
    ]
)
data class WorkoutSet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val workoutExerciseId: Long,
    val position: Int,
    val weightKg: Float? = null,
    val reps: Int? = null,
    val durationSeconds: Int? = null,
    val isCompleted: Boolean = false,
    val isPR: Boolean = false
)