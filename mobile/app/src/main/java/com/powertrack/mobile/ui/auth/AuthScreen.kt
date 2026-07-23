package com.powertrack.mobile.ui.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.powertrack.mobile.R
import com.powertrack.mobile.data.remote.dto.SportGoal
import com.powertrack.mobile.ui.common.PowerTrackPrimaryButton
import com.powertrack.mobile.ui.common.PowerTrackSecondaryButton
import com.powertrack.mobile.ui.common.SectionLabel
import com.powertrack.mobile.ui.common.displayLabel
import com.powertrack.mobile.ui.theme.AntonFontFamily
import com.powertrack.mobile.ui.theme.ElectricVolt

/**
 * Destino "auth" (Login/Register, Docs/vistas-mockups.md). Login y registro
 * viven en la misma pantalla, alternables con un link inferior (fiel al
 * "New Recruit? Sign Up Now" del mockup) en vez de 2 rutas separadas --
 * mismo layout, menos saltos de navegación.
 *
 * Consume: [AuthViewModel.uiState] ([AuthUiState]).
 * Emite hacia el ViewModel: onEmailChange/onPasswordChange/onFullNameChange/
 * onSportGoalChange/togglePasswordVisibility/toggleMode/submit().
 * Emite hacia el NavGraph: `onAuthenticated()` cuando el ViewModel reporta
 * [AuthEvent.Authenticated] (login o registro exitoso).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(onAuthenticated: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: AuthViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.Authenticated -> onAuthenticated()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_powertrack),
            contentDescription = null,
            modifier = Modifier.size(96.dp),
        )
        Text(
            text = "POWERTRACK",
            fontFamily = AntonFontFamily,
            fontSize = 22.sp,
            color = ElectricVolt,
            modifier = Modifier.padding(top = 12.dp),
        )
        Text(
            text = "EVOLUTION IS NON-NEGOTIABLE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 32.dp),
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (uiState.mode == AuthMode.REGISTER) {
                LabeledField(label = "FULL NAME") {
                    OutlinedTextField(
                        value = uiState.fullName,
                        onValueChange = viewModel::onFullNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("Jaxson Vance") },
                        colors = authFieldColors(),
                    )
                }
                LabeledField(label = "SPORT GOAL") {
                    SportGoalDropdown(selected = uiState.sportGoal, onSelected = viewModel::onSportGoalChange)
                }
            }

            LabeledField(label = "ATHLETE EMAIL") {
                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("your@email.com") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    colors = authFieldColors(),
                )
            }

            LabeledField(label = "SECURITY KEY") {
                OutlinedTextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("••••••••") },
                    leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    trailingIcon = {
                        IconButton(onClick = viewModel::togglePasswordVisibility) {
                            Icon(
                                imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                    visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    colors = authFieldColors(),
                )
            }

            // "Forgotten key?" del mockup no tiene endpoint de recuperación de clave en el
            // backend (fuera de alcance del MVP) -- se omite para no prometer un flujo que no existe.

            uiState.errorMessage?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            PowerTrackPrimaryButton(
                text = if (uiState.mode == AuthMode.LOGIN) "Get Started" else "Create Account",
                onClick = viewModel::submit,
                enabled = uiState.isSubmitEnabled,
                isLoading = uiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Icon(Icons.Filled.Bolt, contentDescription = null) },
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(
                text = "SYNC ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        // Apple ID / Google: el mockup los muestra, pero no existe login social en el backend
        // (Docs/api-endpoints.md solo lista email/password). Se dejan visibles pero
        // deshabilitados en vez de omitirlos, para no romper la fidelidad visual del mockup
        // ni simular un flujo que no está implementado.
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PowerTrackSecondaryButton(
                text = "Apple ID",
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f),
            )
            PowerTrackSecondaryButton(
                text = "Google",
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (uiState.mode == AuthMode.LOGIN) "New Recruit? " else "Already training? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = viewModel::toggleMode) {
                Text(
                    text = if (uiState.mode == AuthMode.LOGIN) "SIGN UP NOW" else "SIGN IN",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SectionLabel(text = label)
        content()
    }
}

@Composable
private fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.secondary,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SportGoalDropdown(selected: SportGoal, onSelected: (SportGoal) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected.displayLabel(),
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = authFieldColors(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SportGoal.entries.forEach { goal ->
                DropdownMenuItem(
                    text = { Text(goal.displayLabel()) },
                    onClick = {
                        onSelected(goal)
                        expanded = false
                    },
                )
            }
        }
    }
}
