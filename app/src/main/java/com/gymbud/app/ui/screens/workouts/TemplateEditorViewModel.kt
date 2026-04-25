package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TemplateEditorViewModel(
    private val templateId: Long,
    private val repository: WorkoutRepository,
) : ViewModel() {

    val exercises: StateFlow<List<WorkoutExercise>> =
        repository.observeExercisesForWorkout(templateId)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun observeSets(workoutExerciseId: Long): Flow<List<WorkoutSet>> =
        repository.observeSetsForWorkoutExercise(workoutExerciseId)

    fun addExercises(exerciseIds: List<Long>) {
        viewModelScope.launch {
            exerciseIds.forEach { id ->
                repository.addExerciseToWorkout(templateId, id)
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

    fun deleteSet(set: WorkoutSet) {
        viewModelScope.launch {
            repository.deleteSet(set)
        }
    }

    class Factory(
        private val templateId: Long,
        private val repository: WorkoutRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(TemplateEditorViewModel::class.java))
            return TemplateEditorViewModel(
                templateId, repository
            ) as T
        }
    }
}