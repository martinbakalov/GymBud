package com.gymbud.app.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.History
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
        route = "workouts",
        labelRes = R.string.nav_workouts,
        iconOutlined = Icons.Outlined.PlayArrow,
        iconFilled = Icons.Filled.PlayArrow
    )

    data object Exercises : TopDestination(
        route = "exercises",
        labelRes = R.string.nav_exercises,
        iconOutlined = Icons.Outlined.FitnessCenter,
        iconFilled = Icons.Filled.FitnessCenter
    )

    data object History : TopDestination(
        route = "history",
        labelRes = R.string.nav_history,
        iconOutlined = Icons.Outlined.History,
        iconFilled = Icons.Filled.History
    )

    companion object {
        val all = listOf(Workouts, Exercises, History)
    }
}