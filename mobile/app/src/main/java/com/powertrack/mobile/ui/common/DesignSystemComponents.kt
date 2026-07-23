package com.powertrack.mobile.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.PureBlack

/**
 * Componentes compartidos por las 5 pantallas reales, siguiendo
 * Docs/vistas-mockups.md ("Componentes"): botón primario sólido en Volt con
 * label Anton en negro, botón secundario delineado en blanco. Touch target
 * mínimo 48dp en ambos.
 */
@Composable
fun PowerTrackPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier.padding(vertical = 0.dp),
        shape = MaterialTheme.shapes.small,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = PureBlack,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = PureBlack.copy(alpha = 0.6f),
        ),
        contentPadding = PaddingValues(vertical = 16.dp, horizontal = 24.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(end = 8.dp), color = PureBlack, strokeWidth = 2.dp)
        }
        Text(text = text.uppercase(), fontFamily = AntonFontFamily, fontSize = 16.sp)
        if (trailingIcon != null) {
            Box(modifier = Modifier.padding(start = 12.dp)) { trailingIcon() }
        }
    }
}

@Composable
fun PowerTrackSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
        contentPadding = PaddingValues(vertical = 14.dp, horizontal = 24.dp),
    ) {
        Text(text = text.uppercase(), fontFamily = AntonFontFamily, fontSize = 14.sp)
    }
}

/** `label-bold` (Docs/vistas-mockups.md): mayúsculas, tracking amplio, uso en encabezados de sección/campo. */
@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier, color: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = color,
        modifier = modifier,
    )
}

/** Chip/Tag rectangular (radio 0), fondo negro y texto blanco en mayúsculas — ej. "HIIT", "STRENGTH". */
@Composable
fun PowerTrackTag(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(PureBlack, RoundedCornerShape(0.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
        )
    }
}
