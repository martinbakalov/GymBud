package com.gymbud.app

import android.app.Application
import com.gymbud.app.data.local.GymBudDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.data.prefs.AppPreferences

class GymBudApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database: GymBudDatabase by lazy {
        GymBudDatabase.getDatabase(this, applicationScope)
    }
    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepository(database.exerciseDao())
    }
    val preferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            workoutDao = database.workoutDao(),
            workoutExerciseDao = database.workoutExerciseDao(),
            workoutSetDao = database.workoutSetDao(),
            exerciseDao = database.exerciseDao()
        )
    }
}