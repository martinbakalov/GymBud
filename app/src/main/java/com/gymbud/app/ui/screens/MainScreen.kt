package com.gymbud.app.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.gymbud.app.ui.screens.workouts.WorkoutsScreen
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.gymbud.app.GymBudApplication
import com.gymbud.app.ui.navigation.WorkoutsRoutes
import com.gymbud.app.ui.screens.workouts.ActiveWorkoutScreen
import com.gymbud.app.ui.screens.workouts.TemplateDetailScreen
import com.gymbud.app.ui.navigation.PickerRoutes
import com.gymbud.app.ui.screens.picker.ExercisePickerScreen
import com.gymbud.app.ui.screens.workouts.ActiveWorkoutViewModel
import com.gymbud.app.ui.screens.history.WorkoutDetailScreen
import com.gymbud.app.ui.navigation.ProfileRoutes
import com.gymbud.app.ui.screens.profile.EditProfileScreen
import com.gymbud.app.ui.screens.profile.ProfileScreen
import androidx.compose.foundation.layout.Column
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gymbud.app.ui.screens.banner.ActiveWorkoutBanner
import com.gymbud.app.ui.screens.banner.ActiveWorkoutBannerViewModel
import com.gymbud.app.ui.navigation.SettingsRoutes
import com.gymbud.app.ui.screens.settings.AboutScreen
import com.gymbud.app.ui.screens.settings.NotificationsSettingsScreen
import com.gymbud.app.ui.screens.settings.LanguageSettingsScreen
import com.gymbud.app.ui.screens.settings.SettingsHomeScreen
import com.gymbud.app.ui.screens.settings.ThemeSettingsScreen


