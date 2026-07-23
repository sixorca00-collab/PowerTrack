package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Body de `POST /api/v1/auth/login` (Docs/api-endpoints.md). */
@Serializable
data class LoginRequestDto(
    val email: String,
    val password: String,
)
