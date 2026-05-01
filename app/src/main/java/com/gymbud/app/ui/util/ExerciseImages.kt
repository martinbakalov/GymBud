package com.gymbud.app.ui.util

import androidx.annotation.DrawableRes
import com.gymbud.app.R

@DrawableRes
fun exerciseImageRes(slug: String?): Int? {
    if (slug == null) return null
    return when (slug) {
         "exercise_barbell_bench_press" -> R.drawable.ex_barbell_bench
         "exercise_incline_barbell_bench_press" -> R.drawable.ex_incline_barbell_bench
         "exercise_dumbbell_bench_press" -> R.drawable.ex_db_bench_press
         "exercise_incline_dumbbell_press" -> R.drawable.ex_incline_db_press
         "exercise_dumbbell_fly" -> R.drawable.ex_db_fly
         "exercise_cable_crossover" -> R.drawable.ex_cable_crossover
         "exercise_chest_press_machine" -> R.drawable.ex_chest_press_machine
         "exercise_dips" -> R.drawable.ex_dips
         "exercise_push_up" -> R.drawable.ex_push_up
         "exercise_deadlift" -> R.drawable.ex_deadlift
         "exercise_barbell_row" -> R.drawable.ex_barbell_row
         "exercise_dumbbell_row" -> R.drawable.ex_db_row
         "exercise_lat_pulldown" -> R.drawable.ex_lat_pulldown
         "exercise_seated_cable_row" -> R.drawable.ex_seated_cable_row
         "exercise_pull_up" -> R.drawable.ex_pull_up
         "exercise_overhead_press" -> R.drawable.ex_overhead_press
         "exercise_dumbbell_shoulder_press" -> R.drawable.ex_db_shoulder_press
         "exercise_lateral_raise" -> R.drawable.ex_lateral_raise
         "exercise_face_pull" -> R.drawable.ex_face_pull
         "exercise_barbell_curl" -> R.drawable.ex_barbell_curl
         "exercise_dumbbell_curl" -> R.drawable.ex_db_curl
         "exercise_hammer_curl" -> R.drawable.ex_hammer_curl
         "exercise_triceps_pushdown" -> R.drawable.ex_triceps_pushdown
         "exercise_skull_crusher" -> R.drawable.ex_skull_crusher
         "exercise_triceps_dips" -> R.drawable.ex_triceps_dips
         "exercise_back_squat" -> R.drawable.ex_back_squat
         "exercise_front_squat" -> R.drawable.ex_front_squat
         "exercise_romanian_deadlift" -> R.drawable.ex_romanian_deadlift
         "exercise_leg_press" -> R.drawable.ex_leg_press
         "exercise_leg_extension" -> R.drawable.ex_leg_extension
         "exercise_leg_curl" -> R.drawable.ex_leg_curl
         "exercise_hip_thrust" -> R.drawable.ex_hip_thrust
         "exercise_standing_calf_raise" -> R.drawable.ex_standing_calf_raise
         "exercise_plank" -> R.drawable.ex_plank
         "exercise_treadmill" -> R.drawable.ex_treadmill
         "exercise_exercise_bike" -> R.drawable.ex_exercise_bike
         "exercise_hanging_leg_raise" -> R.drawable.ex_hanging_leg_raise
         "exercise_cable_crunch" -> R.drawable.ex_cable_crunch

        else -> null
    }
}
