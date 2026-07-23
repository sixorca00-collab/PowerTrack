package com.powertrack.mobile.ui.workout

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powertrack.mobile.data.remote.dto.ExerciseLogRequestDto
import com.powertrack.mobile.data.remote.dto.OverallFeelingDto
import com.powertrack.mobile.data.remote.dto.RoutineExerciseDto
import com.powertrack.mobile.data.remote.dto.SetLogRequestDto
import com.powertrack.mobile.data.repository.RoutineRepository
import com.powertrack.mobile.data.repository.WorkoutRepository
import com.powertrack.mobile.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

data class SetEntry(
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Int,
    val completed: Boolean = false,
)

data class ExerciseTrackState(
    val routineExerciseId: String,
    val exerciseName: String,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    /** Referencia real de la última sesión (`GET /workouts/previous-log`), o `null` sin histórico. */
    val previousSummary: String?,
    val sets: List<SetEntry>,
)

data class ExerciseSuggestionUi(val exerciseName: String, val recommendationLabel: String, val message: String)

sealed interface LiveTrackerPhase {
    data object Loading : LiveTrackerPhase
    data class Tracking(val exercises: List<ExerciseTrackState>, val currentIndex: Int) : LiveTrackerPhase
    data class ChoosingFeeling(val exercises: List<ExerciseTrackState>, val errorMessage: String? = null) : LiveTrackerPhase
    data class Completed(val suggestions: List<ExerciseSuggestionUi>) : LiveTrackerPhase
    data class Error(val message: String) : LiveTrackerPhase
}

/**
 * Estado hoisted del Live Tracker (Docs/documentacion_funcional_tecnica_fitness_mvp.md
 * RF-04/US-03/US-05; Docs/propuesta-modulos-rutinas-y-registro.md Módulo 2).
 * Flujo: un ejercicio a la vez -> precarga desde `GET /workouts/previous-log`
 * -> el usuario ajusta con steppers (+/-) y marca "Done" (1-2 toques por
 * serie) -> al terminar el último ejercicio, elige sensación general ->
 * `POST /workouts/session/{id}/finish` (real) -> muestra las sugerencias de
 * progresión que devuelve el backend (reales, `ProgressionRuleEngine`).
 *
 * Solo se envían al backend las series marcadas como completadas; un
 * ejercicio sin ninguna serie marcada se omite del `finish` (se asume
 * salteado). Sin caché local (Room queda fuera de alcance, ver
 * Docs/decisiones-tecnicas.md): si `finishSession` falla por red, se vuelve
 * a la pantalla de selección de sensación para reintentar, sin bloquear con
 * un diálogo (principio offline-first).
 */
