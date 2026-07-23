package com.powertrack.mobile.ui.navigation

/**
 * Destinos de navegación de PowerTrack.
 *
 * `Splash`/`Auth` viven en el `NavHost` raíz ([PowerTrackNavGraph]), sin
 * bottom bar. `Routines`/`Performance`/`Profile` son las 3 tabs dentro de
 * `Main` (ver `ui/main/MainScaffold.kt`), que a su vez es un destino más del
 * `NavHost` raíz. `RoutineCreate`/`RoutineDetail`/`LiveTracker` también
 * viven en el `NavHost` raíz (a propósito, sin bottom bar: son flujos
 * enfocados de una sola tarea, para minimizar distracciones durante la
 * creación de una rutina o una sesión activa).
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Auth : Screen("auth")

    /** Contenedor con bottom bar; ver [Main] como destino del NavHost raíz. */
    data object Main : Screen("main")

    // Tabs internas de Main (usadas por el NavHost anidado en MainScaffold).
    data object Routines : Screen("routines")
    data object Performance : Screen("performance")
    data object Profile : Screen("profile")

    data object RoutineCreate : Screen("routine_create")

    data object RoutineDetail : Screen("routine_detail/{routineId}") {
        const val ARG_ROUTINE_ID = "routineId"
        fun createRoute(routineId: String) = "routine_detail/$routineId"
    }

    data object LiveTracker : Screen("live_tracker/{routineId}/{routineDayId}/{sessionId}") {
        const val ARG_ROUTINE_ID = "routineId"
        const val ARG_ROUTINE_DAY_ID = "routineDayId"
        const val ARG_SESSION_ID = "sessionId"
        fun createRoute(routineId: String, routineDayId: String, sessionId: String) =
            "live_tracker/$routineId/$routineDayId/$sessionId"
    }
}
