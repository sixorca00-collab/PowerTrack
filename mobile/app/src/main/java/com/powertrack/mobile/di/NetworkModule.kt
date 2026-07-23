package com.powertrack.mobile.di

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.powertrack.mobile.BuildConfig
import com.powertrack.mobile.data.remote.AuthApi
import com.powertrack.mobile.data.remote.AuthAuthenticator
import com.powertrack.mobile.data.remote.AuthInterceptor
import com.powertrack.mobile.data.remote.ExerciseApi
import com.powertrack.mobile.data.remote.RoutineApi
import com.powertrack.mobile.data.remote.UserApi
import com.powertrack.mobile.data.remote.WorkoutApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Grafo de red de Auth. Dos `OkHttpClient` a propósito:
 *
 * - [providePlainAuthApi]: SIN [AuthAuthenticator] adjunto. Se usa
 *   únicamente dentro del propio authenticator para llamar a
 *   `/auth/refresh` (ver [AuthAuthenticator] para el porqué de evitar el
 *   loop).
 * - [provideOkHttpClient] (el cliente "app"): con [AuthAuthenticator]
 *   adjunto. Es el que usa el resto de la app (incluida la [AuthApi] que
 *   inyecta `AuthRepository` para login/register).
 *
 * [com.powertrack.mobile.data.session.TokenStore] y [AuthAuthenticator] NO
 * se proveen acá: ambos tienen su propio constructor `@Inject` y Hilt los
 * resuelve por constructor injection (proveerlos también acá produciría un
 * binding duplicado).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val CONNECT_TIMEOUT_SECONDS = 15L

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    }

    @Provides
    @Singleton
    @PlainAuthClient
    fun providePlainAuthApi(json: Json, loggingInterceptor: HttpLoggingInterceptor): AuthApi {
        val plainClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
        return buildRetrofit(plainClient, json).create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        authAuthenticator: AuthAuthenticator,
    ): OkHttpClient = OkHttpClient.Builder()
        // Orden importa: authInterceptor adjunta el Bearer vigente ANTES de
        // salir; authAuthenticator solo entra en juego reactivamente si el
        // backend responde 401 (token vencido), para refrescarlo y reintentar.
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .authenticator(authAuthenticator)
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = buildRetrofit(okHttpClient, json)

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideRoutineApi(retrofit: Retrofit): RoutineApi = retrofit.create(RoutineApi::class.java)

    @Provides
    @Singleton
    fun provideExerciseApi(retrofit: Retrofit): ExerciseApi = retrofit.create(ExerciseApi::class.java)

    @Provides
    @Singleton
    fun provideWorkoutApi(retrofit: Retrofit): WorkoutApi = retrofit.create(WorkoutApi::class.java)

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)

    private fun buildRetrofit(client: OkHttpClient, json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            // BuildConfig.BASE_URL: "http://10.0.2.2:8081/" en debug (10.0.2.2 =
            // localhost del host visto desde el emulador; backend en :8081,
            // ver backend/src/main/resources/application-dev.yml). Cleartext
            // permitido solo para ese host vía src/debug/res/xml/network_security_config.xml.
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}
