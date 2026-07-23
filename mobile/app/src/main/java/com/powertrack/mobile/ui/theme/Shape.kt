package com.powertrack.mobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Lenguaje de forma "agudo y disciplinado" (Views_PowerTrack(1).md, sección
 * "Shapes"): redondeo mínimo, nunca formas de píldora ni círculos salvo
 * avatares (esos se resuelven puntualmente con CircleShape en el
 * composable de avatar, no forman parte de esta escala global).
 *
 * Tokens: sm=0.125rem(2px), DEFAULT/md=0.25-0.375rem(~4-6px, colapsados a
 * 4px para elementos estándar como botones/inputs), lg/xl=0.5rem(8px) para
 * tarjetas y contenedores grandes.
 */
val PowerTrackShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(4.dp),
    large = RoundedCornerShape(8.dp),
    extraLarge = RoundedCornerShape(8.dp),
)
