package com.jefferson.antenas.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jefferson.antenas.data.model.User
import com.jefferson.antenas.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false,
    val passwordResetSent: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(error = "Email e senha não podem estar em branco.") }
            return
        }
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch { loginWithRetry(email, password, tentativa = 1) }
    }

    private suspend fun loginWithRetry(email: String, password: String, tentativa: Int = 1) {
        try {
            Log.d("AuthViewModel", "Login tentativa $tentativa/3")
            val success = withTimeoutOrNull(30_000L) {
                auth.signInWithEmailAndPassword(email, password).await()
                true
            }
            if (success == true) {
                _authState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                retryOrFail(tentativa, "Servidor indisponível. Tente em alguns minutos.") {
                    loginWithRetry(email, password, it)
                }
            }
        } catch (e: TimeoutCancellationException) {
            retryOrFail(tentativa, "Sem conexão com servidor. Verifique sua internet.") {
                loginWithRetry(email, password, it)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Erro no login: ${e.message}", e)
            _authState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao fazer login.") }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(error = "Todos os campos são obrigatórios.") }
            return
        }
        _authState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch { signUpWithRetry(name, email, password, tentativa = 1) }
    }

    private suspend fun signUpWithRetry(name: String, email: String, password: String, tentativa: Int = 1) {
        try {
            Log.d("AuthViewModel", "SignUp tentativa $tentativa/3")
            val authResult = withTimeoutOrNull(30_000L) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }
            if (authResult == null) {
                retryOrFail(tentativa, "Servidor indisponível. Tente em alguns minutos.") {
                    signUpWithRetry(name, email, password, it)
                }
                return
            }
            val firebaseUser = authResult.user ?: run {
                _authState.update { it.copy(isLoading = false, error = "Erro ao criar usuário.") }
                return
            }
            val newUser = User(uid = firebaseUser.uid, name = name, email = email, points = 0)
            val savedSuccessfully = withTimeoutOrNull(20_000L) {
                userRepository.createUser(newUser)
                true
            }
            if (savedSuccessfully == true) {
                _authState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                Log.e("AuthViewModel", "Timeout ao salvar perfil")
                _authState.update { it.copy(isLoading = false, error = "Erro ao salvar perfil. Tente fazer login.") }
            }
        } catch (e: TimeoutCancellationException) {
            retryOrFail(tentativa, "Servidor indisponível. Tente em alguns minutos.") {
                signUpWithRetry(name, email, password, it)
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Erro no signUp: ${e.message}", e)
            _authState.update { it.copy(isLoading = false, error = e.message ?: "Erro ao cadastrar.") }
        }
    }

    fun sendPasswordReset(email: String) {
        if (email.isBlank()) {
            _authState.update { it.copy(error = "Digite seu email para redefinir a senha.") }
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.update { it.copy(error = "Digite um email válido para redefinir a senha.") }
            return
        }
        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(email).await()
                _authState.update { it.copy(passwordResetSent = true) }
            } catch (e: Exception) {
                _authState.update { it.copy(error = e.message ?: "Erro ao enviar email.") }
            }
        }
    }

    fun clearError() = _authState.update { it.copy(error = null) }
    fun clearPasswordResetSent() = _authState.update { it.copy(passwordResetSent = false) }

    private suspend fun retryOrFail(
        tentativa: Int,
        finalErrorMsg: String,
        retry: suspend (Int) -> Unit
    ) {
        if (tentativa < 3) {
            Log.w("AuthViewModel", "Timeout tentativa $tentativa/3, aguardando retry")
            _authState.update { it.copy(error = "Reconectando... (tentativa $tentativa/3)") }
            delay(2000)
            retry(tentativa + 1)
        } else {
            Log.e("AuthViewModel", "Falha após 3 tentativas")
            _authState.update { it.copy(isLoading = false, error = finalErrorMsg) }
        }
    }
}
