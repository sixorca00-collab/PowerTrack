package com.powertrack.mobile.ui.routines.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.RoutineDetailDto
import com.powertrack.mobile.data.repository.RoutineRepository
import com.powertrack.mobile.data.repository.WorkoutRepository
import com.powertrack.mobile.ui.navigation.Screen
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

data class RoutineDetailUiState(
    val isLoading: Boolean = true,
    val routine: RoutineDetailDto? = null,
    val errorMessage: String? = null,
    /** id del día cuya sesión se está iniciando (deshabilita solo ese botón). */
    val startingDayId: String? = null,
    val isDeleting: Boolean = false,
)

sealed interface RoutineDetailEvent {
    data class OpenLiveTracker(val routineId: String, val routineDayId: String, val sessionId: String) : RoutineDetailEvent
    data object RoutineDeleted : RoutineDetailEvent
}

/**
 * Estado hoisted de [RoutineDetailScreen]. Conectado a datos reales:
 * `GET /api/v1/routines/{id}` (detalle con días+ejercicios) y
 * `POST /api/v1/workouts/session/start` (al elegir un día para entrenar).
 */
@HiltViewModel
class RoutineDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle[Screen.RoutineDetail.ARG_ROUTINE_ID])

    private val _uiState = MutableStateFlow(RoutineDetailUiState())
    val uiState: StateFlow<RoutineDetailUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RoutineDetailEvent>()
    val events: SharedFlow<RoutineDetailEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            routineRepository.getRoutine(routineId)
                .onSuccess { detail -> _uiState.update { it.copy(isLoading = false, routine = detail) } }
                .onFailure { _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo cargar la rutina.") } }
        }
    }

    fun startWorkout(routineDayId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(startingDayId = routineDayId, errorMessage = null) }
            workoutRepository.startSession(routineDayId)
                .onSuccess { session ->
                    _events.emit(RoutineDetailEvent.OpenLiveTracker(routineId, routineDayId, session.sessionId))
                }
                .onFailure {
                    // Sin bloquear (offline-first): dejamos el botón disponible para reintentar.
                    _uiState.update { it.copy(errorMessage = "No se pudo iniciar la sesión. Reintentá.") }
                }
            _uiState.update { it.copy(startingDayId = null) }
        }
    }

    fun deleteRoutine() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            routineRepository.deleteRoutine(routineId)
                .onSuccess { _events.emit(RoutineDetailEvent.RoutineDeleted) }
                .onFailure {
                    _uiState.update { it.copy(isDeleting = false, errorMessage = "No se pudo eliminar la rutina.") }
                }
        }
    }
}
