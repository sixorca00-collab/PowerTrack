package com.powertrack.mobile.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Almacenamiento seguro de tokens (PRD §13.1: "Almacenamiento seguro del
 * token en Android utilizando EncryptedSharedPreferences (MasterKey
 * mediante Android Keystore)").
 *
 * Es la única fuente de verdad de la sesión: [AuthAuthenticator] y
 * `AuthRepository` leen/escriben acá, nunca mantienen los tokens en memoria
 * por su cuenta.
 */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext context: Context) {

    private val prefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    @Synchronized
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    @Synchronized
    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    @Synchronized
    fun getRefreshToken(): String? = prefs.getString(KEY_REFRESH_TOKEN, null)

    @Synchronized
    fun clearTokens() {
        prefs.edit().clear().apply()
    }

    fun hasSession(): Boolean = getRefreshToken() != null

    private companion object {
        const val PREFS_FILE_NAME = "powertrack_secure_prefs"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
