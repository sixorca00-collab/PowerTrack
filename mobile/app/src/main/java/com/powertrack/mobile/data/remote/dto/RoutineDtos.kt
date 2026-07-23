package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Shapes calcadas de `RoutineSummaryResponse`/`RoutineDetailResponse`
 * (backend, `infrastructure/adapter/in/web/dto/`). `id`/`exerciseId` viajan
 * como String (mismo criterio que `AuthResponseDto.userId`: el backend
 * serializa `UUID` como texto plano).
 */
@Serializable
data class RoutineSummaryDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val dayCount: Int,
)

@Serializable
data class RoutineDetailDto(
    val id: String,
    val userId: String,
    val name: String,
    val description: String? = null,
    val createdAt: String,
    val days: List<RoutineDayDto>,
)

@Serializable
data class RoutineDayDto(
    val id: String,
    val dayName: String,
    val orderIndex: Int,
    val exercises: List<RoutineExerciseDto>,
)

@Serializable
data class RoutineExerciseDto(
    val id: String,
    val exerciseId: String,
    val exerciseName: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    val restSeconds: Int? = null,
    val notes: String? = null,
)

/**
 * Body de `POST /api/v1/routines`. `routineId` lo genera el cliente
 * (Docs/decisiones-tecnicas.md, 2026-07-23): hace idempotente un reintento
 * de sincronización offline.
 */
@Serializable
data class CreateRoutineRequestDto(
    val routineId: String,
    val name: String,
    val description: String? = null,
    val days: List<CreateRoutineDayRequestDto>,
)

@Serializable
data class CreateRoutineDayRequestDto(
    val dayName: String,
    val orderIndex: Int,
    val exercises: List<CreateRoutineExerciseRequestDto>,
)

@Serializable
data class CreateRoutineExerciseRequestDto(
    val exerciseId: String,
    val orderIndex: Int,
    val targetSets: Int,
    val targetRepMin: Int,
    val targetRepMax: Int,
    val restSeconds: Int? = null,
    val notes: String? = null,
)
