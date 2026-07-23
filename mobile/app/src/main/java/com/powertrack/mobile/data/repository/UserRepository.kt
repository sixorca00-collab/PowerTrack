package com.powertrack.mobile.data.repository

import com.powertrack.mobile.data.remote.UserApi
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.data.remote.dto.UpdateSportGoalRequestDto
import com.powertrack.mobile.data.remote.dto.UserProfileDto
import javax.inject.Inject
import javax.inject.Singleton

/** Fachada de Perfil de Usuario (`GET/PUT /api/v1/users/me*`). */
@Singleton
class UserRepository @Inject constructor(private val userApi: UserApi) {

    suspend fun getProfile(): Result<UserProfileDto> = runCatching { userApi.getMyProfile() }

    suspend fun updateSportGoal(goal: SportGoal): Result<UserProfileDto> = runCatching {
        userApi.updateSportGoal(UpdateSportGoalRequestDto(goal))
    }
}
