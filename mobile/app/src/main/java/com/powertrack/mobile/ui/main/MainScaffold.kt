package com.powertrack.mobile.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.powertrack.mobile.ui.navigation.Screen
import com.powertrack.mobile.ui.performance.PerformanceScreen
import com.powertrack.mobile.ui.profile.ProfileScreen
import com.powertrack.mobile.ui.routines.RoutinesScreen

/**
 * Destino "main" del NavHost raíz: el shell con bottom navigation bar que
 * contiene las 3 tabs post-login (Routines / Performance / Profile,
 * Docs/vistas-mockups.md). Aloja su propio `NavHost` anidado para cambiar
 * de tab sin perder el estado de cada una (patrón estándar de Compose
 * Navigation), mientras que los flujos de una sola tarea (crear rutina,
 * detalle de rutina, Live Tracker, logout) navegan usando [rootNavController]
 * para salir de este shell (sin bottom bar en esas pantallas).
 */
@Composable
fun MainScaffold(rootNavController: NavHostController, modifier: Modifier = Modifier) {
    val tabNavController = rememberNavController()

    Scaffold(
        modifier = modifier,
        bottomBar = { PowerTrackBottomBar(tabNavController) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        NavHost(
            navController = tabNavController,
            startDestination = Screen.Routines.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Screen.Routines.route) {
                RoutinesScreen(
                    onCreateRoutine = { rootNavController.navigate(Screen.RoutineCreate.route) },
                    onOpenRoutine = { routineId ->
                        rootNavController.navigate(Screen.RoutineDetail.createRoute(routineId))
                    },
                    onLiveTrackerReady = { routineId, dayId, sessionId ->
                        rootNavController.navigate(Screen.LiveTracker.createRoute(routineId, dayId, sessionId))
                    },
                )
            }
            composable(Screen.Performance.route) {
                PerformanceScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onLoggedOut = {
                        rootNavController.navigate(Screen.Auth.route) {
                            popUpTo(0)
                        }
                    },
                )
            }
        }
    }
}

private data class BottomNavTab(val screen: Screen, val label: String, val icon: ImageVector)

private val bottomNavTabs = listOf(
    BottomNavTab(Screen.Routines, "Routines", Icons.Filled.FitnessCenter),
    BottomNavTab(Screen.Performance, "Performance", Icons.AutoMirrored.Filled.TrendingUp),
    BottomNavTab(Screen.Profile, "Profile", Icons.Filled.Person),
)

@Composable
private fun PowerTrackBottomBar(tabNavController: NavHostController) {
    val backStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomNavTabs.forEach { tab ->
            val selected = currentDestination?.hierarchy?.any { it.route == tab.screen.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    tabNavController.navigate(tab.screen.route) {
                        popUpTo(tabNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label.uppercase()) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
