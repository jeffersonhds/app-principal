package com.jefferson.antenas.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchProducts()
        updateCartCount()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = repository.getProducts()

            result.onSuccess { products ->
                _products.value = products
                _isLoading.value = false
            }

            result.onFailure { error ->
                Log.e("HomeViewModel", "Falha ao carregar produtos: ${error.message}")
                _products.value = emptyList()
                _isLoading.value = false
                _errorMessage.value = error.message ?: "Erro desconhecido. Tente novamente."
            }
        }
    }

    fun retry() {
        fetchProducts()
    }

    fun addToCart(product: Product) {
        cartRepository.addToCart(product)
    }

    private fun updateCartCount() {
        cartRepository.items.onEach { items ->
            _cartItemCount.value = items.sumOf { it.quantity }
        }.launchIn(viewModelScope)
    }
}