package com.gymbud.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.gymbud.app.ui.navigation.TopDestination
import com.gymbud.app.ui.screens.exercises.ExercisesScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.navigation
import com.gymbud.app.ui.navigation.ExercisesRoutes
import com.gymbud.app.ui.screens.exercises.CreateExerciseScreen



@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                TopDestination.all.forEach { dest ->
                    val selected = backStackEntry?.destination?.hierarchy?.any { it.route == dest.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = if (selected) dest.iconFilled else dest.iconOutlined,
                                contentDescription = null
                            )
                        },
                        label = { Text(stringResource(dest.labelRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.Workouts.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopDestination.Workouts.route) { WorkoutsScreen() }

            navigation(
                route = ExercisesRoutes.GRAPH,
                startDestination = ExercisesRoutes.LIST
            ) {
                composable(ExercisesRoutes.LIST) {
                    ExercisesScreen(
                        onCreateExerciseClick = {
                            navController.navigate(ExercisesRoutes.NEW)
                        }
                    )
                }
                composable(ExercisesRoutes.NEW) {
                    CreateExerciseScreen(
                        onDone = { navController.popBackStack() }
                    )
                }
            }

            composable(TopDestination.History.route) { HistoryScreen() }
        }
    }
}