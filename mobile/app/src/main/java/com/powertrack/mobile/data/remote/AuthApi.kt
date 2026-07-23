package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.AuthResponseDto
import com.powertrack.mobile.data.remote.dto.LoginRequestDto
import com.powertrack.mobile.data.remote.dto.RefreshTokenRequestDto
import com.powertrack.mobile.data.remote.dto.RegisterRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Endpoints bajo `/api/v1/auth/` (Docs/api-endpoints.md). Ninguno requiere
 * `Authorization` header (acceso libre: ese doc aclara que las rutas de
 * autenticación no exigen el header, a diferencia del resto de la API).
 */
interface AuthApi {

    @POST("api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequestDto): AuthResponseDto

    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequestDto): AuthResponseDto

    @POST("api/v1/auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequestDto): AuthResponseDto
}