@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val app = LocalContext.current.applicationContext as GymBudApplication
    val bannerVm: ActiveWorkoutBannerViewModel = viewModel(
        factory = ActiveWorkoutBannerViewModel.Factory(app.workoutRepository)
    )
    val activeWorkout by bannerVm.activeWorkout.collectAsStateWithLifecycle()
    val currentRoute = backStackEntry?.destination?.route
    val bannerHiddenRoutes = setOf(
        WorkoutsRoutes.ACTIVE,
        PickerRoutes.PICK_EXERCISES
    )
    val showBanner = activeWorkout != null && currentRoute !in bannerHiddenRoutes
    Scaffold(
        bottomBar = {
            Column {
                if (showBanner) {
                    ActiveWorkoutBanner(
                        workout = activeWorkout!!,
                        onClick = {
                            navController.navigate(WorkoutsRoutes.active(activeWorkout!!.id)) {
                                launchSingleTop = true
                            }
                        }
                    )
                }
                NavigationBar {
                    TopDestination.all.forEach { dest ->
                        val selected = backStackEntry?.destination?.hierarchy
                            ?.any { it.route == dest.route } == true
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
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopDestination.Workouts.route,
            modifier = Modifier.padding(innerPadding)
        ) {

            navigation(
                route = WorkoutsRoutes.GRAPH,
                startDestination = WorkoutsRoutes.HOME
            ) {
                composable(WorkoutsRoutes.HOME) {
                    WorkoutsScreen(
                        onStartEmptyWorkout = { workoutId ->
                            navController.navigate(WorkoutsRoutes.active(workoutId)) {
                                popUpTo(WorkoutsRoutes.HOME) { inclusive = false }
                            }
                        },
                        onOpenTemplate = { templateId ->
                            navController.navigate(WorkoutsRoutes.template(templateId))
                        }
                    )
                }

                composable(
                    route = WorkoutsRoutes.TEMPLATE,
                    arguments = listOf(
                        navArgument(WorkoutsRoutes.ARG_WORKOUT_ID) { type = NavType.LongType }
                    )
                ) { entry ->
                    val id = entry.arguments
                        ?.getLong(WorkoutsRoutes.ARG_WORKOUT_ID)
                        ?: return@composable
                    TemplateDetailScreen(
                        templateId = id,
                        onStartWorkout = { workoutId ->
                            navController.navigate(WorkoutsRoutes.active(workoutId)) {
                                popUpTo(WorkoutsRoutes.HOME) { inclusive = false }
                            }
                        },
                        onExit = { navController.popBackStack() }
                    )
                }

                composable(
                    route = WorkoutsRoutes.ACTIVE,
                    arguments = listOf(
                        navArgument(WorkoutsRoutes.ARG_WORKOUT_ID) { type = NavType.LongType }
                    )
                ) { entry ->
                    val id = entry.arguments
                        ?.getLong(WorkoutsRoutes.ARG_WORKOUT_ID)
                        ?: return@composable


                    val app = LocalContext.current.applicationContext as GymBudApplication
                    val activeVm: ActiveWorkoutViewModel = viewModel(
                        factory = ActiveWorkoutViewModel.Factory(
                            workoutId = id,
                            repository = app.workoutRepository,
                            exerciseRepository = app.exerciseRepository,
                            preferences = app.preferences
                        ),
                        viewModelStoreOwner = entry
                    )

                    val pickedIds = entry.savedStateHandle
                        .get<LongArray>(PickerRoutes.RESULT_KEY)
                    LaunchedEffect(pickedIds) {
                        if (pickedIds != null && pickedIds.isNotEmpty()) {
                            activeVm.addExercises(pickedIds.toList())
                            entry.savedStateHandle.remove<LongArray>(PickerRoutes.RESULT_KEY)
                        }
                    }

                    ActiveWorkoutScreen(
                        workoutId = id,
                        onExit = {
                            navController.popBackStack(WorkoutsRoutes.HOME, inclusive = false)
                        },
                        onAddExercisesClick = {
                            navController.navigate(PickerRoutes.PICK_EXERCISES)
                        }
                    )
                }

                composable(PickerRoutes.PICK_EXERCISES) {
                    ExercisePickerScreen(
                        onConfirm = { ids ->
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set(PickerRoutes.RESULT_KEY, ids.toLongArray())
                            navController.popBackStack()
                        },
                        onCancel = { navController.popBackStack() }
                    )
                }
            }

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

            navigation(
                route = ProfileRoutes.GRAPH,
                startDestination = ProfileRoutes.HOME
            ) {
                composable(ProfileRoutes.HOME) {
                    ProfileScreen(
                        onWorkoutClick = { id ->
                            navController.navigate(ProfileRoutes.workoutDetail(id))
                        },
                        onEditProfileClick = {
                            navController.navigate(ProfileRoutes.EDIT)
                        },
                        onOpenSettings = {
                            navController.navigate(SettingsRoutes.HOME)
                        }
                    )
                }
                composable(
                    route = ProfileRoutes.WORKOUT_DETAIL,
                    arguments = listOf(
                        navArgument(ProfileRoutes.ARG_WORKOUT_ID) { type = NavType.LongType }
                    )
                ) { entry ->
                    val id = entry.arguments
                        ?.getLong(ProfileRoutes.ARG_WORKOUT_ID)
                        ?: return@composable
                    WorkoutDetailScreen(
                        workoutId = id,
                        onExit = { navController.popBackStack() }
                    )
                }
                composable(ProfileRoutes.EDIT) {
                    EditProfileScreen(
                        onExit = { navController.popBackStack() }
                    )
                }
            }
            navigation(
                route = SettingsRoutes.GRAPH,
                startDestination = SettingsRoutes.HOME
            ) {
                composable(SettingsRoutes.HOME) {
                    SettingsHomeScreen(
                        onExit = { navController.popBackStack() },
                        onOpenNotifications = { navController.navigate(SettingsRoutes.NOTIFICATIONS) },
                        onOpenTheme = { navController.navigate(SettingsRoutes.THEME) },
                        onOpenLanguage = { navController.navigate(SettingsRoutes.LANGUAGE) },
                        onOpenAbout = { navController.navigate(SettingsRoutes.ABOUT) }
                    )
                }
                composable(SettingsRoutes.NOTIFICATIONS) {
                    NotificationsSettingsScreen(onExit = { navController.popBackStack() })
                }
                composable(SettingsRoutes.THEME) {
                    ThemeSettingsScreen(onExit = { navController.popBackStack() })
                }
                composable(SettingsRoutes.LANGUAGE) {
                    LanguageSettingsScreen(onExit = { navController.popBackStack() })
                }
                composable(SettingsRoutes.ABOUT) {
                    AboutScreen(onExit = { navController.popBackStack() })
                }

            }
        }
    }
}