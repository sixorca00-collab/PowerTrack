package com.powertrack.mobile.ui.routines.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.CreateRoutineDayRequestDto
import com.powertrack.mobile.data.remote.dto.CreateRoutineExerciseRequestDto
import com.powertrack.mobile.data.remote.dto.CreateRoutineRequestDto
import com.powertrack.mobile.data.remote.dto.ExerciseDto
import com.powertrack.mobile.data.repository.ExerciseRepository
import com.powertrack.mobile.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RoutineCreateExerciseUi(
    val localId: String = UUID.randomUUID().toString(),
    val exerciseId: String,
    val exerciseName: String,
    val targetSets: Int = DEFAULT_SETS,
    val targetRepMin: Int = DEFAULT_REP_MIN,
    val targetRepMax: Int = DEFAULT_REP_MAX,
) {
    companion object {
        const val DEFAULT_SETS = 3
        const val DEFAULT_REP_MIN = 8
        const val DEFAULT_REP_MAX = 12
    }
}

data class RoutineCreateDayUi(
    val localId: String = UUID.randomUUID().toString(),
    val dayName: String,
    val exercises: List<RoutineCreateExerciseUi> = emptyList(),
)

data class RoutineCreateUiState(
    val name: String = "",
    val description: String = "",
    val days: List<RoutineCreateDayUi> = listOf(RoutineCreateDayUi(dayName = "Día A")),
    val catalog: List<ExerciseDto> = emptyList(),
    val isLoadingCatalog: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val exercisePickerForDayId: String? = null,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && days.isNotEmpty() && days.all { it.exercises.isNotEmpty() }
}

sealed interface RoutineCreateEvent {
    data object RoutineCreated : RoutineCreateEvent
}

/**
 * Estado hoisted de la pantalla "Crear Rutina" (Módulo 1 de
 * Docs/propuesta-modulos-rutinas-y-registro.md: nombre -> días -> ejercicios
 * por día, sin campos obligatorios más allá del nombre y al menos 1
 * ejercicio por día -- el backend exige `@NotEmpty` en ambos niveles).
 *
 * El catálogo de ejercicios ([ExerciseRepository]) se carga una vez al
 * entrar; elegir un ejercicio del catálogo agrega una fila editable
 * (series/rango de reps) al día actual.
 */
@HiltViewModel
class RoutineCreateViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val exerciseRepository: ExerciseRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineCreateUiState())
    val uiState: StateFlow<RoutineCreateUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RoutineCreateEvent>()
    val events: SharedFlow<RoutineCreateEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            exerciseRepository.listExercises()
                .onSuccess { catalog -> _uiState.update { it.copy(isLoadingCatalog = false, catalog = catalog) } }
                .onFailure {
                    _uiState.update {
                        it.copy(isLoadingCatalog = false, errorMessage = "No se pudo cargar el catálogo de ejercicios.")
                    }
                }
        }
    }

    fun updateName(value: String) = _uiState.update { it.copy(name = value) }
    fun updateDescription(value: String) = _uiState.update { it.copy(description = value) }

    fun addDay() = _uiState.update { state ->
        val nextLetter = ('A' + state.days.size)
        state.copy(days = state.days + RoutineCreateDayUi(dayName = "Día $nextLetter"))
    }

    fun removeDay(dayId: String) = _uiState.update { state ->
        if (state.days.size <= 1) state else state.copy(days = state.days.filterNot { it.localId == dayId })
    }

    fun updateDayName(dayId: String, name: String) = updateDay(dayId) { it.copy(dayName = name) }

    fun openExercisePicker(dayId: String) = _uiState.update { it.copy(exercisePickerForDayId = dayId) }
    fun closeExercisePicker() = _uiState.update { it.copy(exercisePickerForDayId = null) }

    fun addExerciseToDay(dayId: String, exercise: ExerciseDto) {
        updateDay(dayId) { day ->
            day.copy(exercises = day.exercises + RoutineCreateExerciseUi(exerciseId = exercise.id, exerciseName = exercise.name))
        }
        closeExercisePicker()
    }

    fun removeExerciseFromDay(dayId: String, exerciseLocalId: String) = updateDay(dayId) { day ->
        day.copy(exercises = day.exercises.filterNot { it.localId == exerciseLocalId })
    }

    fun updateTargetSets(dayId: String, exerciseLocalId: String, delta: Int) =
        updateExercise(dayId, exerciseLocalId) { it.copy(targetSets = (it.targetSets + delta).coerceIn(1, 10)) }

    fun updateTargetRepMin(dayId: String, exerciseLocalId: String, delta: Int) =
        updateExercise(dayId, exerciseLocalId) { exercise ->
            val newMin = (exercise.targetRepMin + delta).coerceIn(1, exercise.targetRepMax)
            exercise.copy(targetRepMin = newMin)
        }

    fun updateTargetRepMax(dayId: String, exerciseLocalId: String, delta: Int) =
        updateExercise(dayId, exerciseLocalId) { exercise ->
            val newMax = (exercise.targetRepMax + delta).coerceIn(exercise.targetRepMin, 50)
            exercise.copy(targetRepMax = newMax)
        }

    fun save() {
        val state = _uiState.value
        if (!state.canSave || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }
            val request = CreateRoutineRequestDto(
                routineId = UUID.randomUUID().toString(),
                name = state.name.trim(),
                description = state.description.trim().ifBlank { null },
                days = state.days.mapIndexed { dayIndex, day ->
                    CreateRoutineDayRequestDto(
                        dayName = day.dayName,
                        orderIndex = dayIndex,
                        exercises = day.exercises.mapIndexed { exerciseIndex, exercise ->
                            CreateRoutineExerciseRequestDto(
                                exerciseId = exercise.exerciseId,
                                orderIndex = exerciseIndex,
                                targetSets = exercise.targetSets,
                                targetRepMin = exercise.targetRepMin,
                                targetRepMax = exercise.targetRepMax,
                            )
                        },
                    )
                },
            )
            routineRepository.createRoutine(request)
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false) }
                    _events.emit(RoutineCreateEvent.RoutineCreated)
                }
                .onFailure {
                    _uiState.update { it.copy(isSaving = false, errorMessage = "No se pudo crear la rutina. Reintentá.") }
                }
        }
    }

    private inline fun updateDay(dayId: String, transform: (RoutineCreateDayUi) -> RoutineCreateDayUi) {
        _uiState.update { state ->
            state.copy(days = state.days.map { if (it.localId == dayId) transform(it) else it })
        }
    }

    private inline fun updateExercise(
        dayId: String,
        exerciseLocalId: String,
        transform: (RoutineCreateExerciseUi) -> RoutineCreateExerciseUi,
    ) = updateDay(dayId) { day ->
        day.copy(exercises = day.exercises.map { if (it.localId == exerciseLocalId) transform(it) else it })
    }
}
