package com.powertrack.mobile.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

enum class AuthMode { LOGIN, REGISTER }

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val sportGoal: SportGoal = SportGoal.FUERZA,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isSubmitEnabled: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && (mode == AuthMode.LOGIN || fullName.isNotBlank())
}

sealed interface AuthEvent {
    data object Authenticated : AuthEvent
}

/**
 * Estado hoisted de [AuthScreen] (login + registro en una sola pantalla,
 * alternable con [toggleMode] -- el mockup de Docs/vistas-mockups.md solo
 * muestra login; el modo registro es una extensión necesaria para cubrir
 * RF-01, ver nota de decisión en la respuesta final del agente).
 *
 * Reutiliza [AuthRepository] (ya existente, habla con `AuthApi` + persiste
 * en `TokenStore`); no duplica esa lógica.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AuthEvent>()
    val events: SharedFlow<AuthEvent> = _events.asSharedFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, errorMessage = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, errorMessage = null) }
    }

    fun onFullNameChange(value: String) {
        _uiState.update { it.copy(fullName = value, errorMessage = null) }
    }

    fun onSportGoalChange(goal: SportGoal) {
        _uiState.update { it.copy(sportGoal = goal) }
    }

    fun togglePasswordVisibility() {
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                mode = if (it.mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN,
                errorMessage = null,
            )
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.isSubmitEnabled || state.isLoading) return
        if (state.mode == AuthMode.REGISTER && state.password.length < MIN_PASSWORD_LENGTH) {
            _uiState.update { it.copy(errorMessage = "La clave debe tener al menos 8 caracteres.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = if (state.mode == AuthMode.LOGIN) {
                authRepository.login(state.email.trim(), state.password)
            } else {
                authRepository.register(state.email.trim(), state.password, state.fullName.trim(), state.sportGoal)
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.emit(AuthEvent.Authenticated)
                }
                .onFailure { throwable ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapError(throwable, state.mode)) }
                }
        }
    }

    private fun mapError(throwable: Throwable, mode: AuthMode): String {
        val httpCode = (throwable as? HttpException)?.code()
        return when {
            httpCode == 401 -> "Email o clave incorrectos."
            httpCode == 409 -> "Ese email ya está registrado."
            httpCode == 400 -> "Revisá los datos ingresados."
            throwable is IOException -> "Sin conexión. Probá de nuevo en un momento."
            mode == AuthMode.LOGIN -> "No se pudo iniciar sesión. Intentá de nuevo."
            else -> "No se pudo completar el registro. Intentá de nuevo."
        }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 8
    }
}