@HiltViewModel
class LiveTrackerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val routineRepository: RoutineRepository,
    private val workoutRepository: WorkoutRepository,
) : ViewModel() {

    private val routineId: String = checkNotNull(savedStateHandle[Screen.LiveTracker.ARG_ROUTINE_ID])
    private val routineDayId: String = checkNotNull(savedStateHandle[Screen.LiveTracker.ARG_ROUTINE_DAY_ID])
    private val sessionId: String = checkNotNull(savedStateHandle[Screen.LiveTracker.ARG_SESSION_ID])

    private val _phase = MutableStateFlow<LiveTrackerPhase>(LiveTrackerPhase.Loading)
    val phase: StateFlow<LiveTrackerPhase> = _phase.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _phase.value = LiveTrackerPhase.Loading
            routineRepository.getRoutine(routineId)
                .onSuccess { detail ->
                    val day = detail.days.find { it.id == routineDayId }
                    if (day == null) {
                        _phase.value = LiveTrackerPhase.Error("No se encontró el día de la rutina.")
                        return@onSuccess
                    }
                    val exercises = day.exercises.sortedBy { it.orderIndex }.map { buildInitialState(it) }
                    _phase.value = LiveTrackerPhase.Tracking(exercises, currentIndex = 0)
                }
                .onFailure { _phase.value = LiveTrackerPhase.Error("No se pudo cargar la rutina. Revisá tu conexión.") }
        }
    }

    private suspend fun buildInitialState(exercise: RoutineExerciseDto): ExerciseTrackState {
        val previous = workoutRepository.previousLog(exercise.id)
        val lastKnownSet = previous?.sets?.maxByOrNull { it.setNumber }
        val sets = (1..exercise.targetSets).map { setNumber ->
            val matchingSet = previous?.sets?.find { it.setNumber == setNumber } ?: lastKnownSet
            SetEntry(
                setNumber = setNumber,
                weightKg = matchingSet?.weightKg ?: 0.0,
                reps = matchingSet?.repsCompleted ?: exercise.targetRepMin,
                rpe = matchingSet?.rpe ?: DEFAULT_RPE,
            )
        }
        val previousSummary = lastKnownSet?.let { "Último: ${formatWeight(it.weightKg)}kg x ${it.repsCompleted} @RPE${it.rpe}" }
        return ExerciseTrackState(
            routineExerciseId = exercise.id,
            exerciseName = exercise.exerciseName,
            targetSets = exercise.targetSets,
            targetRepMin = exercise.targetRepMin,
            targetRepMax = exercise.targetRepMax,
            previousSummary = previousSummary,
            sets = sets,
        )
    }

    fun adjustWeight(exerciseIndex: Int, setIndex: Int, delta: Double) = updateSet(exerciseIndex, setIndex) {
        it.copy(weightKg = (it.weightKg + delta).coerceAtLeast(0.0))
    }

    fun adjustReps(exerciseIndex: Int, setIndex: Int, delta: Int) = updateSet(exerciseIndex, setIndex) {
        it.copy(reps = (it.reps + delta).coerceAtLeast(0))
    }

    fun adjustRpe(exerciseIndex: Int, setIndex: Int, delta: Int) = updateSet(exerciseIndex, setIndex) {
        it.copy(rpe = (it.rpe + delta).coerceIn(1, 10))
    }

    fun toggleSetCompleted(exerciseIndex: Int, setIndex: Int) = updateSet(exerciseIndex, setIndex) {
        it.copy(completed = !it.completed)
    }

    private inline fun updateSet(exerciseIndex: Int, setIndex: Int, transform: (SetEntry) -> SetEntry) {
        val current = _phase.value as? LiveTrackerPhase.Tracking ?: return
        val updatedExercises = current.exercises.mapIndexed { exIdx, exercise ->
            if (exIdx != exerciseIndex) {
                exercise
            } else {
                exercise.copy(sets = exercise.sets.mapIndexed { setIdx, set -> if (setIdx == setIndex) transform(set) else set })
            }
        }
        _phase.value = current.copy(exercises = updatedExercises)
    }

    fun goToNext() {
        val current = _phase.value as? LiveTrackerPhase.Tracking ?: return
        _phase.value = if (current.currentIndex < current.exercises.lastIndex) {
            current.copy(currentIndex = current.currentIndex + 1)
        } else {
            LiveTrackerPhase.ChoosingFeeling(current.exercises)
        }
    }

    fun goToPrevious() {
        val current = _phase.value as? LiveTrackerPhase.Tracking ?: return
        if (current.currentIndex > 0) _phase.value = current.copy(currentIndex = current.currentIndex - 1)
    }

    fun cancelFeelingSelection() {
        val current = _phase.value as? LiveTrackerPhase.ChoosingFeeling ?: return
        _phase.value = LiveTrackerPhase.Tracking(current.exercises, current.exercises.lastIndex)
    }

    fun finishSession(feeling: OverallFeelingDto) {
        val current = _phase.value as? LiveTrackerPhase.ChoosingFeeling ?: return
        val logs = current.exercises.mapNotNull { exercise ->
            val completedSets = exercise.sets.filter { it.completed }
            if (completedSets.isEmpty()) {
                null
            } else {
                ExerciseLogRequestDto(
                    routineExerciseId = exercise.routineExerciseId,
                    sets = completedSets.mapIndexed { idx, set ->
                        SetLogRequestDto(setNumber = idx + 1, weightKg = set.weightKg, repsCompleted = set.reps, rpe = set.rpe)
                    },
                )
            }
        }
        if (logs.isEmpty()) {
            _phase.value = current.copy(errorMessage = "Marcá al menos una serie como completada antes de finalizar.")
            return
        }

        viewModelScope.launch {
            workoutRepository.finishSession(sessionId, feeling, logs)
                .onSuccess { response ->
                    val nameByExercise = current.exercises.associateBy({ it.routineExerciseId }, { it.exerciseName })
                    val suggestions = response.suggestions.map { suggestion ->
                        ExerciseSuggestionUi(
                            exerciseName = nameByExercise[suggestion.routineExerciseId] ?: "Ejercicio",
                            recommendationLabel = suggestion.recommendation.displayLabel(),
                            message = suggestion.message,
                        )
                    }
                    _phase.value = LiveTrackerPhase.Completed(suggestions)
                }
                .onFailure { throwable ->
                    if ((throwable as? HttpException)?.code() == 409) {
                        // 409 en finish = la sesión ya se había completado antes (reintento
                        // idempotente exitoso, Docs/decisiones-tecnicas.md 2026-07-23), no un error.
                        _phase.value = LiveTrackerPhase.Completed(emptyList())
                    } else {
                        // No bloqueamos con un diálogo (offline-first): se queda acá para reintentar.
                        _phase.value = current.copy(errorMessage = "No se pudo sincronizar todavía. Volvé a intentar.")
                    }
                }
        }
    }

    private fun formatWeight(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()

    private companion object {
        const val DEFAULT_RPE = 7
    }
}
