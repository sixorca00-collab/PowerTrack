package com.powertrack.mobile.data.repository

import com.powertrack.mobile.data.remote.AuthApi
import com.powertrack.mobile.data.remote.dto.AuthResponseDto
import com.powertrack.mobile.data.remote.dto.LoginRequestDto
import com.powertrack.mobile.data.remote.dto.RegisterRequestDto
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.data.session.TokenStore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fachada de Auth para el resto de la app: habla con [AuthApi] y persiste
 * la sesión resultante en [TokenStore]. `login`/`register` usan el
 * Retrofit "autenticado" (con [com.powertrack.mobile.data.remote.AuthAuthenticator]
 * adjunto), lo cual es inofensivo acá porque ninguna de las dos llamadas
 * envía Authorization de entrada.
 */
@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) {

    suspend fun register(
        email: String,
        password: String,
        fullName: String,
        sportGoal: SportGoal,
    ): Result<AuthResponseDto> = runCatching {
        val response = authApi.register(RegisterRequestDto(email, password, fullName, sportGoal))
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
        response
    }

    suspend fun login(email: String, password: String): Result<AuthResponseDto> = runCatching {
        val response = authApi.login(LoginRequestDto(email, password))
        tokenStore.saveTokens(response.accessToken, response.refreshToken)
        response
    }

    fun isLoggedIn(): Boolean = tokenStore.hasSession()

    fun logout() = tokenStore.clearTokens()
}
