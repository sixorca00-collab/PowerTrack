package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Shapes del módulo Registro/Progreso (Docs/api-endpoints.md,
 * Docs/decisiones-tecnicas.md 2026-07-23). `sessionId`/`startTime`/`endTime`
 * los genera/reporta el cliente, nunca el servidor.
 */
@Serializable
data class StartWorkoutSessionRequestDto(
    val sessionId: String,
    val routineDayId: String,
    val startTime: String,
)

@Serializable
data class StartWorkoutSessionResponseDto(
    val sessionId: String,
    val routineDayId: String,
    val startTime: String,
)

@Serializable
data class PreviousLogDto(
    val workoutLogId: String,
    val routineExerciseId: String,
    val performedAt: String,
    val notes: String? = null,
    val sets: List<PreviousLogSetDto>,
)

@Serializable
data class PreviousLogSetDto(
    val setNumber: Int,
    val weightKg: Double,
    val repsCompleted: Int,
    val rpe: Int,
)

/** Calcado de `com.powertrack.backend.domain.workout.OverallFeeling`. */
@Serializable
enum class OverallFeelingDto {
    MALA,
    REGULAR,
    BUENA,
    EXCELENTE,
}

/** Body de `POST /api/v1/workouts/session/{id}/finish`. */
@Serializable
data class FinishWorkoutSessionRequestDto(
    val overallFeeling: OverallFeelingDto,
    val exerciseLogs: List<ExerciseLogRequestDto>,
    val endTime: String,
)

@Serializable
data class ExerciseLogRequestDto(
    val routineExerciseId: String,
    val notes: String? = null,
    val sets: List<SetLogRequestDto>,
)

@Serializable
data class SetLogRequestDto(
    val setNumber: Int,
    val weightKg: Double,
    val repsCompleted: Int,
    val rpe: Int,
)

/** Calcado de `com.powertrack.backend.domain.workout.Recommendation`. */
@Serializable
enum class RecommendationDto {
    INCREASE_WEIGHT,
    INCREASE_REPS,
    MAINTAIN,
    DECREASE_WEIGHT,
    DELOAD,
}

@Serializable
data class FinishWorkoutSessionResponseDto(
    val sessionId: String,
    val startTime: String,
    val endTime: String,
    val overallFeeling: OverallFeelingDto,
    val suggestions: List<ExerciseSuggestionDto>,
)

@Serializable
data class ExerciseSuggestionDto(
    val routineExerciseId: String,
    val recommendation: RecommendationDto,
    val message: String,
)
