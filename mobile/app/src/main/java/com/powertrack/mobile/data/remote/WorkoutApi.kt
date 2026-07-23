package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.FinishWorkoutSessionRequestDto
import com.powertrack.mobile.data.remote.dto.FinishWorkoutSessionResponseDto
import com.powertrack.mobile.data.remote.dto.PreviousLogDto
import com.powertrack.mobile.data.remote.dto.StartWorkoutSessionRequestDto
import com.powertrack.mobile.data.remote.dto.StartWorkoutSessionResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints bajo `/api/v1/workouts` (Docs/api-endpoints.md). */
interface WorkoutApi {

    @POST("api/v1/workouts/session/start")
    suspend fun startSession(@Body request: StartWorkoutSessionRequestDto): StartWorkoutSessionResponseDto

    /**
     * `Response<PreviousLogDto>` (no el DTO "pelado") porque el backend
     * responde `204 No Content` sin body cuando no hay histórico — ver
     * `WorkoutController#previousLog`. Retrofit trata 204/205 como body nulo
     * sin invocar el converter, así que `response.body()` es `null` en ese
     * caso, nunca una excepción de parseo.
     */
    @GET("api/v1/workouts/previous-log")
    suspend fun getPreviousLog(@Query("routineExerciseId") routineExerciseId: String): Response<PreviousLogDto>

    @POST("api/v1/workouts/session/{sessionId}/finish")
    suspend fun finishSession(
        @Path("sessionId") sessionId: String,
        @Body request: FinishWorkoutSessionRequestDto,
    ): FinishWorkoutSessionResponseDto
}
