package com.gymbud.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.ExerciseType
import com.gymbud.app.domain.model.MuscleGroup
import com.gymbud.app.domain.model.WeightUnit


@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val equipment: Equipment,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val type: ExerciseType = ExerciseType.WEIGHT_REPS,
    val preferredUnit: WeightUnit? = null,
    val isCustom: Boolean = false,
    val notes: String? = null
)