package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.repository.WorkoutRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WorkoutsViewModel(
    private val repository: WorkoutRepository
) : ViewModel() {

    val templates: StateFlow<List<Workout>> = repository.observeTemplates()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )
    val activeWorkout: StateFlow<Workout?> = repository.observeActiveWorkout()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    fun createTemplate(name: String, onDone: () -> Unit = {}) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.createTemplate(trimmed)
            onDone()
        }
    }

    fun deleteTemplate(workout: Workout) {
        viewModelScope.launch {
            repository.deleteWorkout(workout)
        }
    }
    fun startEmptyWorkout(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.startEmptyWorkout()
            onStarted(newId)
        }
    }

    fun startFromTemplate(templateId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.startFromTemplate(templateId)
            onStarted(newId)
        }
    }

    fun discardActiveAndStartEmpty(onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            activeWorkout.value?.let { repository.discardWorkout(it.id) }
            val newId = repository.startEmptyWorkout()
            onStarted(newId)
        }
    }

    fun discardActiveAndStartFromTemplate(templateId: Long, onStarted: (Long) -> Unit) {
        viewModelScope.launch {
            activeWorkout.value?.let { repository.discardWorkout(it.id) }
            val newId = repository.startFromTemplate(templateId)
            onStarted(newId)
        }
    }

    class Factory(
        private val repository: WorkoutRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WorkoutsViewModel::class.java))
            return WorkoutsViewModel(repository) as T
        }
    }
}