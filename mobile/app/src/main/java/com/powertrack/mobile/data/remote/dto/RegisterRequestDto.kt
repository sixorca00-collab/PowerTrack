package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Body de `POST /api/v1/auth/register` (Docs/api-endpoints.md). */
@Serializable
data class RegisterRequestDto(
    val email: String,
    val password: String,
    val fullName: String,
    val sportGoal: SportGoal,
)
