package com.powertrack.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle

/**
 * Slots tipográficos del design system que no tienen equivalente directo en
 * `androidx.compose.material3.Typography` (ver tokens en
 * Views_PowerTrack(1).md). Se exponen vía [PowerTrackTheme.extendedTypography],
 * análogo a `MaterialTheme.typography`.
 */
@Immutable
data class PowerTrackExtendedTypography(
    val numericData: TextStyle,
)

private val LocalPowerTrackExtendedTypography = staticCompositionLocalOf {
    PowerTrackExtendedTypography(numericData = TextStyle.Default)
}

private val PowerTrackDarkColorScheme = darkColorScheme(
    primary = ElectricVolt,
    onPrimary = PureBlack,
    primaryContainer = ElectricVolt,
    onPrimaryContainer = PureBlack,
    secondary = PureWhite,
    onSecondary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = CharcoalContainer,
    onSurface = PureWhite,
    surfaceVariant = CharcoalActive,
    onSurfaceVariant = PureWhite,
    outline = DividerDarkGrey,
    outlineVariant = DividerDarkGrey,
    error = PureRed,
    onError = PureWhite,
)

/**
 * Tema raíz de la app. El design system ("Aggressive Modernism",
 * Views_PowerTrack(1).md) define un único modo oscuro OLED como parte de la
 * identidad de marca -- no hay variante clara especificada en el doc, y el
 * fondo negro puro es intencional para el contexto de uso (gimnasios con
 * iluminación pobre/variable). Por eso NO se ramifica en
 * `isSystemInDarkTheme()`: se fuerza [PowerTrackDarkColorScheme] siempre.
 * Si en el futuro se aprueba una variante clara, es una decisión de
 * producto/diseño explícita a tomar sobre este mismo esqueleto, no un
 * default técnico de esta base.
 */
@Composable
fun PowerTrackTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalPowerTrackExtendedTypography provides PowerTrackExtendedTypography(
            numericData = PowerTrackNumericDataStyle,
        ),
    ) {
        MaterialTheme(
            colorScheme = PowerTrackDarkColorScheme,
            typography = PowerTrackTypography,
            shapes = PowerTrackShapes,
            content = content,
        )
    }
}

/** Punto de acceso análogo a `MaterialTheme` para los tokens extendidos. */
object PowerTrackTheme {
    val extendedTypography: PowerTrackExtendedTypography
        @Composable
        get() = LocalPowerTrackExtendedTypography.current
}
