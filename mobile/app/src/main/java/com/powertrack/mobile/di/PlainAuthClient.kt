package com.powertrack.mobile.di

import javax.inject.Qualifier

/**
 * Marca la instancia de [com.powertrack.mobile.data.remote.AuthApi]
 * construida sobre un `OkHttpClient` SIN [com.powertrack.mobile.data.remote.AuthAuthenticator]
 * adjunto. Se usa exclusivamente dentro del propio authenticator para la
 * llamada de refresh (ver comentario en NetworkModule).
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PlainAuthClient
