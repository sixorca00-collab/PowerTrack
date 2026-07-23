package com.powertrack.mobile.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.data.remote.dto.UserProfileDto
import com.powertrack.mobile.data.repository.AuthRepository
import com.powertrack.mobile.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val isLoading: Boolean = true,
    val profile: UserProfileDto? = null,
    val errorMessage: String? = null,
    val isEditingGoal: Boolean = false,
    val isSavingGoal: Boolean = false,
)

sealed interface ProfileEvent {
    data object LoggedOut : ProfileEvent
}

/**
 * Estado hoisted de [ProfileScreen]. Conectado a datos reales:
 * `GET /api/v1/users/me` y `PUT /api/v1/users/me/goal` (único campo editable
 * que expone el backend). Logout limpia [AuthRepository] (TokenStore).
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events: SharedFlow<ProfileEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            userRepository.getProfile()
                .onSuccess { profile -> _uiState.update { it.copy(isLoading = false, profile = profile) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo cargar tu perfil.") } }
        }
    }

    fun openEditGoal() = _uiState.update { it.copy(isEditingGoal = true) }
    fun closeEditGoal() = _uiState.update { it.copy(isEditingGoal = false) }

    fun updateGoal(goal: SportGoal) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingGoal = true) }
            userRepository.updateSportGoal(goal)
                .onSuccess { profile -> _uiState.update { it.copy(isSavingGoal = false, isEditingGoal = false, profile = profile) } }
                .onFailure { _uiState.update { it.copy(isSavingGoal = false, errorMessage = "No se pudo actualizar tu objetivo.") } }
        }
    }

    fun logout() {
        authRepository.logout()
        viewModelScope.launch { _events.emit(ProfileEvent.LoggedOut) }
    }
}
