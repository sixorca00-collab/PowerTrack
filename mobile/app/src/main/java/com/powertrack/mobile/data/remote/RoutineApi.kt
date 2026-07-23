package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.CreateRoutineRequestDto
import com.powertrack.mobile.data.remote.dto.RoutineDetailDto
import com.powertrack.mobile.data.remote.dto.RoutineSummaryDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Endpoints bajo `/api/v1/routines` (Docs/api-endpoints.md). Todos requieren sesión. */
interface RoutineApi {

    @POST("api/v1/routines")
    suspend fun createRoutine(@Body request: CreateRoutineRequestDto): RoutineDetailDto

    @GET("api/v1/routines")
    suspend fun listRoutines(): List<RoutineSummaryDto>

    @GET("api/v1/routines/{id}")
    suspend fun getRoutine(@Path("id") id: String): RoutineDetailDto

    @DELETE("api/v1/routines/{id}")
    suspend fun deleteRoutine(@Path("id") id: String)
}
