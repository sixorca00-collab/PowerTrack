package com.powertrack.mobile.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.powertrack.mobile.R
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.ElectricVolt
import com.powertrack.mobile.ui.theme.InterFontFamily
import kotlinx.coroutines.delay

/**
 * Pantalla "Logo" (Docs/vistas-mockups.md). Muestra la marca ~900ms y decide
 * el siguiente destino según haya o no sesión guardada -- sin bottom bar,
 * fuera del shell principal.
 *
 * Consume: [SplashViewModel.hasActiveSession] (lectura local, sin red).
 * Emite: `onSessionResolved(loggedIn)` hacia el NavGraph, que decide entre
 * `Screen.Main` y `Screen.Auth`.
 *
 * Nota de asset: el isotipo (`logo_powertrack.png`) es el PNG de
 * Docs/img/mockups/logo.png usado tal cual como placeholder de marca hasta
 * que exista un asset vectorial de producción entregado por diseño.
 */
@Composable
fun SplashScreen(onSessionResolved: (loggedIn: Boolean) -> Unit, modifier: Modifier = Modifier) {
    val viewModel: SplashViewModel = hiltViewModel()

    LaunchedEffect(Unit) {
        delay(SPLASH_DELAY_MS)
        onSessionResolved(viewModel.hasActiveSession())
    }

    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_powertrack),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
        )
        Text(
            text = "POWERTRACK",
            fontFamily = AntonFontFamily,
            fontSize = 32.sp,
            color = ElectricVolt,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "EVOLUTION IS NON-NEGOTIABLE",
            fontFamily = InterFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private const val SPLASH_DELAY_MS = 900L
