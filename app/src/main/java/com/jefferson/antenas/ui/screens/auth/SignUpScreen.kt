package com.jefferson.antenas.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.utils.ValidationUtils

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // ✅ CORRIGIDO: rememberSaveable salva o estado mesmo quando rotaciona
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Observa o estado de sucesso do cadastro para navegar
    LaunchedEffect(authState.isLoginSuccessful) {
        if (authState.isLoginSuccessful) {
            onSignUpSuccess()
        }
    }

    // Observa o estado de erro para mostrar um Toast
    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Crie Sua Conta",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = TextPrimary
        )
        Text(
            "Comece a juntar pontos hoje mesmo",
            style = MaterialTheme.typography.bodyLarge,
            color = SignalOrange
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = name.isNotEmpty() && !ValidationUtils.isValidName(name),
            supportingText = {
                ValidationUtils.getNameError(name)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = email.isNotEmpty() && !ValidationUtils.isValidEmail(email),
            supportingText = {
                ValidationUtils.getEmailError(email)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Campo de Senha com Ícone de Olho
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Esconder senha" else "Mostrar senha",
                        tint = SignalOrange
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = password.isNotEmpty() && !ValidationUtils.isValidPassword(password),
            supportingText = {
                ValidationUtils.getPasswordError(password)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Campo de Confirmar Senha com Ícone de Olho
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirme a Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Esconder senha" else "Mostrar senha",
                        tint = SignalOrange
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showConfirmPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = password != confirmPassword && confirmPassword.isNotEmpty(),
            enabled = !authState.isLoading,
            supportingText = {
                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text("As senhas não coincidem", color = ErrorRed, fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ Botão de Cadastrar
        Button(
            onClick = { viewModel.signUp(name, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
            enabled = !authState.isLoading &&
                    ValidationUtils.isValidName(name) &&
                    ValidationUtils.isValidEmail(email) &&
                    ValidationUtils.isValidPassword(password) &&
                    password == confirmPassword
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
                    Text("Criando Conta...", fontWeight = FontWeight.Bold, color = MidnightBlueStart)
                }
            } else {
                Text("Criar Conta", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link para Login
        val annotatedString = buildAnnotatedString {
            append("Já tem uma conta? ")
            withStyle(style = SpanStyle(color = SignalOrange, fontWeight = FontWeight.Bold)) {
                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                append("Faça Login")
                pop()
            }
        }

        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                if (!authState.isLoading) {
                    annotatedString.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let { onLoginClick() }
                }
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
    }
}