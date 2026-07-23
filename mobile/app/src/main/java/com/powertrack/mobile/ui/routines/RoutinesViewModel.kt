package com.powertrack.mobile.ui.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.RoutineSummaryDto
import com.powertrack.mobile.data.repository.RoutineRepository
import com.powertrack.mobile.data.repository.WorkoutRepository
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

data class RoutinesUiState(
    val isLoading: Boolean = true,
    val routines: List<RoutineSummaryDto> = emptyList(),
    val errorMessage: String? = null,
    /** routineId cuya sesión se está iniciando (deshabilita ese botón puntual, no toda la pantalla). */
    val startingRoutineId: String? = null,
)

sealed interface RoutinesEvent {
    data class OpenRoutineDetail(val routineId: String) : RoutinesEvent
    data class OpenLiveTracker(val routineId: String, val routineDayId: String, val sessionId: String) : RoutinesEvent
}

/**
 * Estado hoisted de [RoutinesScreen]. 100% conectado a datos reales:
 * `GET /api/v1/routines` vía [RoutineRepository].
 *
 * "Start Workout" desde la lista intenta ahorrar un salto de pantalla: si la
 * rutina tiene un solo día, arranca la sesión directo
 * ([WorkoutRepository.startSession]) y navega al Live Tracker; si tiene
 * varios días, abre el Detalle de Rutina para que el usuario elija cuál
 * (ambigüedad que no se puede resolver sin esa elección explícita).
 */
@HiltViewModel
class RoutinesViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutinesUiState())
    val uiState: StateFlow<RoutinesUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RoutinesEvent>()
    val events: SharedFlow<RoutinesEvent> = _events.asSharedFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            routineRepository.listRoutines()
                .onSuccess { list -> _uiState.update { it.copy(isLoading = false, routines = list) } }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "No se pudieron cargar tus rutinas. Deslizá para reintentar.")
                    }
                }
        }
    }

    fun onRoutineTapped(routineId: String) {
        viewModelScope.launch { _events.emit(RoutinesEvent.OpenRoutineDetail(routineId)) }
    }

    fun onDeleteRoutine(routineId: String) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
                .onSuccess { load() }
                .onFailure { _uiState.update { it.copy(errorMessage = "No se pudo eliminar la rutina. Reintentá.") } }
        }
    }

    fun onStartWorkoutTapped(routineId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(startingRoutineId = routineId, errorMessage = null) }
            routineRepository.getRoutine(routineId)
                .onSuccess { detail ->
                    val onlyDay = detail.days.singleOrNull()
                    if (onlyDay == null) {
                        _events.emit(RoutinesEvent.OpenRoutineDetail(routineId))
                    } else {
                        workoutRepository.startSession(onlyDay.id)
                            .onSuccess { session ->
                                _events.emit(RoutinesEvent.OpenLiveTracker(routineId, onlyDay.id, session.sessionId))
                            }
                            .onFailure {
                                // Sin red o error del backend: no bloqueamos con un diálogo (principio
                                // offline-first), dejamos que el usuario reintente desde el Detalle.
                                _events.emit(RoutinesEvent.OpenRoutineDetail(routineId))
                            }
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(errorMessage = "No se pudo abrir la rutina. Reintentá.") }
                }
            _uiState.update { it.copy(startingRoutineId = null) }
        }
    }
}
