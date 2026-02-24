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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    // ✅ CORRIGIDO: injeta o Repository em vez da API direta
    // O Repository tem cache inteligente — se a API falhar, usa o banco local
    private val repository: ProductRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    // ✅ Estado de carregamento — para mostrar shimmer enquanto busca
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ✅ Estado de erro — para mostrar mensagem se falhar tudo
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        Log.d("HomeViewModel", "📦 HomeViewModel inicializado")
        fetchProducts()
        updateCartCount()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val startTime = System.currentTimeMillis()
            Log.d("HomeViewModel", "🌐 Buscando produtos via Repository...")

            // ✅ CORRIGIDO: usa o repository que tem:
            // 1. Tentativa na API
            // 2. Se falhar → fallback automático pro cache Room
            // 3. Só falha de verdade se não tiver nem internet nem cache
            val result = repository.getProducts()

            val fetchTime = System.currentTimeMillis() - startTime

            result.onSuccess { products ->
                Log.d("HomeViewModel", "✅ ${products.size} produtos carregados em ${fetchTime}ms")
                _products.value = products
                _isLoading.value = false
            }

            result.onFailure { error ->
                Log.e("HomeViewModel", "❌ Falha ao carregar produtos: ${error.message}")
                _products.value = emptyList()
                _isLoading.value = false
                _errorMessage.value = error.message ?: "Erro desconhecido. Tente novamente."
            }
        }
    }

    // ✅ Permite a tela chamar um novo fetch manualmente (ex: botão "Tentar novamente")
    fun retry() {
        Log.d("HomeViewModel", "🔄 Retry solicitado pelo usuário")
        fetchProducts()
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