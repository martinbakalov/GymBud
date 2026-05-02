package com.gymbud.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.gymbud.app.data.local.GymBudDatabase
import com.gymbud.app.data.prefs.AppPreferences
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.data.repository.ProfileRepository
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.domain.model.AppLanguage
import com.gymbud.app.domain.model.ThemeMode
import com.gymbud.app.notifications.DailyNotificationScheduler
import com.gymbud.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class GymBudApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob())

    val database: GymBudDatabase by lazy {
        GymBudDatabase.getDatabase(this, applicationScope)
    }
    val exerciseRepository: ExerciseRepository by lazy {
        ExerciseRepository(database.exerciseDao(), this)
    }
    val preferences: AppPreferences by lazy {
        AppPreferences(this)
    }

    val workoutRepository: WorkoutRepository by lazy {
        WorkoutRepository(
            workoutDao = database.workoutDao(),
            workoutExerciseDao = database.workoutExerciseDao(),
            workoutSetDao = database.workoutSetDao(),
            exerciseDao = database.exerciseDao(),
        )
    }

    var initialThemeMode: ThemeMode = ThemeMode.SYSTEM
        private set

    private fun applyStoredThemeAndLanguageBlocking() {
        runBlocking {
            val savedTheme = preferences.themeMode.first()
            initialThemeMode = savedTheme
            val nightMode = when (savedTheme) {
                ThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                ThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                ThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            }
            AppCompatDelegate.setDefaultNightMode(nightMode)

            val savedLang = preferences.language.first()
            val locales = if (savedLang == AppLanguage.SYSTEM) {
                LocaleListCompat.getEmptyLocaleList()
            } else {
                LocaleListCompat.forLanguageTags(savedLang.tag)
            }
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    val profileRepository: ProfileRepository by lazy {
        ProfileRepository(database.profileDao())
    }

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        applyStoredThemeAndLanguageBlocking()
        DailyNotificationScheduler.scheduleNext(this)
    }
}