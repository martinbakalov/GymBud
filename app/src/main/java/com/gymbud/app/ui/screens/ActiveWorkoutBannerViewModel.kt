package com.gymbud.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn


class ActiveWorkoutBannerViewModel(
    repository: WorkoutRepository
) : ViewModel() {

    val activeWorkout: StateFlow<Workout?> = repository.observeActiveWorkout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    class Factory(
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ActiveWorkoutBannerViewModel::class.java))
            return ActiveWorkoutBannerViewModel(repository) as T
        }
    }
}