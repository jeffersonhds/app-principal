package com.jefferson.antenas.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueEnd
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary

@Composable
fun EditProfileScreen(
    onBackClick: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            snackbarHostState.showSnackbar("Perfil atualizado com sucesso!")
            viewModel.clearSaved()
            onBackClick()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MidnightBlueStart, MidnightBlueEnd)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // ── TopBar ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
                }
                Text(
                    "Editar Perfil",
                    color = TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = SignalOrange)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(24.dp))

                    // ── Avatar placeholder ───────────────────────────────
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(SatelliteBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            color = SatelliteBlue,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 32.sp   // tamanho especial do avatar — não existe equivalente no tema
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Campo Nome ───────────────────────────────────────
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = {
                            Text("Nome completo", style = MaterialTheme.typography.bodySmall)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = SatelliteBlue, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = uiState.error != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MidnightBlueStart,
                            unfocusedContainerColor = MidnightBlueStart,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SatelliteBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = SatelliteBlue,
                            unfocusedLabelColor = TextTertiary,
                            cursorColor = SatelliteBlue,
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (uiState.error != null) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            uiState.error!!,
                            color = ErrorRed,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // ── Campo Email (somente leitura) ────────────────────
                    OutlinedTextField(
                        value = uiState.email,
                        onValueChange = {},
                        label = {
                            Text("E-mail", style = MaterialTheme.typography.bodySmall)
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledContainerColor = MidnightBlueStart,
                            disabledTextColor = TextTertiary,
                            disabledBorderColor = CardBorder,
                            disabledLabelColor = TextTertiary,
                            disabledLeadingIconColor = TextTertiary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "O e-mail não pode ser alterado aqui.",
                        color = TextTertiary,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(Modifier.height(28.dp))

                    // ── Botão Salvar ─────────────────────────────────────
                    Button(
                        onClick = viewModel::save,
                        enabled = !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SatelliteBlue)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Text(
                                "  Salvar Alterações",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = SuccessGreen,
                contentColor = Color.White
            )
        }
    }
}
