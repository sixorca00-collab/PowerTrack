package com.powertrack.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.powertrack.mobile.ui.auth.AuthScreen
import com.powertrack.mobile.ui.main.MainScaffold
import com.powertrack.mobile.ui.routines.create.RoutineCreateScreen
import com.powertrack.mobile.ui.routines.detail.RoutineDetailScreen
import com.powertrack.mobile.ui.splash.SplashScreen
import com.powertrack.mobile.ui.workout.LiveTrackerScreen

/**
 * NavHost raíz de PowerTrack.
 *
 * `Splash`/`Auth` no llevan bottom bar (pre-login). `Main` es el shell con
 * bottom nav que aloja Routines/Performance/Profile en un `NavHost` anidado
 * (ver [MainScaffold]). `RoutineCreate`/`RoutineDetail`/`LiveTracker`
 * también viven acá (no en el shell) a propósito: son flujos de una sola
 * tarea, sin distracciones de navegación mientras se crea una rutina o hay
 * una sesión de entrenamiento activa.
 */
@Composable
fun PowerTrackNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSessionResolved = { loggedIn ->
                    val destination = if (loggedIn) Screen.Main.route else Screen.Auth.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Auth.route) {
            AuthScreen(
                onAuthenticated = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Main.route) {
            MainScaffold(rootNavController = navController)
        }

        composable(Screen.RoutineCreate.route) {
            RoutineCreateScreen(
                onBack = { navController.popBackStack() },
                onCreated = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.RoutineDetail.route,
            arguments = listOf(navArgument(Screen.RoutineDetail.ARG_ROUTINE_ID) { type = NavType.StringType }),
        ) {
            RoutineDetailScreen(
                onBack = { navController.popBackStack() },
                onStartLiveTracker = { routineId, routineDayId, sessionId ->
                    navController.navigate(Screen.LiveTracker.createRoute(routineId, routineDayId, sessionId))
                },
                onRoutineDeleted = { navController.popBackStack() },
            )
        }

        composable(
            route = Screen.LiveTracker.route,
            arguments = liveTrackerArguments,
        ) {
            LiveTrackerScreen(
                onExit = { navController.popBackStack(Screen.Main.route, inclusive = false) },
            )
        }
    }
}

private val liveTrackerArguments: List<NamedNavArgument> = listOf(
    navArgument(Screen.LiveTracker.ARG_ROUTINE_ID) { type = NavType.StringType },
    navArgument(Screen.LiveTracker.ARG_ROUTINE_DAY_ID) { type = NavType.StringType },
    navArgument(Screen.LiveTracker.ARG_SESSION_ID) { type = NavType.StringType },
)
