package com.jefferson.antenas.ui.screens.orders

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.jefferson.antenas.data.model.Order
import com.jefferson.antenas.data.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val uid = auth.currentUser?.uid ?: run {
            _isLoading.value = false
            return
        }
        _isLoading.value = true
        _error.value = null
        _hasMore.value = true
        viewModelScope.launch {
            try {
                val result = orderRepository.getOrders(userId = uid, limit = 20)
                _orders.value = result
                _hasMore.value = result.size >= 20
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Erro ao carregar pedidos", e)
                _error.value = "Erro ao carregar pedidos. Tente novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoadingMore.value || !_hasMore.value) return
        val uid = auth.currentUser?.uid ?: return
        val lastEpoch = _orders.value.lastOrNull()?.createdAtEpoch ?: return
        _isLoadingMore.value = true
        viewModelScope.launch {
            try {
                val more = orderRepository.loadMoreOrders(userId = uid, afterEpoch = lastEpoch, limit = 20)
                _orders.value = _orders.value + more
                _hasMore.value = more.size >= 20
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Erro ao carregar mais pedidos", e)
            } finally {
                _isLoadingMore.value = false
            }
        }
    }
}
