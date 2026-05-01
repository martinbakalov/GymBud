package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.prefs.AppPreferences
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class FinishWorkoutViewModel(
    private val workoutId: Long,
    private val repository: WorkoutRepository,
    preferences: AppPreferences
) : ViewModel() {

    val workout: StateFlow<Workout?> = flowOf(workoutId)
        .flatMapLatest { id -> flowOf(repository.getWorkout(id)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val stats: StateFlow<WorkoutStats> = repository.observeStats(workoutId)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            WorkoutStats(totalSets = 0, totalVolumeKg = 0f, durationMillis = null)
        )

    val unit: StateFlow<WeightUnit> = preferences.weightUnit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeightUnit.KG)

    val elapsedMillis: StateFlow<Long> = workout
        .flatMapLatest { w ->
            val startedAt = w?.startedAt ?: return@flatMapLatest flowOf(0L)
            flowOf((System.currentTimeMillis() - startedAt).coerceAtLeast(0L))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    fun setName(name: String) {
        viewModelScope.launch {
            repository.renameWorkout(workoutId, name.trim())
        }
    }

    fun setNotes(notes: String?) {
        viewModelScope.launch {
            repository.updateWorkoutNotes(workoutId, notes?.trim()?.takeIf { it.isNotBlank() })
        }
    }

    fun setPhotoPath(path: String?) {
        viewModelScope.launch {
            repository.updateWorkoutPhoto(workoutId, path)
        }
    }

    fun save(
        name: String,
        notes: String?,
        photoPath: String?,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            if (name.isNotBlank()) repository.renameWorkout(workoutId, name.trim())
            repository.updateWorkoutNotes(workoutId, notes?.trim()?.takeIf { it.isNotBlank() })
            repository.updateWorkoutPhoto(workoutId, photoPath)
            repository.finishWorkout(workoutId)
            onSaved()
        }
    }

    class Factory(
        private val workoutId: Long,
        private val repository: WorkoutRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(FinishWorkoutViewModel::class.java))
            return FinishWorkoutViewModel(workoutId, repository, preferences) as T
        }
    }
}
