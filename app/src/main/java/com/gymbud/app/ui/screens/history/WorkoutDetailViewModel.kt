package com.gymbud.app.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.local.entity.Workout
import com.gymbud.app.data.local.entity.WorkoutExercise
import com.gymbud.app.data.local.entity.WorkoutSet
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.data.repository.WorkoutRepository
import com.gymbud.app.domain.model.WorkoutStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ExerciseBlock(
    val workoutExercise: WorkoutExercise,
    val exercise: Exercise?,
    val sets: List<WorkoutSet>
)

data class DetailUiState(
    val workout: Workout? = null,
    val stats: WorkoutStats? = null,
    val blocks: List<ExerciseBlock> = emptyList(),
    val isLoaded: Boolean = false
)

class WorkoutDetailViewModel(
    private val workoutId: Long,
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkout(workoutId)
            val stats = workoutRepository.statsFor(workoutId)
            val weList = workoutRepository.observeExercisesForWorkout(workoutId).first()
            val blocks = weList.map { we ->
                ExerciseBlock(
                    workoutExercise = we,
                    exercise = exerciseRepository.getById(we.exerciseId),
                    sets = workoutRepository.observeSetsForWorkoutExercise(we.id).first()
                )
            }
            _uiState.value = DetailUiState(
                workout = workout,
                stats = stats,
                blocks = blocks,
                isLoaded = true
            )
        }
    }

    class Factory(
        private val workoutId: Long,
        private val workoutRepository: WorkoutRepository,
        private val exerciseRepository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(WorkoutDetailViewModel::class.java))
            return WorkoutDetailViewModel(workoutId, workoutRepository, exerciseRepository) as T
        }
    }
}