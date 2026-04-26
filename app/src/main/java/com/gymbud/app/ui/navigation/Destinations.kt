package com.gymbud.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector
import com.gymbud.app.R

sealed class TopDestination(
    val route: String,
    @get:StringRes val labelRes: Int,
    val iconOutlined: ImageVector,
    val iconFilled: ImageVector
) {
    data object Workouts : TopDestination(
        route = WorkoutsRoutes.GRAPH,
        labelRes = R.string.nav_workouts,
        iconOutlined = Icons.Outlined.PlayArrow,
        iconFilled = Icons.Filled.PlayArrow
    )

    data object Exercises : TopDestination(
        route = ExercisesRoutes.GRAPH,
        labelRes = R.string.nav_exercises,
        iconOutlined = Icons.Outlined.FitnessCenter,
        iconFilled = Icons.Filled.FitnessCenter
    )

    data object Profile : TopDestination(
        route = ProfileRoutes.GRAPH,
        labelRes = R.string.nav_profile,
        iconOutlined = Icons.Outlined.Person,
        iconFilled = Icons.Filled.Person
    )


    companion object {
        val all = listOf(Workouts, Exercises, Profile)
    }
}
object ExercisesRoutes {
    const val GRAPH = "exercises"
    const val LIST = "exercises/list"
    const val NEW = "exercises/new"
}

object SettingsRoutes {
    const val GRAPH = "settings"
    const val HOME = "settings/home"
    const val PROFILE = "settings/profile"
    const val NOTIFICATIONS = "settings/notifications"
    const val THEME = "settings/theme"
    const val LANGUAGE = "settings/language"
    const val UNITS = "settings/units"
    const val ABOUT = "settings/about"
}
object WorkoutsRoutes {
    const val GRAPH = "workouts"
    const val HOME = "workouts/home"
    const val TEMPLATE = "workouts/template/{workoutId}"
    const val ACTIVE = "workouts/active/{workoutId}"

    fun template(workoutId: Long): String = "workouts/template/$workoutId"
    fun active(workoutId: Long): String = "workouts/active/$workoutId"

    const val ARG_WORKOUT_ID = "workoutId"
}

object PickerRoutes {
    const val PICK_EXERCISES = "picker/exercises"

    const val RESULT_KEY = "pickedExerciseIds"
}
object ProfileRoutes {
    const val GRAPH = "profile"
    const val HOME = "profile/home"
    const val WORKOUT_DETAIL = "profile/workout/{workoutId}"

    fun workoutDetail(workoutId: Long): String = "profile/workout/$workoutId"

    const val ARG_WORKOUT_ID = "workoutId"
}