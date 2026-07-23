package com.powertrack.mobile.data.remote.dto

import kotlinx.serialization.Serializable

/**
 * Calcado 1:1 de `com.powertrack.backend.domain.user.SportGoal` (backend).
 * Jackson serializa el enum por su nombre tal cual, así que los nombres de
 * las constantes deben ser idénticos en ambos lados.
 */
@Serializable
enum class SportGoal {
    FUERZA,
    HIPERTROFIA,
    ESTETICA,
    ATLETISMO,
    MOVILIDAD,
    FLEXIBILIDAD,
    SALUD,
    POWERLIFTING,
    CALISTENIA,
    RUNNING,
}
