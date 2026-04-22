package com.gymbud.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.gymbud.app.domain.model.WeightUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "gymbud_prefs")

class AppPreferences(context: Context) {

    val weightUnit: Flow<WeightUnit> = context.dataStore.data
        .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
        .map { prefs ->
            val stored = prefs[KEY_WEIGHT_UNIT]
            stored?.let { runCatching { WeightUnit.valueOf(it) }.getOrNull() }
                ?: WeightUnit.KG
        }


    private companion object {
        val KEY_WEIGHT_UNIT = stringPreferencesKey("weight_unit")
    }
}