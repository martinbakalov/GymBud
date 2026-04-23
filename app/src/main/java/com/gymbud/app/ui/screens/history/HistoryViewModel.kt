package com.gymbud.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    val history: StateFlow<List<Pair<Workout, WorkoutStats>>> =
        repository.observeHistoryWithStats()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun deleteWorkout(workout: Workout) {
        viewModelScope.launch {
            repository.discardWorkout(workout.id)
        }
    }

    class Factory(
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(HistoryViewModel::class.java))
            return HistoryViewModel(repository) as T
        }
    }
}