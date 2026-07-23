package com.powertrack.mobile.ui.routines

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powertrack.mobile.data.remote.dto.RoutineSummaryDto
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton
import com.powertrack.mobile.ui.common.SectionLabel
import com.powertrack.mobile.ui.common.formatShortDate
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.CharcoalActive
import com.powertrack.mobile.ui.theme.ElectricVolt

/**
 * Tab "Routines" (Docs/vistas-mockups.md) -- también cumple el rol de
 * pantalla-hub de RF-02 (decisión binaria "Empezar Rutina"/"Ver Desempeño":
 * esta tab + la tab "Performance" del mismo bottom nav).
 *
 * 100% conectada a datos reales: `GET /api/v1/routines`. Se omiten a
 * propósito los bloques puramente decorativos del mockup que no tienen
 * respaldo en el backend (racha "12 DAYS", gráfico "Weekly Intensity",
 * tarjeta "Elite Status") -- incluirlos como estático en la pantalla de
 * acción principal del producto se sintió más riesgoso que útil (ver
 * respuesta final del agente para el detalle de esta decisión).
 *
 * Consume: [RoutinesViewModel.uiState] ([RoutinesUiState]).
 * Emite hacia el ViewModel: load()/onRoutineTapped()/onStartWorkoutTapped()/onDeleteRoutine().
 * Emite hacia el NavGraph (vía MainScaffold): onCreateRoutine(), onOpenRoutine(routineId),
 * onLiveTrackerReady(routineId, dayId, sessionId).
 */
@Composable
fun RoutinesScreen(
    onCreateRoutine: () -> Unit,
    onOpenRoutine: (String) -> Unit,
    onLiveTrackerReady: (routineId: String, routineDayId: String, sessionId: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoutinesViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RoutinesEvent.OpenRoutineDetail -> onOpenRoutine(event.routineId)
                is RoutinesEvent.OpenLiveTracker -> onLiveTrackerReady(event.routineId, event.routineDayId, event.sessionId)
            }
        }
    }

    var pendingDeleteId by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoutine, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Filled.Add, contentDescription = "Crear rutina", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item(key = "header") {
                Column {
                    SectionLabel(text = "Training Protocol", color = MaterialTheme.colorScheme.primary)
                    Text(text = "EXECUTE ROUTINE", style = MaterialTheme.typography.headlineLarge)
                }
            }

            item(key = "section_label") {
                SectionLabel(text = "Custom Routines")
            }

            when {
                uiState.isLoading -> item(key = "loading") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                uiState.routines.isEmpty() -> item(key = "empty") {
                    EmptyRoutinesState(onCreateRoutine = onCreateRoutine)
                }
                else -> items(uiState.routines, key = { it.id }) { routine ->
                    RoutineCard(
                        routine = routine,
                        isStarting = uiState.startingRoutineId == routine.id,
                        onCardTapped = { viewModel.onRoutineTapped(routine.id) },
                        onStartWorkout = { viewModel.onStartWorkoutTapped(routine.id) },
                        onDelete = { pendingDeleteId = routine.id },
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item(key = "error") {
                    Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }

    pendingDeleteId?.let { routineId ->
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null },
            title = { Text("¿Eliminar rutina?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteRoutine(routineId)
                    pendingDeleteId = null
                }) { Text("ELIMINAR", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null }) { Text("CANCELAR") }
            },
        )
    }
}

@Composable
private fun EmptyRoutinesState(onCreateRoutine: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.FitnessCenter,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Todavía no tenés rutinas.",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        )
        PowerTrackPrimaryButton(text = "Crear rutina", onClick = onCreateRoutine)
    }
}

@Composable
private fun RoutineCard(
    routine: RoutineSummaryDto,
    isStarting: Boolean,
    onCardTapped: () -> Unit,
    onStartWorkout: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onCardTapped,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column {
            Box(
                modifier = Modifier.fillMaxWidth().height(96.dp).background(CharcoalActive),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.outline,
                )
                IconButton(onClick = onDelete, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar rutina", tint = Color.White)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = routine.name.uppercase(), fontFamily = AntonFontFamily, style = MaterialTheme.typography.headlineMedium)
                routine.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }

                Row(modifier = Modifier.padding(top = 12.dp, bottom = 16.dp)) {
                    RoutineStat(label = "Días", value = routine.dayCount.toString(), modifier = Modifier.weight(1f))
                    RoutineStat(label = "Creada", value = formatShortDate(routine.createdAt), modifier = Modifier.weight(1f))
                }

                PowerTrackPrimaryButton(
                    text = "Start Workout",
                    onClick = onStartWorkout,
                    isLoading = isStarting,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Icon(Icons.Filled.PlayArrow, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun RoutineStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionLabel(text = label)
        Text(
            text = value,
            fontFamily = AntonFontFamily,
            color = ElectricVolt,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Normal,
        )
    }
}
