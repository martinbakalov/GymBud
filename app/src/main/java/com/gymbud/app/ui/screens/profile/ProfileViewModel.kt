package com.gymbud.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Profile
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.repository.ProfileRepository
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    val profile: StateFlow<Profile?> = profileRepository.observe()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    val workoutCount: StateFlow<Int> = profileRepository.observeFinishedWorkoutCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = 0
        )

    val history: StateFlow<List<Pair<Workout, WorkoutStats>>> =
        workoutRepository.observeHistoryWithStats()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            workoutRepository.discardWorkout(workout.id)
        }
    }

    class Factory(
        private val profileRepository: ProfileRepository,
        private val workoutRepository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ProfileViewModel::class.java))
            return ProfileViewModel(profileRepository, workoutRepository) as T
        }
    }
}