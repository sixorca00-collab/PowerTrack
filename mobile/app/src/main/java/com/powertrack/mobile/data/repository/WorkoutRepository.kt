package com.powertrack.mobile.data.repository

import com.powertrack.mobile.data.remote.WorkoutApi
import com.powertrack.mobile.data.remote.dto.ExerciseLogRequestDto
import com.powertrack.mobile.data.remote.dto.FinishWorkoutSessionRequestDto
import com.powertrack.mobile.data.remote.dto.FinishWorkoutSessionResponseDto
import com.powertrack.mobile.data.remote.dto.OverallFeelingDto
import com.powertrack.mobile.data.remote.dto.PreviousLogDto
import com.powertrack.mobile.data.remote.dto.StartWorkoutSessionRequestDto
import com.powertrack.mobile.data.remote.dto.StartWorkoutSessionResponseDto
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada del Live Tracker (Docs/api-endpoints.md, módulo Registro/Progreso).
 * `sessionId`/`startTime`/`endTime` los genera/reporta el cliente (nunca el
 * servidor) — ver Docs/decisiones-tecnicas.md, entrada 2026-07-23.
 */
@Singleton
class WorkoutRepository @Inject constructor(private val workoutApi: WorkoutApi) {

    suspend fun startSession(routineDayId: String): Result<StartWorkoutSessionResponseDto> = runCatching {
        val request = StartWorkoutSessionRequestDto(
            sessionId = UUID.randomUUID().toString(),
            routineDayId = routineDayId,
            startTime = Instant.now().toString(),
        )
        workoutApi.startSession(request)
    }

    /** `null` tanto si no hay histórico (204) como fallback silencioso ante error de red. */
    suspend fun previousLog(routineExerciseId: String): PreviousLogDto? = runCatching {
        workoutApi.getPreviousLog(routineExerciseId).body()
    }.getOrNull()

    suspend fun finishSession(
        sessionId: String,
        overallFeeling: OverallFeelingDto,
        exerciseLogs: List<ExerciseLogRequestDto>,
    ): Result<FinishWorkoutSessionResponseDto> = runCatching {
        val request = FinishWorkoutSessionRequestDto(
            overallFeeling = overallFeeling,
            exerciseLogs = exerciseLogs,
            endTime = Instant.now().toString(),
        )
        workoutApi.finishSession(sessionId, request)
    }
}
