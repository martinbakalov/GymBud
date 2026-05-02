package com.gymbud.app.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymbud.app.R
import com.gymbud.app.data.local.entity.Exercise

fun exerciseNameResId(nameKey: String): Int? = when (nameKey) {
    "exercise_barbell_bench_press"         -> R.string.exercise_barbell_bench_press
    "exercise_incline_barbell_bench_press" -> R.string.exercise_incline_barbell_bench_press
    "exercise_dumbbell_bench_press"        -> R.string.exercise_dumbbell_bench_press
    "exercise_incline_dumbbell_press"      -> R.string.exercise_incline_dumbbell_press
    "exercise_dumbbell_fly"                -> R.string.exercise_dumbbell_fly
    "exercise_cable_crossover"             -> R.string.exercise_cable_crossover
    "exercise_chest_press_machine"         -> R.string.exercise_chest_press_machine
    "exercise_dips"                        -> R.string.exercise_dips
    "exercise_push_up"                     -> R.string.exercise_push_up
    "exercise_deadlift"                    -> R.string.exercise_deadlift
    "exercise_barbell_row"                 -> R.string.exercise_barbell_row
    "exercise_dumbbell_row"                -> R.string.exercise_dumbbell_row
    "exercise_lat_pulldown"                -> R.string.exercise_lat_pulldown
    "exercise_seated_cable_row"            -> R.string.exercise_seated_cable_row
    "exercise_pull_up"                     -> R.string.exercise_pull_up
    "exercise_overhead_press"              -> R.string.exercise_overhead_press
    "exercise_dumbbell_shoulder_press"     -> R.string.exercise_dumbbell_shoulder_press
    "exercise_lateral_raise"               -> R.string.exercise_lateral_raise
    "exercise_face_pull"                   -> R.string.exercise_face_pull
    "exercise_barbell_curl"                -> R.string.exercise_barbell_curl
    "exercise_dumbbell_curl"               -> R.string.exercise_dumbbell_curl
    "exercise_hammer_curl"                 -> R.string.exercise_hammer_curl
    "exercise_triceps_pushdown"            -> R.string.exercise_triceps_pushdown
    "exercise_skull_crusher"               -> R.string.exercise_skull_crusher
    "exercise_triceps_dips"                -> R.string.exercise_triceps_dips
    "exercise_back_squat"                  -> R.string.exercise_back_squat
    "exercise_front_squat"                 -> R.string.exercise_front_squat
    "exercise_romanian_deadlift"           -> R.string.exercise_romanian_deadlift
    "exercise_leg_press"                   -> R.string.exercise_leg_press
    "exercise_leg_extension"               -> R.string.exercise_leg_extension
    "exercise_leg_curl"                    -> R.string.exercise_leg_curl
    "exercise_hip_thrust"                  -> R.string.exercise_hip_thrust
    "exercise_standing_calf_raise"         -> R.string.exercise_standing_calf_raise
    "exercise_plank"                       -> R.string.exercise_plank
    "exercise_treadmill"                   -> R.string.exercise_treadmill
    "exercise_exercise_bike"               -> R.string.exercise_exercise_bike
    "exercise_hanging_leg_raise"           -> R.string.exercise_hanging_leg_raise
    "exercise_cable_crunch"                -> R.string.exercise_cable_crunch
    else                                   -> null
}

@Composable
fun Exercise.displayName(): String {
    val key = nameKey
    if (key.isNullOrBlank()) return name
    val resId = exerciseNameResId(key) ?: return name
    return stringResource(resId)
}