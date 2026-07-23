package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Calcado de `ExerciseResponse` (backend): catálogo de ejercicios (sin targets). */
@Serializable
data class ExerciseDto(
    val id: String,
    val name: String,
    val targetMuscle: String,
    val category: String,
    val predefined: Boolean,
)

/** Body de `POST /api/v1/exercises` (crear ejercicio personalizado). */
@Serializable
data class CreateExerciseRequestDto(
    val name: String,
    val targetMuscle: String,
    val category: String,
)
