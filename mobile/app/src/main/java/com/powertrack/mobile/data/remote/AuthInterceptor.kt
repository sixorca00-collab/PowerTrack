package com.powertrack.mobile.data.remote

import com.powertrack.mobile.data.session.TokenStore
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * Adjunta `Authorization: Bearer <accessToken>` a toda request saliente que
 * NO apunte al prefijo `/api/v1/auth/` (esas rutas son de acceso libre, ver
 * [AuthApi]). Todo lo demás (Rutinas, Ejercicios, Workout, Perfil) lo exige
 * el backend (Docs/api-endpoints.md: "403 si falta").
 *
 * Pieza que faltaba en el scaffold original: [AuthAuthenticator] solo
 * reacciona a un 401 sobre una request que YA llevaba un Bearer token (para
 * refrescarlo) — pero nada adjuntaba ese header en el request inicial. Era
 * invisible mientras el único consumidor de la Retrofit "autenticada" era
 * [AuthApi] (login/register/refresh no lo necesitan); se vuelve necesario
 * ahora que Rutinas/Workout/Perfil sí lo requieren.
 */
class AuthInterceptor @Inject constructor(private val tokenStore: TokenStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.url.encodedPath.contains(AUTH_PATH_SEGMENT)) {
            return chain.proceed(original)
        }

        val accessToken = tokenStore.getAccessToken() ?: return chain.proceed(original)
        val authorized = original.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(authorized)
    }

    private companion object {
        const val AUTH_PATH_SEGMENT = "/api/v1/auth/"
    }
}
