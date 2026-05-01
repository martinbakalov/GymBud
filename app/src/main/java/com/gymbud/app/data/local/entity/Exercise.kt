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
    val nameKey: String? = null,
    val equipment: Equipment,
    val primaryMuscle: MuscleGroup,
    val secondaryMuscles: List<MuscleGroup> = emptyList(),
    val type: ExerciseType = ExerciseType.WEIGHT_REPS,
    val preferredUnit: WeightUnit? = null,
    val isCustom: Boolean = false,
    val notes: String? = null,
    /**
     * Stable identifier used to look up a bundled drawable for this exercise
     * (see [com.gymbud.app.ui.util.exerciseImageRes]). Preseeded exercises
     * set this to their nameKey so a single drop-in drawable is enough to
     * give the row a thumbnail. Custom exercises leave it null.
     */
    val imageSlug: String? = null,
    /**
     * Absolute file path of a user-supplied photo (camera or gallery).
     * Only set on custom exercises. Takes precedence over [imageSlug] when
     * both are present.
     */
    val photoPath: String? = null
)