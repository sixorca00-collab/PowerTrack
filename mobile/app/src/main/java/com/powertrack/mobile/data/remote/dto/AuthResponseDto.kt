package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Response compartido por `register`, `login` y `refresh`
 * (Docs/api-endpoints.md: "register/login devuelven { userId, email,
 * accessToken, refreshToken }"; `refresh` responde con el mismo shape).
 *
 * `userId` viaja como String (Jackson serializa `UUID` como texto plano en
 * el backend); no se parsea a `java.util.UUID` en este scaffold para no
 * añadir un serializer custom antes de que haga falta.
 */
@Serializable
data class AuthResponseDto(
    val userId: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
)
