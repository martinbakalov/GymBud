package com.gymbud.app.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gymbud.app.data.local.entity.Profile
import com.gymbud.app.data.repository.ProfileRepository
import com.gymbud.app.domain.model.Sex
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditProfileUiState(
    val displayName: String = "",
    val bio: String = "",
    val sex: Sex? = null,
    val birthdayMillis: Long? = null,
    val avatarPath: String? = null,
    val isSaving: Boolean = false,
    val isLoaded: Boolean = false
)

class EditProfileViewModel(
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val current = repository.get() ?: Profile(id = 1L)
            _uiState.value = EditProfileUiState(
                displayName = current.displayName.orEmpty(),
                bio = current.bio.orEmpty(),
                sex = current.sex,
                birthdayMillis = current.birthdayMillis,
                avatarPath = current.avatarPath,
                isLoaded = true
            )
        }
    }

    fun onNameChange(value: String) {
        val capped = if (value.length > MAX_DISPLAY_NAME_LENGTH) {
            value.take(MAX_DISPLAY_NAME_LENGTH)
        } else value
        _uiState.value = _uiState.value.copy(displayName = capped)
    }

    fun onBioChange(value: String) {
        _uiState.value = _uiState.value.copy(bio = value)
    }

    fun onSexChange(value: Sex?) {
        _uiState.value = _uiState.value.copy(sex = value)
    }

    fun onBirthdayChange(value: Long?) {
        _uiState.value = _uiState.value.copy(birthdayMillis = value)
    }

    fun onAvatarChange(path: String?) {
        _uiState.value = _uiState.value.copy(avatarPath = path)
    }

    fun save(onSaved: () -> Unit) {
        val s = _uiState.value
        if (s.isSaving) return
        _uiState.value = s.copy(isSaving = true)
        viewModelScope.launch {
            repository.upsert(
                Profile(
                    id = 1L,
                    displayName = s.displayName.trim().takeIf { it.isNotBlank() },
                    bio = s.bio.trim().takeIf { it.isNotBlank() },
                    sex = s.sex,
                    birthdayMillis = s.birthdayMillis,
                    avatarPath = s.avatarPath
                )
            )
            onSaved()
        }
    }

    class Factory(
        private val repository: ProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(EditProfileViewModel::class.java))
            return EditProfileViewModel(repository) as T
        }
    }

    companion object {
        const val MAX_DISPLAY_NAME_LENGTH = 20
    }
}