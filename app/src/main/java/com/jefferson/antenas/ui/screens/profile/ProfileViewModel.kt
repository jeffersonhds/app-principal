package com.jefferson.antenas.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jefferson.antenas.data.model.User
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.ProductRepository
import com.jefferson.antenas.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _uiState.update { it.copy(isLoading = false, error = "Nenhum usuário logado.") }
            return
        }

        // Mostra imediatamente com dados já em memória (sem rede)
        val quickUser = User(
            uid = firebaseUser.uid,
            name = firebaseUser.displayName ?: "",
            email = firebaseUser.email ?: ""
        )
        _uiState.update { it.copy(isLoading = false, user = quickUser) }

        // Busca pontos e dados completos no Firestore em background
        viewModelScope.launch {
            try {
                val fullUser = userRepository.getUser(firebaseUser.uid)
                if (fullUser != null) {
                    _uiState.update { it.copy(user = fullUser) }
                }
            } catch (_: Exception) {
                // Mantém quickUser — pontos aparecem quando a rede responder
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            cartRepository.clearCart()
            productRepository.clearCache()
            auth.signOut()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
