package com.gymbud.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gymbud.app.domain.model.AppLanguage
import com.gymbud.app.domain.model.ThemeMode
import com.gymbud.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gymbud_prefs")

class AppPreferences(private val context: Context) {

    val weightUnit: Flow<WeightUnit> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val stored = prefs[KEY_WEIGHT_UNIT]
            stored?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
                ?: WeightUnit.KG
        }
    val dailyNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[KEY_DAILY_ENABLED] ?: false }

    val dailyNotificationHour: Flow<Int> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[KEY_DAILY_HOUR] ?: 8 }

    val dailyNotificationMinute: Flow<Int> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[KEY_DAILY_MINUTE] ?: 0 }

    val dailyNotificationMessage: Flow<String> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[KEY_DAILY_MESSAGE] ?: "" }

    val themeMode: Flow<ThemeMode> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val stored = prefs[KEY_THEME_MODE]
            stored?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
        }

    val workoutNotificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { it[KEY_WORKOUT_NOTIF_ENABLED] ?: true }

    suspend fun setWorkoutNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WORKOUT_NOTIF_ENABLED] = enabled }
    }

    suspend fun setWeightUnit(unit: WeightUnit) {
        context.dataStore.edit { prefs ->
            prefs[KEY_WEIGHT_UNIT] = unit.name
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode.name }
    }

    suspend fun setDailyNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DAILY_ENABLED] = enabled }
    }

    suspend fun setDailyNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[KEY_DAILY_HOUR] = hour
            it[KEY_DAILY_MINUTE] = minute
        }
    }

    suspend fun setDailyNotificationMessage(message: String) {
        context.dataStore.edit { it[KEY_DAILY_MESSAGE] = message }
    }

    val language: Flow<AppLanguage> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val stored = prefs[KEY_LANGUAGE]
            stored?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() } ?: AppLanguage.SYSTEM
        }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[KEY_LANGUAGE] = language.name }
    }


    private companion object {
        val KEY_WEIGHT_UNIT = stringPreferencesKey("weight_unit")
        val KEY_DAILY_ENABLED = booleanPreferencesKey("daily_enabled")
        val KEY_WORKOUT_NOTIF_ENABLED = booleanPreferencesKey("workout_notif_enabled")
        val KEY_DAILY_HOUR = intPreferencesKey("daily_hour")
        val KEY_DAILY_MINUTE = intPreferencesKey("daily_minute")
        val KEY_DAILY_MESSAGE = stringPreferencesKey("daily_message")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
    }
}