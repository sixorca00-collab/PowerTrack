package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.remote.dto.RefreshTokenRequestDto
import com.powertrack.mobile.data.session.TokenStore
import com.powertrack.mobile.di.PlainAuthClient
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Flujo de refresh de PRD §13.1: "Authenticator personalizado de Retrofit
 * que detecta respuestas HTTP 401 y solicita automáticamente la renovación
 * con el refreshToken sin interrumpir la experiencia del usuario".
 *
 * Importante: [plainAuthApi] se construye (ver [di.NetworkModule]) con un
 * `OkHttpClient` que NO tiene este mismo authenticator adjunto. Si
 * reutilizáramos el cliente autenticado para la llamada de refresh, un
 * refreshToken inválido volvería a disparar este authenticator sobre la
 * propia respuesta 401 del refresh -> loop infinito.
 */
@Singleton
class AuthAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    @PlainAuthClient private val plainAuthApi: AuthApi,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Solo nos interesa reintentar requests que ya llevaban un Bearer
        // token (es decir, llamadas autenticadas de verdad). Los propios
        // endpoints de /auth/** (login/register/refresh) no envían
        // Authorization, así que un 401 de credenciales inválidas en login
        // nunca debe disparar un intento de refresh.
        val failedAccessToken = response.request.header("Authorization")
            ?.removePrefix("Bearer ")
            ?: return null

        // Corta reintentos infinitos si el request original ya falló varias
        // veces encadenadas (p.ej. el backend sigue devolviendo 401 incluso
        // con un access token nuevo).
        if (responseCount(response) > MAX_RETRIES) return null

        val newAccessToken = synchronized(this) {
            // Otra request concurrente puede haber refrescado mientras esta
            // esperaba el lock: si el access token guardado ya cambió
            // respecto al que falló, lo reusamos sin llamar de nuevo a
            // /auth/refresh.
            val currentAccessToken = tokenStore.getAccessToken()
            if (currentAccessToken != null && currentAccessToken != failedAccessToken) {
                currentAccessToken
            } else {
                refreshTokens()
            }
        } ?: run {
            // El refreshToken también es inválido/expiró: no hay forma de
            // recuperar la sesión sin que el usuario vuelva a loguearse.
            tokenStore.clearTokens()
            return null
        }

        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }

    /** @return el nuevo accessToken persistido, o `null` si el refresh falló. */
    private fun refreshTokens(): String? {
        val refreshToken = tokenStore.getRefreshToken() ?: return null
        return runCatching {
            // Authenticator.authenticate() es una API síncrona de OkHttp,
            // ejecutada en el propio hilo de la llamada de red -> bloquear
            // acá es el uso esperado.
            val authResponse = runBlocking { plainAuthApi.refresh(RefreshTokenRequestDto(refreshToken)) }
            tokenStore.saveTokens(authResponse.accessToken, authResponse.refreshToken)
            authResponse.accessToken
        }.getOrNull()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }

    private companion object {
        const val MAX_RETRIES = 3
    }
}
