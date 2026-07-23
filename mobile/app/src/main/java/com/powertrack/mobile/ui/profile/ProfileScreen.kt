package com.powertrack.mobile.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton
import com.powertrack.mobile.ui.common.PowerTrackTag
import com.powertrack.mobile.ui.common.SectionLabel
import com.powertrack.mobile.ui.common.displayLabel
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.CharcoalActive

/**
 * Tab "Profile" (Docs/vistas-mockups.md). El avatar circular del mockup
 * (marcado "-moz-alt-content" = imagen rota de la herramienta que generó el
 * mockup) se reemplaza acá por un ícono placeholder real -- el backend no
 * soporta foto de perfil en el MVP.
 *
 * Datos reales conectados: nombre, email, objetivo deportivo
 * (`GET/PUT /api/v1/users/me*`) y logout.
 *
 * Datos NO reales del mockup (LVL/gamificación, "Elite Athlete", cita
 * personal, Total Workouts/Personal Bests/Active Streak): el backend no
 * expone ninguna métrica de este tipo. En vez de inventar números que
 * parezcan reales, el slot de "badge" se repropone para mostrar el
 * `sportGoal` real del usuario, y las 3 tarjetas de stats muestran un
 * estado vacío honesto ("—" / "Sin datos aún") en lugar de un número
 * fabricado. Las filas "Account/Privacy/Notifications" quedan visibles pero
 * inertes (sin pantalla real detrás en este MVP).
 *
 * Consume: [ProfileViewModel.uiState].
 * Emite hacia el NavGraph: `onLoggedOut()` tras logout.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onLoggedOut: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.LoggedOut -> onLoggedOut()
            }
        }
    }

    var showLogoutConfirm by remember { mutableStateOf(false) }

    Scaffold(modifier = modifier, containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.profile == null -> Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "No se pudo cargar tu perfil.")
            }
            else -> {
                val profile = uiState.profile!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    item(key = "header") {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier.size(96.dp).background(CharcoalActive, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                            }
                            PowerTrackTag(text = profile.sportGoal.displayLabel(), modifier = Modifier.padding(top = 12.dp))
                            Text(
                                text = profile.fullName.uppercase(),
                                fontFamily = AntonFontFamily,
                                style = MaterialTheme.typography.headlineMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            Text(text = profile.email, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            PowerTrackPrimaryButton(
                                text = "Edit Profile",
                                onClick = viewModel::openEditGoal,
                                modifier = Modifier.padding(top = 16.dp),
                            )
                        }
                    }

                    item(key = "stats") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            EmptyStatCard(label = "Total Workouts", modifier = Modifier.weight(1f))
                            EmptyStatCard(label = "Personal Bests", modifier = Modifier.weight(1f))
                            EmptyStatCard(label = "Active Streak", modifier = Modifier.weight(1f))
                        }
                    }

                    item(key = "settings_label") { SectionLabel(text = "Settings") }
                    item(key = "settings_account") { InertSettingsRow(icon = Icons.Filled.Person, label = "Account") }
                    item(key = "settings_privacy") { InertSettingsRow(icon = Icons.Filled.Lock, label = "Privacy") }
                    item(key = "settings_notifications") { InertSettingsRow(icon = Icons.Filled.Notifications, label = "Notifications") }
                    item(key = "logout") {
                        InertSettingsRow(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            label = "Log Out",
                            tint = MaterialTheme.colorScheme.error,
                            onClick = { showLogoutConfirm = true },
                        )
                    }

                    uiState.errorMessage?.let { message ->
                        item(key = "error") { Text(text = message, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
    }

    if (uiState.isEditingGoal) {
        val profile = uiState.profile
        var selectedGoal by remember(profile) { mutableStateOf(profile?.sportGoal ?: SportGoal.FUERZA) }
        AlertDialog(
            onDismissRequest = viewModel::closeEditGoal,
            title = { Text("Objetivo deportivo") },
            text = {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedGoal.displayLabel(),
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SportGoal.entries.forEach { goal ->
                            DropdownMenuItem(
                                text = { Text(goal.displayLabel()) },
                                onClick = { selectedGoal = goal; expanded = false },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.updateGoal(selectedGoal) }, enabled = !uiState.isSavingGoal) {
                    Text("GUARDAR")
                }
            },
            dismissButton = { TextButton(onClick = viewModel::closeEditGoal) { Text("CANCELAR") } },
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("¿Cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = { showLogoutConfirm = false; viewModel.logout() }) {
                    Text("CERRAR SESIÓN", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showLogoutConfirm = false }) { Text("CANCELAR") } },
        )
    }
}

/** Placeholder honesto (no fabrica un número): el backend no expone estas métricas en el MVP. */
@Composable
private fun EmptyStatCard(label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            SectionLabel(text = label)
            Text(text = "—", fontFamily = AntonFontFamily, style = MaterialTheme.typography.headlineMedium)
            Text(text = "Sin datos aún", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Fila de "Settings". Cuando `onClick` es `null` la fila es puramente visual
 * (sin destino real detrás -- Account/Privacy/Notifications no existen como
 * pantallas en este MVP); solo "Log Out" pasa un `onClick` real.
 */
@Composable
private fun InertSettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: (() -> Unit)? = null,
) {
    val rowContent: @Composable () -> Unit = {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint)
            Text(text = label.uppercase(), modifier = Modifier.weight(1f).padding(start = 16.dp), color = tint)
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
    }
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) { rowContent() }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) { rowContent() }
    }
}
