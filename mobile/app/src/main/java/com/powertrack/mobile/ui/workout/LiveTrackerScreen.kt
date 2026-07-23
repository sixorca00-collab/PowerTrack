package com.powertrack.mobile.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powertrack.mobile.data.remote.dto.OverallFeelingDto
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton
import com.powertrack.mobile.ui.common.PowerTrackSecondaryButton
import com.powertrack.mobile.ui.common.SectionLabel
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.ElectricVolt
import kotlin.math.roundToInt

/**
 * Live Tracker (Docs/documentacion_funcional_tecnica_fitness_mvp.md RF-04):
 * un ejercicio a la vez, todo prellenado, pensado para el pulgar. Marcar una
 * serie como hecha es 1 toque ("DONE" sobre los valores precargados); si
 * hace falta ajustar peso/reps/RPE, cada tap de +/- es un ajuste discreto,
 * así que el peor caso ("ajustar" + "DONE") sigue siendo 2 toques.
 *
 * Consume: [LiveTrackerViewModel.phase] ([LiveTrackerPhase]).
 * Emite hacia el ViewModel: adjustWeight/adjustReps/adjustRpe/toggleSetCompleted/
 * goToNext/goToPrevious/cancelFeelingSelection/finishSession(feeling)/load().
 * Emite hacia el NavGraph: `onExit()` (abandonar o sesión ya finalizada,
 * vuelve al shell principal con bottom nav).
 */
