package com.powertrack.mobile.data.repository

import com.powertrack.mobile.data.remote.ExerciseApi
import com.powertrack.mobile.data.remote.dto.CreateExerciseRequestDto
import com.powertrack.mobile.data.remote.dto.ExerciseDto
import javax.inject.Inject
import javax.inject.Singleton

/** Fachada del catálogo de ejercicios, usado por el creador de rutinas. */
@Singleton
class ExerciseRepository @Inject constructor(private val exerciseApi: ExerciseApi) {

    suspend fun listExercises(): Result<List<ExerciseDto>> = runCatching { exerciseApi.listExercises() }

    suspend fun createExercise(name: String, targetMuscle: String, category: String): Result<ExerciseDto> =
        runCatching { exerciseApi.createExercise(CreateExerciseRequestDto(name, targetMuscle, category)) }
}
