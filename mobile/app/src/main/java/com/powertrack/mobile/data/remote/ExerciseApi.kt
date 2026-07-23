package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.CreateExerciseRequestDto
import com.powertrack.mobile.data.remote.dto.ExerciseDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

/** Endpoints bajo `/api/v1/exercises` (Docs/api-endpoints.md). Catálogo para armar rutinas. */
interface ExerciseApi {

    @GET("api/v1/exercises")
    suspend fun listExercises(): List<ExerciseDto>

    @POST("api/v1/exercises")
    suspend fun createExercise(@Body request: CreateExerciseRequestDto): ExerciseDto
}
