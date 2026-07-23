package com.powertrack.mobile.ui.common

import com.powertrack.mobile.data.remote.dto.SportGoal

/**
 * Etiqueta legible (con acentos) para cada [SportGoal]. El enum viaja al
 * backend sin acentos (debe calzar 1:1 con
 * `com.powertrack.backend.domain.user.SportGoal`), pero la UI puede mostrar
 * el label correcto en español.
 */
fun SportGoal.displayLabel(): String = when (this) {
    SportGoal.FUERZA -> "Fuerza"
    SportGoal.HIPERTROFIA -> "Hipertrofia"
    SportGoal.ESTETICA -> "Estética"
    SportGoal.ATLETISMO -> "Atletismo"
    SportGoal.MOVILIDAD -> "Movilidad"
    SportGoal.FLEXIBILIDAD -> "Flexibilidad"
    SportGoal.SALUD -> "Salud"
    SportGoal.POWERLIFTING -> "Powerlifting"
    SportGoal.CALISTENIA -> "Calistenia"
    SportGoal.RUNNING -> "Running"
}
