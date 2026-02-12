package com.jefferson.antenas.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductUiState {
    data object Loading : ProductUiState
    data class Success(val product: Product) : ProductUiState
    data class Error(val message: String) : ProductUiState
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { ProductUiState.Loading }

            val result = repository.getProductById(productId)

            result.onSuccess { product ->
                if (product != null) {
                    _uiState.update { ProductUiState.Success(product) }
                } else {
                    _uiState.update { ProductUiState.Error("Produto não encontrado") }
                }
            }.onFailure { error ->
                _uiState.update { ProductUiState.Error(error.localizedMessage ?: "Erro de conexão") }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        cartRepository.addToCart(product, quantity)
    }
}