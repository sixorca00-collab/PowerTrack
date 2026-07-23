package com.powertrack.mobile.ui.routines.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.powertrack.mobile.data.remote.dto.RoutineDayDto
import com.powertrack.mobile.data.remote.dto.RoutineExerciseDto
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton

/**
 * Pantalla "Detalle de rutina" (Módulo 1, Docs/propuesta-modulos-rutinas-y-registro.md:
 * "muestra los días y ejercicios ... es la pantalla que enlaza con Comenzar
 * entrenamiento"). No forma parte de los 5 mockups visuales.
 *
 * Consume: [RoutineDetailViewModel.uiState] (`GET /api/v1/routines/{id}`, real).
 * Emite: onBack(), onStartLiveTracker(routineId, dayId, sessionId) (tras
 * `POST /api/v1/workouts/session/start`, real), onRoutineDeleted().
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineDetailScreen(
    onBack: () -> Unit,
    onStartLiveTracker: (routineId: String, routineDayId: String, sessionId: String) -> Unit,
    onRoutineDeleted: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: RoutineDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RoutineDetailEvent.OpenLiveTracker ->
                    onStartLiveTracker(event.routineId, event.routineDayId, event.sessionId)
                RoutineDetailEvent.RoutineDeleted -> onRoutineDeleted()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(uiState.routine?.name ?: "Rutina") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar rutina")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            uiState.routine == null -> Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                Text(uiState.errorMessage ?: "No se pudo cargar la rutina.")
            }
            else -> {
                val routine = uiState.routine!!
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentPadding = PaddingValues(24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    routine.description?.takeIf { it.isNotBlank() }?.let { description ->
                        item(key = "description") {
                            Text(text = description, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(routine.days.sortedBy { it.orderIndex }, key = { it.id }) { day ->
                        DayCard(
                            day = day,
                            isStarting = uiState.startingDayId == day.id,
                            onStartWorkout = { viewModel.startWorkout(day.id) },
                        )
                    }
                    uiState.errorMessage?.let { message ->
                        item(key = "error") {
                            Text(text = message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar rutina?") },
            text = { Text("Se eliminarán también sus días y ejercicios. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteRoutine()
                }) { Text("ELIMINAR", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("CANCELAR") }
            },
        )
    }
}

@Composable
private fun DayCard(day: RoutineDayDto, isStarting: Boolean, onStartWorkout: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = day.dayName.uppercase(), style = MaterialTheme.typography.headlineMedium)

            day.exercises.sortedBy { it.orderIndex }.forEachIndexed { index, exercise ->
                ExerciseRow(exercise)
                if (index != day.exercises.lastIndex) HorizontalDivider()
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

@Composable
private fun ExerciseRow(exercise: RoutineExerciseDto) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = exercise.exerciseName, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "${exercise.targetSets} series · ${exercise.targetRepMin}-${exercise.targetRepMax} reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
