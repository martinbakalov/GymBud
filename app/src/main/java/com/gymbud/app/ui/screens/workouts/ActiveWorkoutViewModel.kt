package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class ActiveWorkoutViewModel(
    private val workoutId: Long,
    private val repository: WorkoutRepository
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

    fun rename(newName: String) {
        val trimmed = newName.trim()
        viewModelScope.launch {
            repository.renameWorkout(workoutId, trimmed)
        }
    }

    fun finish(onFinished: () -> Unit) {
        viewModelScope.launch {
            repository.finishWorkout(workoutId)
            onFinished()
        }
    }

    class Factory(
        private val workoutId: Long,
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ActiveWorkoutViewModel::class.java))
            return ActiveWorkoutViewModel(workoutId, repository) as T
        }
    }

    }