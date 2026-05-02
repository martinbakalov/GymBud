package com.gymbud.app.ui.screens.picker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.model.Equipment
import com.gymbud.app.model.MuscleGroup
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

data class ExercisePickerUiState(
    val query: String = "",
    val muscleFilter: MuscleGroup? = null,
    val equipmentFilter: Equipment? = null,
    val selectedIds: Set<Long> = emptySet()
)

@OptIn(ExperimentalCoroutinesApi::class)
class ExercisePickerViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExercisePickerUiState())
    val uiState: StateFlow<ExercisePickerUiState> = _uiState.asStateFlow()

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

    fun onQueryChange(value: String) {
        _uiState.value = _uiState.value.copy(query = value)
    }

    fun onMuscleFilterChange(value: MuscleGroup?) {
        _uiState.value = _uiState.value.copy(muscleFilter = value)
    }

    fun onEquipmentFilterChange(value: Equipment?) {
        _uiState.value = _uiState.value.copy(equipmentFilter = value)
    }

    fun togglePick(exerciseId: Long) {
        val current = _uiState.value
        val newSet = if (exerciseId in current.selectedIds) {
            current.selectedIds - exerciseId
        } else {
            current.selectedIds + exerciseId
        }
        _uiState.value = current.copy(selectedIds = newSet)
    }

    class Factory(
        private val repository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ExercisePickerViewModel::class.java))
            return ExercisePickerViewModel(repository) as T
        }
    }
}