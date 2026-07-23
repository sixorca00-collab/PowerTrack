package com.powertrack.mobile.data.repository

import com.powertrack.mobile.data.remote.RoutineApi
import com.powertrack.mobile.data.remote.dto.CreateRoutineRequestDto
import com.powertrack.mobile.data.remote.dto.RoutineDetailDto
import com.powertrack.mobile.data.remote.dto.RoutineSummaryDto
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada de Rutinas para el resto de la app. Sin caché local (Room queda
 * fuera de alcance por ahora, ver Docs/decisiones-tecnicas.md): cada llamada
 * pega directo al backend.
 */
@Singleton
class RoutineRepository @Inject constructor(private val routineApi: RoutineApi) {

    suspend fun listRoutines(): Result<List<RoutineSummaryDto>> = runCatching { routineApi.listRoutines() }

    suspend fun getRoutine(routineId: String): Result<RoutineDetailDto> = runCatching {
        routineApi.getRoutine(routineId)
    }

    suspend fun createRoutine(request: CreateRoutineRequestDto): Result<RoutineDetailDto> = runCatching {
        routineApi.createRoutine(request)
    }

    suspend fun deleteRoutine(routineId: String): Result<Unit> = runCatching {
        routineApi.deleteRoutine(routineId)
    }
}
