package com.powertrack.mobile.ui.workout

import com.powertrack.mobile.data.remote.dto.RecommendationDto

/** Etiqueta corta para mostrar en la tarjeta de sugerencia al finalizar una sesión. */
fun RecommendationDto.displayLabel(): String = when (this) {
    RecommendationDto.INCREASE_WEIGHT -> "Aumentar peso"
    RecommendationDto.INCREASE_REPS -> "Aumentar repeticiones"
    RecommendationDto.MAINTAIN -> "Mantener"
    RecommendationDto.DECREASE_WEIGHT -> "Reducir peso"
    RecommendationDto.DELOAD -> "Deload"
}