@Composable
fun LiveTrackerScreen(onExit: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: LiveTrackerViewModel = hiltViewModel()
    val phase by viewModel.phase.collectAsStateWithLifecycle()
    var showAbandonConfirm by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (val currentPhase = phase) {
                LiveTrackerPhase.Loading -> LoadingContent()
                is LiveTrackerPhase.Error -> ErrorContent(currentPhase.message, onRetry = viewModel::load, onExit = onExit)
                is LiveTrackerPhase.Tracking -> TrackingContent(
                    phase = currentPhase,
                    onClose = { showAbandonConfirm = true },
                    onWeightDelta = viewModel::adjustWeight,
                    onRepsDelta = viewModel::adjustReps,
                    onRpeDelta = viewModel::adjustRpe,
                    onToggleSet = viewModel::toggleSetCompleted,
                    onPrevious = viewModel::goToPrevious,
                    onNext = viewModel::goToNext,
                )
                is LiveTrackerPhase.ChoosingFeeling -> ChoosingFeelingContent(
                    phase = currentPhase,
                    onFeelingSelected = viewModel::finishSession,
                    onBack = viewModel::cancelFeelingSelection,
                )
                is LiveTrackerPhase.Completed -> CompletedContent(phase = currentPhase, onDone = onExit)
            }
        }
    }

    if (showAbandonConfirm) {
        AlertDialog(
            onDismissRequest = { showAbandonConfirm = false },
            title = { Text("¿Salir del entrenamiento?") },
            text = { Text("Se perderán las series registradas en esta sesión.") },
            confirmButton = {
                TextButton(onClick = { showAbandonConfirm = false; onExit() }) {
                    Text("SALIR", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showAbandonConfirm = false }) { Text("SEGUIR") } },
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(bottom = 24.dp))
        PowerTrackPrimaryButton(text = "Reintentar", onClick = onRetry, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(12.dp))
        PowerTrackSecondaryButton(text = "Salir", onClick = onExit, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun TrackingContent(
    phase: LiveTrackerPhase.Tracking,
    onClose: () -> Unit,
    onWeightDelta: (Int, Int, Double) -> Unit,
    onRepsDelta: (Int, Int, Int) -> Unit,
    onRpeDelta: (Int, Int, Int) -> Unit,
    onToggleSet: (Int, Int) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val exercise = phase.exercises[phase.currentIndex]
    val isLastExercise = phase.currentIndex == phase.exercises.lastIndex

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SectionLabel(
                text = "Ejercicio ${phase.currentIndex + 1}/${phase.exercises.size}",
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose) { Icon(Icons.Filled.Close, contentDescription = "Salir") }
        }
        LinearProgressIndicator(
            progress = { (phase.currentIndex + 1f) / phase.exercises.size },
            modifier = Modifier.fillMaxWidth().height(4.dp).padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )

        Column(modifier = Modifier.weight(1f).padding(24.dp)) {
            Text(text = exercise.exerciseName.uppercase(), fontFamily = AntonFontFamily, style = MaterialTheme.typography.headlineLarge)
            Text(
                text = "Objetivo: ${exercise.targetSets} series x ${exercise.targetRepMin}-${exercise.targetRepMax} reps",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = exercise.previousSummary ?: "Sin historial previo para este ejercicio",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.weight(1f)) {
                items(exercise.sets, key = { it.setNumber }) { set ->
                    val setIndex = set.setNumber - 1
                    SetRow(
                        set = set,
                        onWeightDelta = { delta -> onWeightDelta(phase.currentIndex, setIndex, delta) },
                        onRepsDelta = { delta -> onRepsDelta(phase.currentIndex, setIndex, delta) },
                        onRpeDelta = { delta -> onRpeDelta(phase.currentIndex, setIndex, delta) },
                        onToggle = { onToggleSet(phase.currentIndex, setIndex) },
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (phase.currentIndex > 0) {
                PowerTrackSecondaryButton(text = "Anterior", onClick = onPrevious, modifier = Modifier.weight(1f))
            }
            PowerTrackPrimaryButton(
                text = if (isLastExercise) "Finalizar" else "Siguiente",
                onClick = onNext,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SetRow(
    set: SetEntry,
    onWeightDelta: (Double) -> Unit,
    onRepsDelta: (Int) -> Unit,
    onRpeDelta: (Int) -> Unit,
    onToggle: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (set.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "SET ${set.setNumber}",
                fontFamily = AntonFontFamily,
                modifier = Modifier.padding(end = 8.dp),
            )
            NumericStepper(label = "KG", value = formatWeightValue(set.weightKg), onIncrement = { onWeightDelta(2.5) }, onDecrement = { onWeightDelta(-2.5) })
            Spacer(modifier = Modifier.padding(start = 8.dp))
            NumericStepper(label = "REPS", value = set.reps.toString(), onIncrement = { onRepsDelta(1) }, onDecrement = { onRepsDelta(-1) })
            Spacer(modifier = Modifier.padding(start = 8.dp))
            NumericStepper(label = "RPE", value = set.rpe.toString(), onIncrement = { onRpeDelta(1) }, onDecrement = { onRpeDelta(-1) })
            Spacer(modifier = Modifier.weight(1f))
            DoneToggle(completed = set.completed, onToggle = onToggle)
        }
    }
}

@Composable
private fun NumericStepper(label: String, value: String, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        SectionLabel(text = label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Menos $label", modifier = Modifier.size(16.dp))
            }
            Text(text = value, fontFamily = AntonFontFamily, style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onIncrement, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Más $label", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DoneToggle(completed: Boolean, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .background(
                color = if (completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onToggle) {
            Icon(
                Icons.Filled.Check,
                contentDescription = if (completed) "Serie completada" else "Marcar serie como completada",
                tint = if (completed) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ChoosingFeelingContent(
    phase: LiveTrackerPhase.ChoosingFeeling,
    onFeelingSelected: (OverallFeelingDto) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "¿CÓMO TE SENTISTE HOY?", fontFamily = AntonFontFamily, style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OverallFeelingDto.entries.forEach { feeling ->
                FilterChip(
                    selected = false,
                    onClick = { onFeelingSelected(feeling) },
                    label = { Text(feeling.name) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = MaterialTheme.colorScheme.surface),
                )
            }
        }
        phase.errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 16.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onBack) { Text("VOLVER") }
    }
}

@Composable
private fun CompletedContent(phase: LiveTrackerPhase.Completed, onDone: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text(text = "SESIÓN COMPLETADA", fontFamily = AntonFontFamily, style = MaterialTheme.typography.headlineLarge, color = ElectricVolt)
        Spacer(modifier = Modifier.height(16.dp))

        if (phase.suggestions.isEmpty()) {
            Text(
                text = "Esta sesión ya se había sincronizado antes.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(phase.suggestions, key = { it.exerciseName + it.recommendationLabel }) { suggestion ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = suggestion.exerciseName.uppercase(), fontWeight = FontWeight.Bold)
                            Text(
                                text = suggestion.recommendationLabel.uppercase(),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
                            )
                            Text(text = suggestion.message, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }

        PowerTrackPrimaryButton(text = "Volver a Rutinas", onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 16.dp))
    }
}

private fun formatWeightValue(value: Double): String {
    val rounded = (value * 10).roundToInt() / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
