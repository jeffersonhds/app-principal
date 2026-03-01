package com.jefferson.antenas.ui.screens.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val email: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        val uid = userRepository.currentUserId
        if (uid == null) {
            _uiState.update { it.copy(isLoading = false, error = "Usuário não encontrado.") }
            return
        }
        viewModelScope.launch {
            try {
                val user = userRepository.getUser(uid)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        name = user?.name ?: "",
                        email = userRepository.currentUserEmail ?: ""
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, email = userRepository.currentUserEmail ?: "")
                }
            }
        }
    }

    fun onNameChange(value: String) = _uiState.update { it.copy(name = value, error = null) }

    fun save() {
        val name = _uiState.value.name.trim()
        if (name.length < 3) {
            _uiState.update { it.copy(error = "Nome deve ter pelo menos 3 caracteres.") }
            return
        }
        val uid = userRepository.currentUserId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                userRepository.updateUserName(uid, name)
                _uiState.update { it.copy(isSaving = false, isSaved = true) }
                Log.d("EditProfileViewModel", "Perfil atualizado para: $name")
            } catch (e: Exception) {
                Log.e("EditProfileViewModel", "Erro ao salvar perfil", e)
                _uiState.update { it.copy(isSaving = false, error = "Erro ao salvar. Tente novamente.") }
            }
        }
    }

    fun clearSaved() = _uiState.update { it.copy(isSaved = false) }
}
