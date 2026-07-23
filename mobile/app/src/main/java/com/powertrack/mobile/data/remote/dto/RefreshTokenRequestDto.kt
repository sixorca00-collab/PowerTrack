package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/** Body de `POST /api/v1/auth/refresh` (Docs/api-endpoints.md). */
@Serializable
data class RefreshTokenRequestDto(
    val refreshToken: String,
)
