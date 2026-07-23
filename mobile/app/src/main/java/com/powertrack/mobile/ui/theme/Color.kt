package com.powertrack.mobile.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Paleta cruda del design system "Aggressive Modernism" (ver
 * Views_PowerTrack(1).md, sección prosa "Brand & Style" / "Colores" — se
 * prioriza sobre el bloque YAML de tokens, que es un export automático con
 * algunos valores inconsistentes entre sí, ej. surface != #000000).
 */

// Tier 1 (Base): negro puro, máxima eficiencia OLED.
val PureBlack = Color(0xFF000000)

// Tier 2 (Cards/Contenedores): Charcoal.
val CharcoalContainer = Color(0xFF1A1A1A)

// Tier 3 (Activo/Pop-overs) y pistas de progreso sin llenar.
val CharcoalActive = Color(0xFF262626)

// Primario "Electric Volt": exclusivo para acciones críticas y estados
// activos. Usar con moderación (efecto "alarma").
val ElectricVolt = Color(0xFFE2FF00)

val PureWhite = Color(0xFFFFFFFF)

// Estado "Descanso"/"Detener".
val PureRed = Color(0xFFFF0000)

// Separadores de listas de alta densidad (1px Dark Grey).
val DividerDarkGrey = Color(0xFF262626)
