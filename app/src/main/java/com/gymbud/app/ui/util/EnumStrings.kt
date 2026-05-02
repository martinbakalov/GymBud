package com.gymbud.app.ui.util

import androidx.annotation.StringRes
import com.gymbud.app.R
import com.gymbud.app.model.Equipment
import com.gymbud.app.model.MuscleGroup
import com.gymbud.app.model.Sex


@StringRes
fun Equipment.labelRes(): Int = when (this) {
    Equipment.BARBELL     -> R.string.equipment_barbell
    Equipment.DUMBBELL    -> R.string.equipment_dumbbell
    Equipment.MACHINE     -> R.string.equipment_machine
    Equipment.CABLE       -> R.string.equipment_cable
    Equipment.BODYWEIGHT  -> R.string.equipment_bodyweight
    Equipment.KETTLEBELL  -> R.string.equipment_kettlebell
    Equipment.BAND        -> R.string.equipment_band
    Equipment.OTHER       -> R.string.equipment_other
}

@StringRes
fun MuscleGroup.labelRes(): Int = when (this) {
    MuscleGroup.CHEST       -> R.string.muscle_chest
    MuscleGroup.BACK        -> R.string.muscle_back
    MuscleGroup.SHOULDERS   -> R.string.muscle_shoulders
    MuscleGroup.BICEPS      -> R.string.muscle_biceps
    MuscleGroup.TRICEPS     -> R.string.muscle_triceps
    MuscleGroup.FOREARMS    -> R.string.muscle_forearms
    MuscleGroup.CORE        -> R.string.muscle_core
    MuscleGroup.QUADS       -> R.string.muscle_quads
    MuscleGroup.HAMSTRINGS  -> R.string.muscle_hamstrings
    MuscleGroup.GLUTES      -> R.string.muscle_glutes
    MuscleGroup.CALVES      -> R.string.muscle_calves
    MuscleGroup.FULL_BODY   -> R.string.muscle_full_body
    MuscleGroup.OTHER       -> R.string.muscle_other
}

@StringRes
fun Sex.labelRes(): Int = when (this) {
    Sex.MALE -> R.string.sex_male
    Sex.FEMALE -> R.string.sex_female
    Sex.PREFER_NOT_TO_SAY -> R.string.sex_prefer_not_to_say
}