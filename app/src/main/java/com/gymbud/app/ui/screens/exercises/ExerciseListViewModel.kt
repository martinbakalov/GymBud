package com.gymbud.app.ui.screens.exercises

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.domain.model.Equipment
import com.gymbud.app.domain.model.MuscleGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn


data class ExerciseListUiState(
    val query: String = "",
    val muscleFilter: MuscleGroup? = null,
    val equipmentFilter: Equipment? = null
)


@OptIn(ExperimentalCoroutinesApi::class)
class ExerciseListViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseListUiState())
    val uiState: StateFlow<ExerciseListUiState> = _uiState.asStateFlow()

    val exercises: StateFlow<List<Exercise>> = _uiState
        .flatMapLatest { state ->
            repository.observeFiltered(
                query = state.query,
                muscle = state.muscleFilter,
                equipment = state.equipmentFilter
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun onQueryChange(newQuery: String) {
        _uiState.value = _uiState.value.copy(query = newQuery)
    }

    fun onMuscleFilterChange(muscle: MuscleGroup?) {
        _uiState.value = _uiState.value.copy(muscleFilter = muscle)
    }

    fun onEquipmentFilterChange(equipment: Equipment?) {
        _uiState.value = _uiState.value.copy(equipmentFilter = equipment)
    }

    class Factory(
        private val repository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ExerciseListViewModel::class.java))
            return ExerciseListViewModel(repository) as T
        }
    }
}