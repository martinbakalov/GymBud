package com.gymbud.app.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.ui.graphics.vector.ImageVector
import com.gymbud.app.R


sealed class NavIcon {
    data class Material(
        val outlined: ImageVector,
        val filled: ImageVector
    ) : NavIcon()

    data class Drawable(
        @param:DrawableRes val outlined: Int,
        @param:DrawableRes val filled: Int
    ) : NavIcon()
}

sealed class TopDestination(
    val route: String,
    @get:StringRes val labelRes: Int,
    val icon: NavIcon
) {
    data object Workouts : TopDestination(
        route = WorkoutsRoutes.GRAPH,
        labelRes = R.string.nav_workouts,
        icon = NavIcon.Drawable(
            outlined = R.drawable.ic_nav_workouts,
            filled = R.drawable.ic_nav_workouts
        )
    )

    data object Exercises : TopDestination(
        route = ExercisesRoutes.GRAPH,
        labelRes = R.string.nav_exercises,
        icon = NavIcon.Drawable(
            outlined = R.drawable.ic_nav_exercises,
            filled = R.drawable.ic_nav_exercises
        )
    )

    data object Profile : TopDestination(
        route = ProfileRoutes.GRAPH,
        labelRes = R.string.nav_profile,
        icon = NavIcon.Material(
            outlined = Icons.Outlined.AccountCircle,
            filled = Icons.Filled.AccountCircle
        )
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
    const val FINISH = "workouts/finish/{workoutId}"

    fun template(workoutId: Long): String = "workouts/template/$workoutId"
    fun active(workoutId: Long): String = "workouts/active/$workoutId"
    fun finish(workoutId: Long): String = "workouts/finish/$workoutId"

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
