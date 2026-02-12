package com.jefferson.antenas.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.CustomToast
import com.jefferson.antenas.ui.componets.ToastType
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.utils.ErrorMessageHandler
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // ✅ CORRIGIDO: rememberSaveable salva o estado mesmo quando rotaciona
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showErrorToast by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // Observa o estado de sucesso do login para navegar
    LaunchedEffect(authState.isLoginSuccessful) {
        if (authState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Observa o estado de erro para mostrar um Toast customizado
    LaunchedEffect(authState.error) {
        authState.error?.let {
            errorMessage = ErrorMessageHandler.tratarErro(Exception(it))
            showErrorToast = true
            viewModel.clearError()

            delay(3000)
            showErrorToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ TÍTULOS HARMONIZADOS
            Text(
                "Bem-vindo de Volta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = TextPrimary  // Branco puro
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Acesse sua conta para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = SignalOrange  // Laranja harmonioso
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Campo de Senha com Ícone de Olho
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                // ✅ NOVO: Ícone de olho para mostrar/esconder senha
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Esconder senha" else "Mostrar senha",
                            tint = SignalOrange
                        )
                    }
                },
                visualTransformation = if (showPassword) {
                    VisualTransformation.None  // Mostra a senha em texto plano
                } else {
                    PasswordVisualTransformation()  // Esconde com pontos
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ Botão de Entrar
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = MidnightBlueStart,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Entrando...", fontWeight = FontWeight.Bold, color = MidnightBlueStart)
                    }
                } else {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ LINK PARA CADASTRO - CORES HARMONIZADAS
            val annotatedString = buildAnnotatedString {
                append("Não tem uma conta? ")  // TextSecondary (cinza suave)
                withStyle(style = SpanStyle(color = SignalOrange, fontWeight = FontWeight.Bold)) {
                    pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
                    append("Cadastre-se")  // Laranja
                    pop()
                }
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    if (!authState.isLoading) {
                        annotatedString.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                            .firstOrNull()?.let { onSignUpClick() }
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        // ✅ Toast customizado profissional
        CustomToast(
            visible = showErrorToast,
            message = errorMessage,
            type = ToastType.ERROR,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}