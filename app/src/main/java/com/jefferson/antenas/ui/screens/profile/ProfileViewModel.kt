package com.jefferson.antenas.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.User
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
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

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val document = firestore.collection("users").document(firebaseUser.uid).get().await()
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    _uiState.update { it.copy(isLoading = false, user = user) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Perfil de usuário não encontrado.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: ${e.message}") }
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
