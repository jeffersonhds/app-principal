package com.jefferson.antenas.ui.screens.orders

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Order
import com.jefferson.antenas.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val orderId: String = checkNotNull(savedStateHandle["orderId"])

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        loadOrder()
    }

    private fun loadOrder() {
        viewModelScope.launch {
            try {
                val order = orderRepository.getOrderById(orderId)
                _uiState.update { it.copy(isLoading = false, order = order) }
            } catch (e: Exception) {
                Log.e("OrderDetailViewModel", "Erro ao carregar pedido $orderId", e)
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Erro ao carregar pedido.")
                }
            }
        }
    }
}
