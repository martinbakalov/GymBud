package com.gymbud.app.ui.screens.exercises

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Exercise
import com.gymbud.app.data.repository.ExerciseRepository
import com.gymbud.app.model.Equipment
import com.gymbud.app.model.ExerciseType
import com.gymbud.app.model.MuscleGroup
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CreateExerciseUiState(
    val name: String = "",
    val equipment: Equipment = Equipment.BARBELL,
    val primaryMuscle: MuscleGroup = MuscleGroup.CHEST,
    val secondaryMuscles: Set<MuscleGroup> = emptySet(),
    val type: ExerciseType = ExerciseType.WEIGHT_REPS,
    val photoPath: String? = null,
    val pendingCameraUri: Uri? = null,
    val isSaving: Boolean = false
) {
    val canSave: Boolean get() = name.isNotBlank() && !isSaving
}

class CreateExerciseViewModel(
    private val repository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateExerciseUiState())
    val uiState: StateFlow<CreateExerciseUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(name = value)
    }

    fun onEquipmentChange(value: Equipment) {
        _uiState.value = _uiState.value.copy(equipment = value)
    }

    fun onPrimaryMuscleChange(value: MuscleGroup) {
        val current = _uiState.value
        _uiState.value = current.copy(
            primaryMuscle = value,
            secondaryMuscles = current.secondaryMuscles - value
        )
    }

    fun onToggleSecondaryMuscle(muscle: MuscleGroup) {
        val current = _uiState.value
        if (muscle == current.primaryMuscle) return
        val newSet = if (muscle in current.secondaryMuscles) {
            current.secondaryMuscles - muscle
        } else {
            current.secondaryMuscles + muscle
        }
        _uiState.value = current.copy(secondaryMuscles = newSet)
    }

    fun onTypeChange(value: ExerciseType) {
        _uiState.value = _uiState.value.copy(type = value)
    }

    fun createCameraImageUri(context: Context): Uri {
        val dir = File(context.filesDir, "Pictures/exercises").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "exercise_$timestamp.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        _uiState.value = _uiState.value.copy(pendingCameraUri = uri)
        return uri
    }

    fun onCameraResult(success: Boolean) {
        val uri = _uiState.value.pendingCameraUri ?: return
        if (success) {
            _uiState.value = _uiState.value.copy(
                photoPath = uri.toString(),
                pendingCameraUri = null
            )
        } else {
            _uiState.value = _uiState.value.copy(pendingCameraUri = null)
        }
    }

    fun onGalleryResult(uri: Uri?) {
        if (uri == null) return
        _uiState.value = _uiState.value.copy(photoPath = uri.toString())
    }

    fun clearPhoto() {
        _uiState.value = _uiState.value.copy(photoPath = null)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (!state.canSave) return

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            repository.createCustom(
                Exercise(
                    name = state.name.trim(),
                    equipment = state.equipment,
                    primaryMuscle = state.primaryMuscle,
                    secondaryMuscles = state.secondaryMuscles.toList(),
                    type = state.type,
                    photoPath = state.photoPath
                )
            )
            onSaved()
        }
    }

    class Factory(
        private val repository: ExerciseRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(CreateExerciseViewModel::class.java))
            return CreateExerciseViewModel(repository) as T
        }
    }
}
