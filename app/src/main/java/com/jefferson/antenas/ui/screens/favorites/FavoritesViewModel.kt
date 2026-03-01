package com.jefferson.antenas.ui.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteIds: StateFlow<Set<String>> = _favoriteIds.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _syncError = MutableStateFlow<String?>(null)
    val syncError: StateFlow<String?> = _syncError.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        val uid = userRepository.currentUserId ?: run {
            _isLoading.value = false
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                _favoriteIds.value = userRepository.getFavorites(uid)
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Erro ao carregar favoritos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val uid = userRepository.currentUserId ?: return
        val current = _favoriteIds.value.toMutableSet()

        if (productId in current) current.remove(productId) else current.add(productId)

        // Optimistic update — UI responde imediatamente
        _favoriteIds.value = current

        viewModelScope.launch {
            try {
                userRepository.updateFavorites(uid, current.toList())
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Erro ao salvar favorito $productId", e)
                // Reverte estado em caso de falha
                _favoriteIds.value = _favoriteIds.value.toMutableSet().also {
                    if (productId in it) it.remove(productId) else it.add(productId)
                }
                _syncError.value = "Falha ao sincronizar favoritos. Verifique sua conexão."
            }
        }
    }
}
