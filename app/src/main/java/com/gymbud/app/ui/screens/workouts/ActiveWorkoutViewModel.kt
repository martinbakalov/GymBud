package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.domain.model.WorkoutStats
import com.gymbud.app.data.prefs.AppPreferences
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.domain.model.WeightUnit
import com.gymbud.app.ui.util.effectiveUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch




@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModel(
    private val workoutId: Long,
    private val repository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    preferences: AppPreferences
) : ViewModel() {

    val workout: StateFlow<Workout?> = flowOf(workoutId)
        .flatMapLatest { id ->
            flowOf(repository.getWorkout(id))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val exercises: StateFlow<List<WorkoutExercise>> = repository
        .observeExercisesForWorkout(workoutId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val stats: StateFlow<WorkoutStats> = repository.observeStats(workoutId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WorkoutStats(
                totalSets = 0,
                totalVolumeKg = 0f,
                durationMillis = null
            )
        )

    fun rename(newName: String) {
        val trimmed = newName.trim()
        viewModelScope.launch {
            val current = repository.getWorkout(workoutId)
            if (current?.endedAt != null) return@launch
            repository.renameWorkout(workoutId, trimmed)
        }
    }

    fun finish(
        name: String,
        notes: String?,
        photoPath: String?,
        onFinished: () -> Unit
    ) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                repository.renameWorkout(workoutId, name)
            }
            val trimmedNotes = notes?.trim()?.takeIf { it.isNotBlank() }
            if (trimmedNotes != null) {
                repository.updateWorkoutNotes(workoutId, trimmedNotes)
            }
            if (photoPath != null) {
                repository.updateWorkoutPhoto(workoutId, photoPath)
            }
            repository.finishWorkout(workoutId)
            onFinished()
        }
    }

    fun discard(onDiscarded: () -> Unit) {
        viewModelScope.launch {
            repository.discardWorkout(workoutId)
            onDiscarded()
        }
    }

    fun observeSets(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        repository.observeSetsForWorkoutExercise(workoutExerciseId)

    fun addExercises(exerciseIds: List<Long>) {
        viewModelScope.launch {
            for (id in exerciseIds) {
                repository.addExerciseToWorkout(workoutId, id)
            }
        }
    }

    fun removeExercise(workoutExercise: WorkoutExercise) {
        viewModelScope.launch {
            repository.removeExerciseFromWorkout(workoutExercise)
        }
    }

    fun addSet(workoutExerciseId: Long) {
        viewModelScope.launch {
            repository.addSet(workoutExerciseId)
        }
    }

    fun updateSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.updateSet(set)
        }
    }

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    class Factory(
        private val workoutId: Long,
        private val repository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val preferences: AppPreferences
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java))
            return ActiveWorkoutViewModel(
                workoutId, repository, exerciseRepository, preferences
            ) as T
        }
    }

    val globalUnit: StateFlow<WeightUnit> = preferences.weightUnit
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = WeightUnit.KG
        )

    fun unitForExercise(exerciseId: Long, globalDefault: WeightUnit): Flow<WeightUnit> =
        exerciseRepository.observeById(exerciseId)
            .map { ex -> effectiveUnit(ex, globalDefault) }

    fun toggleUnitForExercise(exerciseId: Long, currentlyDisplayed: WeightUnit) {
        viewModelScope.launch {
            val ex = exerciseRepository.getById(exerciseId) ?: return@launch
            val newUnit = if (currentlyDisplayed == WeightUnit.KG) WeightUnit.LBS else WeightUnit.KG
            exerciseRepository.update(ex.copy(preferredUnit = newUnit))
        }
    }

    }