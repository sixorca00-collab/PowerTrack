package com.powertrack.mobile.ui.splash

import androidx.lifecycle.ViewModel
import com.powertrack.mobile.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * No hace llamadas de red: solo consulta si hay sesión guardada
 * ([AuthRepository.isLoggedIn], respaldado por [com.powertrack.mobile.data.session.TokenStore])
 * para decidir el destino post-splash. Ver [SplashScreen].
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    fun hasActiveSession(): Boolean = authRepository.isLoggedIn()
}
