package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Calcado de `UserProfileResponse` (`GET/PUT /api/v1/users/me*`). */
@Serializable
data class UserProfileDto(
    val id: String,
    val email: String,
    val fullName: String,
    val sportGoal: SportGoal,
    val createdAt: String,
)

/** Body de `PUT /api/v1/users/me/goal`. */
@Serializable
data class UpdateSportGoalRequestDto(
    val sportGoal: SportGoal,
)
