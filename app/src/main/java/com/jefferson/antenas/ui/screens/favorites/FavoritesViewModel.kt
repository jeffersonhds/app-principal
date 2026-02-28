package com.jefferson.antenas.ui.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
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
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _isLoading.value = false
            return
        }
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(uid).get().await()
                @Suppress("UNCHECKED_CAST")
                val ids = (doc.get("favorites") as? List<String>)?.toSet() ?: emptySet()
                _favoriteIds.value = ids
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Erro ao carregar favoritos", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(productId: String) {
        val uid = auth.currentUser?.uid ?: return
        val current = _favoriteIds.value.toMutableSet()

        if (productId in current) current.remove(productId)
        else current.add(productId)

        // Atualiza estado local imediatamente para UI responsiva (optimistic update)
        _favoriteIds.value = current

        viewModelScope.launch {
            try {
                firestore.collection("users").document(uid)
                    .update("favorites", current.toList())
                    .await()
            } catch (e: Exception) {
                Log.e("FavoritesViewModel", "Erro ao salvar favorito $productId", e)
                // Reverte o estado local em caso de falha
                _favoriteIds.value = _favoriteIds.value.toMutableSet().also {
                    if (productId in it) it.remove(productId) else it.add(productId)
                }
                _syncError.value = "Falha ao sincronizar favoritos. Verifique sua conexão."
            }
        }
    }
}
