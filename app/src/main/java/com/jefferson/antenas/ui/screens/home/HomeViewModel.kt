package com.jefferson.antenas.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.remote.JeffersonApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: JeffersonApi,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    init {
        Log.d("HomeViewModel", "📦 HomeViewModel inicializado")
        val startTime = System.currentTimeMillis()

        fetchProducts()
        updateCartCount()

        Log.d("HomeViewModel", "✅ Init concluído em ${System.currentTimeMillis() - startTime}ms")
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                Log.d("HomeViewModel", "🌐 Iniciando fetch de produtos...")

                val result = api.getProducts()

                val fetchTime = System.currentTimeMillis() - startTime
                Log.d("HomeViewModel", "✅ Produtos carregados em ${fetchTime}ms - Total: ${result.size} itens")

                _products.value = result
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ ERRO ao buscar produtos: ${e.message}", e)
                _products.value = emptyList()
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "🛒 Adicionando ao carrinho: ${product.name}")
            cartRepository.addToCart(product)
        }
    }

    private fun updateCartCount() {
        Log.d("HomeViewModel", "👁️ Observando carrinho...")
        cartRepository.items.onEach { items ->
            _cartItemCount.value = items.sumOf { it.quantity }
            Log.d("HomeViewModel", "🔄 Carrinho atualizado - Count: ${_cartItemCount.value}")
        }.launchIn(viewModelScope)
    }
}