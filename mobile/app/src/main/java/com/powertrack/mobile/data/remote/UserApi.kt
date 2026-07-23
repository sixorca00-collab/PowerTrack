package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.UpdateSportGoalRequestDto
import com.powertrack.mobile.data.remote.dto.UserProfileDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

/** Endpoints bajo `/api/v1/users` (Docs/api-endpoints.md). */
interface UserApi {

    @GET("api/v1/users/me")
    suspend fun getMyProfile(): UserProfileDto

    @PUT("api/v1/users/me/goal")
    suspend fun updateSportGoal(@Body request: UpdateSportGoalRequestDto): UserProfileDto
}
