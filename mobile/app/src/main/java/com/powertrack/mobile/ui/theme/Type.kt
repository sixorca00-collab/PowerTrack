package com.powertrack.mobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.powertrack.mobile.R

/**
 * Estrategia de doble fuente vía Google Fonts *downloadable* (no se
 * bundlean binarios .ttf en el APK): Anton para titulares/datos numéricos,
 * Inter para cuerpo/UI. Ver Views_PowerTrack(1).md, sección "Tipografía".
 *
 * El certificado del proveedor está en res/values/font_certs.xml.
 */
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val antonGoogleFont = GoogleFont(name = "Anton")
private val interGoogleFont = GoogleFont(name = "Inter")

val AntonFontFamily = FontFamily(
    Font(googleFont = antonGoogleFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
)

val InterFontFamily = FontFamily(
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = interGoogleFont, fontProvider = googleFontProvider, weight = FontWeight.ExtraBold),
)

/**
 * Mapeo de los tokens tipográficos del doc de mockups a los slots de
 * Material 3. `numeric-data` (Anton 32sp, para pesos/reps/temporizadores)
 * no tiene slot equivalente en M3 y se expone aparte, ver
 * [PowerTrackExtendedTypography] en Theme.kt.
 */
val PowerTrackTypography = Typography(
    // display-xl
    displayLarge = TextStyle(
        fontFamily = AntonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 72.sp,
        lineHeight = 72.sp,
        letterSpacing = 0.02.em,
    ),
    // headline-lg-mobile (se prioriza la variante mobile; headline-lg de
    // 48sp es la versión desktop del mismo token)
    headlineLarge = TextStyle(
        fontFamily = AntonFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 36.sp,
    ),
    // headline-md
    headlineMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
    ),
    // body-lg
    bodyLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp,
    ),
    // body-md
    bodyMedium = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    // label-bold
    labelLarge = TextStyle(
        fontFamily = InterFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.05.em,
    ),
)

/** numeric-data: usado siempre para conteo de reps, pesos y temporizadores. */
val PowerTrackNumericDataStyle = TextStyle(
    fontFamily = AntonFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 32.sp,
    lineHeight = 32.sp,
)
