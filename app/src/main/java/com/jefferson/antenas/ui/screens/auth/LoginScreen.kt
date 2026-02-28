package com.jefferson.antenas.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.CustomToast
import com.jefferson.antenas.ui.componets.ToastType
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SignalOrangeDark
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.utils.ErrorMessageHandler
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showErrorToast by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showSuccessToast by rememberSaveable { mutableStateOf(false) }
    var successMessage by rememberSaveable { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    val emailValid = email.contains("@") && email.contains(".")
    val passwordValid = password.length >= 6
    val canLogin = !authState.isLoading && email.isNotBlank() && password.isNotBlank()

    LaunchedEffect(authState.isLoginSuccessful) {
        if (authState.isLoginSuccessful) onLoginSuccess()
    }

    LaunchedEffect(authState.error) {
        authState.error?.let {
            errorMessage = ErrorMessageHandler.tratarErro(Exception(it))
            showErrorToast = true
            viewModel.clearError()
            delay(3000)
            showErrorToast = false
        }
    }

    LaunchedEffect(authState.passwordResetSent) {
        if (authState.passwordResetSent) {
            successMessage = "Email de redefinição enviado! Verifique sua caixa de entrada."
            showSuccessToast = true
            viewModel.clearPasswordResetSent()
            delay(3000)
            showSuccessToast = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Background ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(MidnightBlueStart, Color(0xFF0C1A2E), MidnightBlueStart)
                    )
                )
        )

        // Decorative glow — top right
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 80.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SignalOrange.copy(alpha = 0.13f), Color.Transparent)
                    )
                )
        )

        // Decorative glow — bottom left
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-50).dp, y = 50.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SatelliteBlue.copy(alpha = 0.10f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero Zone ─────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Antenna icon with glow
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(SignalOrange.copy(alpha = 0.22f), Color.Transparent)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📡", fontSize = 48.sp)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    "JEFFERSON ANTENAS",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = 1.5.sp
                )

                Spacer(Modifier.height(6.dp))

                Text(
                    "Sua central de receptores e satélites",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(20.dp))

                // Trust chips
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TrustChip(emoji = "📦", label = "Entrega BR")
                    TrustChip(emoji = "⭐", label = "10k+ clientes")
                    TrustChip(emoji = "✅", label = "Oficial")
                }
            }

            // ── Form Card ─────────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MidnightBlueCard,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                shadowElevation = 24.dp,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp)
                        .padding(top = 28.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Form header
                    Text(
                        "Bem-vindo de volta 👋",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Entre na sua conta para continuar",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email, null,
                                tint = if (emailValid) SuccessGreen else TextTertiary
                            )
                        },
                        trailingIcon = if (emailValid) ({
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }) else null,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SignalOrange,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = SignalOrange,
                            unfocusedLabelColor = TextSecondary,
                            focusedLeadingIconColor = if (emailValid) SuccessGreen else SignalOrange,
                            unfocusedLeadingIconColor = TextTertiary,
                            cursorColor = SignalOrange,
                            focusedContainerColor = CardGradientStart,
                            unfocusedContainerColor = CardGradientStart
                        )
                    )

                    Spacer(Modifier.height(14.dp))

                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Senha") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock, null,
                                tint = if (passwordValid) SuccessGreen else TextTertiary
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    if (showPassword) Icons.Default.Visibility
                                    else Icons.Default.VisibilityOff,
                                    contentDescription = if (showPassword) "Esconder senha"
                                    else "Mostrar senha",
                                    tint = SignalOrange
                                )
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = SignalOrange,
                            unfocusedBorderColor = CardBorder,
                            focusedLabelColor = SignalOrange,
                            unfocusedLabelColor = TextSecondary,
                            focusedLeadingIconColor = if (passwordValid) SuccessGreen else SignalOrange,
                            unfocusedLeadingIconColor = TextTertiary,
                            cursorColor = SignalOrange,
                            focusedContainerColor = CardGradientStart,
                            unfocusedContainerColor = CardGradientStart
                        )
                    )

                    // Forgot password
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = { viewModel.sendPasswordReset(email) },
                            enabled = !authState.isLoading && email.isNotBlank() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        ) {
                            Text("Esqueci minha senha?", color = SignalOrange, fontSize = 13.sp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Gradient login button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (canLogin)
                                    Brush.horizontalGradient(
                                        listOf(SignalOrange, SignalOrangeDark)
                                    )
                                else
                                    Brush.horizontalGradient(
                                        listOf(
                                            SignalOrange.copy(alpha = 0.35f),
                                            SignalOrangeDark.copy(alpha = 0.35f)
                                        )
                                    )
                            )
                            .clickable(enabled = canLogin) { viewModel.login(email, password) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState.isLoading) {
                            Row(
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    color = MidnightBlueStart,
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    "Entrando...",
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightBlueStart,
                                    fontSize = 15.sp
                                )
                            }
                        } else {
                            Text(
                                "Entrar",
                                fontWeight = FontWeight.ExtraBold,
                                color = if (canLogin) MidnightBlueStart else MidnightBlueStart.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    // Security indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock, null,
                            tint = TextTertiary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Conexão segura e criptografada",
                            color = TextTertiary,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Signup link
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Não tem uma conta?", color = TextSecondary, fontSize = 14.sp)
                        TextButton(onClick = { if (!authState.isLoading) onSignUpClick() }) {
                            Text(
                                "Cadastre-se grátis",
                                color = SignalOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // ── Toasts ────────────────────────────────────────────────────
        CustomToast(
            visible = showErrorToast,
            message = errorMessage,
            type = ToastType.ERROR,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        CustomToast(
            visible = showSuccessToast,
            message = successMessage,
            type = ToastType.SUCCESS,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ── Trust Chip ────────────────────────────────────────────────────────────────

@Composable
private fun TrustChip(emoji: String, label: String) {
    Surface(
        color = CardBorder.copy(alpha = 0.25f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, CardBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
