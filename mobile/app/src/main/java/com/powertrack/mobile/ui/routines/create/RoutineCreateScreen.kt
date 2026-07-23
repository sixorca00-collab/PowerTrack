package com.powertrack.mobile.ui.routines.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.powertrack.mobile.data.remote.dto.ExerciseDto
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton
import com.powertrack.mobile.ui.common.SectionLabel

/**
 * Pantalla "Crear rutina" (Módulo 1, Docs/propuesta-modulos-rutinas-y-registro.md).
 * No forma parte de los 5 mockups visuales (esos no cubren este flujo), así
 * que su layout sigue el sistema de diseño general en vez de un mockup
 * pixel-perfect: nombre -> agregar días -> agregar ejercicios por día
 * (elegidos del catálogo real, `GET /api/v1/exercises`) -> Crear.
 *
 * Consume: [RoutineCreateViewModel.uiState].
 * Emite: onBack() (cancelar), onCreated() (rutina creada con éxito, real:
 * `POST /api/v1/routines`).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineCreateScreen(onBack: () -> Unit, onCreated: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: RoutineCreateViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                RoutineCreateEvent.RoutineCreated -> onCreated()
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("New Routine") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                SectionLabel(text = "Nombre de la rutina")
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Heavy Duty") },
                )

                SectionLabel(text = "Descripción (opcional)")
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = viewModel::updateDescription,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Fuerza / Hipertrofia") },
                )

                uiState.days.forEach { day ->
                    DayEditor(
                        day = day,
                        canRemove = uiState.days.size > 1,
                        onDayNameChange = { name -> viewModel.updateDayName(day.localId, name) },
                        onRemoveDay = { viewModel.removeDay(day.localId) },
                        onAddExercise = { viewModel.openExercisePicker(day.localId) },
                        onRemoveExercise = { exerciseId -> viewModel.removeExerciseFromDay(day.localId, exerciseId) },
                        onSetsDelta = { exerciseId, delta -> viewModel.updateTargetSets(day.localId, exerciseId, delta) },
                        onRepMinDelta = { exerciseId, delta -> viewModel.updateTargetRepMin(day.localId, exerciseId, delta) },
                        onRepMaxDelta = { exerciseId, delta -> viewModel.updateTargetRepMax(day.localId, exerciseId, delta) },
                    )
                }

                TextButton(onClick = viewModel::addDay) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" AGREGAR DÍA")
                }

                uiState.errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            }

            PowerTrackPrimaryButton(
                text = "Crear rutina",
                onClick = viewModel::save,
                enabled = uiState.canSave,
                isLoading = uiState.isSaving,
                modifier = Modifier.fillMaxWidth().padding(24.dp),
            )
        }
    }

    uiState.exercisePickerForDayId?.let { dayId ->
        ExercisePickerSheet(
            catalog = uiState.catalog,
            isLoading = uiState.isLoadingCatalog,
            onDismiss = viewModel::closeExercisePicker,
            onExerciseSelected = { exercise -> viewModel.addExerciseToDay(dayId, exercise) },
        )
    }
}

@Composable
private fun DayEditor(
    day: RoutineCreateDayUi,
    canRemove: Boolean,
    onDayNameChange: (String) -> Unit,
    onRemoveDay: () -> Unit,
    onAddExercise: () -> Unit,
    onRemoveExercise: (String) -> Unit,
    onSetsDelta: (String, Int) -> Unit,
    onRepMinDelta: (String, Int) -> Unit,
    onRepMaxDelta: (String, Int) -> Unit,
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = day.dayName,
                    onValueChange = onDayNameChange,
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text("Día") },
                )
                if (canRemove) {
                    IconButton(onClick = onRemoveDay) {
                        Icon(Icons.Filled.Close, contentDescription = "Quitar día")
                    }
                }
            }

            day.exercises.forEach { exercise ->
                ExerciseRow(
                    exercise = exercise,
                    onRemove = { onRemoveExercise(exercise.localId) },
                    onSetsDelta = { delta -> onSetsDelta(exercise.localId, delta) },
                    onRepMinDelta = { delta -> onRepMinDelta(exercise.localId, delta) },
                    onRepMaxDelta = { delta -> onRepMaxDelta(exercise.localId, delta) },
                )
            }

            TextButton(onClick = onAddExercise) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text(" AGREGAR EJERCICIO")
            }
        }
    }
}

@Composable
private fun ExerciseRow(
    exercise: RoutineCreateExerciseUi,
    onRemove: () -> Unit,
    onSetsDelta: (Int) -> Unit,
    onRepMinDelta: (Int) -> Unit,
    onRepMaxDelta: (Int) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = exercise.exerciseName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            IconButton(onClick = onRemove) { Icon(Icons.Filled.Close, contentDescription = "Quitar ejercicio") }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Stepper(label = "Series", value = exercise.targetSets.toString(), onIncrement = { onSetsDelta(1) }, onDecrement = { onSetsDelta(-1) })
            Stepper(label = "Rep min", value = exercise.targetRepMin.toString(), onIncrement = { onRepMinDelta(1) }, onDecrement = { onRepMinDelta(-1) })
            Stepper(label = "Rep max", value = exercise.targetRepMax.toString(), onIncrement = { onRepMaxDelta(1) }, onDecrement = { onRepMaxDelta(-1) })
        }
    }
}

@Composable
private fun Stepper(label: String, value: String, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Column {
        SectionLabel(text = label)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onDecrement) { Icon(Icons.Filled.Remove, contentDescription = "Menos $label") }
            Text(text = value, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(horizontal = 4.dp))
            IconButton(onClick = onIncrement) { Icon(Icons.Filled.Add, contentDescription = "Más $label") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExercisePickerSheet(
    catalog: List<ExerciseDto>,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onExerciseSelected: (ExerciseDto) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = remember(catalog, query) {
        if (query.isBlank()) catalog else catalog.filter { it.name.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(text = "ELEGIR EJERCICIO", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Buscar...") },
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (isLoading) {
                Text("Cargando catálogo...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                LazyColumn(modifier = Modifier.height(320.dp)) {
                    items(filtered, key = { it.id }) { exercise ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = exercise.name, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "${exercise.targetMuscle} · ${exercise.category}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            TextButton(onClick = { onExerciseSelected(exercise) }) { Text("AGREGAR") }
                        }
                    }
                }
            }
        }
    }
}
