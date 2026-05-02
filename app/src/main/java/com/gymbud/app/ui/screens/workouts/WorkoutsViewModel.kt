package com.gymbud.app.ui.screens.workouts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.data.repository.ProfileRepository
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.model.MuscleGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TemplateMeta(
    val exerciseCount: Int,
    val totalSets: Int,
    val primaryMuscles: List<MuscleGroup>,
    val lastUsedAt: Long? = null
)

class WorkoutsViewModel(
    private val repository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    profileRepository: ProfileRepository
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

    val profileName: StateFlow<String?> = profileRepository.observe()
        .map { profile -> profile?.displayName?.takeIf { it.isNotBlank() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )

    private fun observeExercises(templateId: Long): Flow<List<com.gymbud.app.data.local.entity.WorkoutExercise>> =
        repository.observeExercisesForWorkout(templateId)

    suspend fun templateMeta(templateId: Long): TemplateMeta {
        val workoutExercises = observeExercises(templateId).first()
        val muscles = workoutExercises
            .mapNotNull { we ->
                we.exerciseId?.let { exerciseRepository.getById(it)?.primaryMuscle }
            }
            .distinct()
        val totalSets = repository.getSetCountForTemplate(templateId)
        val lastUsedAt = repository.getLastUsedAt(templateId)
        return TemplateMeta(
            exerciseCount = workoutExercises.size,
            totalSets = totalSets,
            primaryMuscles = muscles,
            lastUsedAt = lastUsedAt
        )
    }

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
        private val repository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository,
        private val profileRepository: ProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WorkoutsViewModel::class.java))
            return WorkoutsViewModel(repository, exerciseRepository, profileRepository) as T
        }
    }
}
