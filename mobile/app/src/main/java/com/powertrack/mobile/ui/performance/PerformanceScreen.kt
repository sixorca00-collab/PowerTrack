package com.powertrack.mobile.ui.performance

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.powertrack.mobile.ui.common.PowerTrackTag
import com.powertrack.mobile.ui.common.SectionLabel
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.CharcoalActive
import com.powertrack.mobile.ui.theme.ElectricVolt

/**
 * Tab "Performance" (Docs/vistas-mockups.md).
 *
 * *** PANTALLA INTENCIONALMENTE ESTÁTICA ***. El backend del MVP no expone
 * ningún endpoint de analítica (Docs/api-endpoints.md, sección "Pendientes":
 * `GET /analytics/exercise/{id}` y `GET /analytics/muscle-group` no están
 * implementados). "Power Output (kW)", "1RM estimado", "Coach Suggestions"
 * de texto libre, el estado Improving/Stagnated/Regressing y la tabla de
 * "Lifting Performance" tampoco tienen ninguna fuente de datos real hoy.
 *
 * Esta pantalla replica la estructura visual del mockup con datos de
 * ejemplo fijos (constantes `SAMPLE_*` más abajo), sin ViewModel, sin
 * `StateFlow` y sin ninguna llamada de red -- no hay nada que conectar
 * todavía. Cuando el backend agregue analítica real, esta pantalla debe
 * reconstruirse consumiendo un `PerformanceViewModel` real, siguiendo el
 * mismo patrón que `RoutinesScreen`/`ProfileScreen`.
 */
@Composable
fun PerformanceScreen(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item(key = "header") {
            Column {
                SectionLabel(text = "Monthly Summary", color = MaterialTheme.colorScheme.primary)
                Text(text = "PERFORMANCE STATUS", style = MaterialTheme.typography.headlineLarge)
            }
        }

        item(key = "status_chips") {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = true,
                    onClick = {},
                    label = { Text("IMPROVING") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary),
                )
                FilterChip(selected = false, onClick = {}, label = { Text("STAGNATED") })
                FilterChip(selected = false, onClick = {}, label = { Text("REGRESSING") })
            }
        }

        item(key = "power_output") {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel(text = "Power Output (kW)", modifier = Modifier.weight(1f))
                        PowerTrackTag(text = "Last 30 Days")
                    }
                    StaticSparkline(
                        points = SAMPLE_POWER_OUTPUT,
                        modifier = Modifier.fillMaxWidth().height(120.dp).padding(top = 16.dp),
                    )
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("WK 01", "WK 02", "WK 03", "WK 04").forEach {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item(key = "total_volume") {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel(text = "Total Volume")
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(text = "42.8", fontFamily = AntonFontFamily, style = MaterialTheme.typography.displayLarge, color = MaterialTheme.colorScheme.primary)
                        Text(text = " TONS", modifier = Modifier.padding(bottom = 12.dp))
                    }
                    Text(text = "+12% vs last month", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item(key = "training_frequency") {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel(text = "Training Frequency")
                    Row(
                        modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        SAMPLE_TRAINING_FREQUENCY.forEach { active ->
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .height(if (active) 80.dp else 32.dp)
                                    .background(if (active) MaterialTheme.colorScheme.primary else CharcoalActive),
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                            Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item(key = "coach_suggestions_label") { SectionLabel(text = "Coach Suggestions") }

        items(SAMPLE_COACH_SUGGESTIONS) { suggestion ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        PowerTrackTag(text = suggestion.tag, modifier = Modifier.weight(1f, fill = false))
                    }
                    Text(
                        text = suggestion.title.uppercase(),
                        fontFamily = AntonFontFamily,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(text = suggestion.body, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }

        item(key = "lifting_performance_label") { SectionLabel(text = "Lifting Performance By Category") }

        item(key = "lifting_performance_table") {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SAMPLE_LIFTS.forEachIndexed { index, lift ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = lift.name, fontWeight = FontWeight.Bold)
                                Text(text = "${lift.oneRepMax} KG", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text(text = lift.monthlyDelta, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                            PowerTrackTag(text = lift.status)
                        }
                        if (index != SAMPLE_LIFTS.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun StaticSparkline(points: List<Float>, modifier: Modifier = Modifier) {
    val lineColor = ElectricVolt
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas
        val stepX = size.width / (points.size - 1)
        val maxValue = points.max()
        val minValue = points.min()
        val range = (maxValue - minValue).takeIf { it != 0f } ?: 1f
        val path = points.mapIndexed { index, value ->
            val x = index * stepX
            val normalized = (value - minValue) / range
            val y = size.height - (normalized * size.height)
            Offset(x, y)
        }
        for (i in 0 until path.size - 1) {
            drawLine(color = lineColor, start = path[i], end = path[i + 1], strokeWidth = 4f)
        }
    }
}

private data class CoachSuggestionSample(val tag: String, val title: String, val body: String)
private data class LiftSample(val name: String, val oneRepMax: String, val monthlyDelta: String, val status: String)

// --- Datos de ejemplo, puramente ilustrativos (ver comentario de cabecera). ---

private val SAMPLE_POWER_OUTPUT = listOf(2.1f, 1.8f, 2.4f, 2.6f, 2.3f, 2.9f, 3.1f, 3.4f)
private val SAMPLE_TRAINING_FREQUENCY = listOf(false, false, true, false, true, false, false)

private val SAMPLE_COACH_SUGGESTIONS = listOf(
    CoachSuggestionSample(
        tag = "Recovery",
        title = "Extend Rest Periods",
        body = "Ejemplo ilustrativo: los datos de esta pantalla no provienen del backend todavía.",
    ),
    CoachSuggestionSample(
        tag = "Load Adaptation",
        title = "Increase Leg Press Load",
        body = "Ejemplo ilustrativo: los datos de esta pantalla no provienen del backend todavía.",
    ),
)

private val SAMPLE_LIFTS = listOf(
    LiftSample(name = "Squat (Low Bar)", oneRepMax = "185.0", monthlyDelta = "+2.5%", status = "Optimal"),
    LiftSample(name = "Bench Press", oneRepMax = "125.0", monthlyDelta = "0.0%", status = "Stagnant"),
    LiftSample(name = "Deadlift", oneRepMax = "220.0", monthlyDelta = "+4.1%", status = "Optimal"),
)
