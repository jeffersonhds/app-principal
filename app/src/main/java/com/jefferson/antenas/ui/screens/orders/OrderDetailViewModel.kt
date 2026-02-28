package com.jefferson.antenas.ui.screens.orders

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

data class OrderDetailUiState(
    val isLoading: Boolean = true,
    val order: Order? = null,
    val error: String? = null
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    private val firestore: FirebaseFirestore,
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
                val doc = firestore.collection("orders").document(orderId).get().await()
                if (!doc.exists()) {
                    _uiState.update { it.copy(isLoading = false, error = "Pedido não encontrado.") }
                    return@launch
                }

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                val statusStr = doc.getString("status") ?: "paid"
                val status = when (statusStr) {
                    "processing" -> OrderStatus.PROCESSING
                    "shipped" -> OrderStatus.SHIPPED
                    "delivered" -> OrderStatus.DELIVERED
                    "cancelled" -> OrderStatus.CANCELLED
                    else -> OrderStatus.CONFIRMED
                }

                val total = doc.getDouble("total") ?: 0.0
                val timestamp = doc.getTimestamp("createdAt")
                val dateStr = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "—"

                @Suppress("UNCHECKED_CAST")
                val rawItems = (doc.get("items") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
                val orderItems = rawItems.map { item ->
                    OrderItem(
                        name = item["productName"] as? String ?: "Produto",
                        quantity = (item["quantity"] as? Long)?.toInt() ?: 1,
                        unitPrice = item["unitPrice"] as? Double ?: 0.0
                    )
                }

                val order = Order(
                    id = doc.id,
                    number = "JA-${doc.id.takeLast(6).uppercase()}",
                    status = status,
                    items = orderItems,
                    total = total,
                    date = dateStr,
                    createdAtEpoch = timestamp?.seconds ?: 0L,
                    trackingCode = doc.getString("trackingCode"),
                    estimatedDelivery = doc.getString("estimatedDelivery"),
                    deliveredDate = doc.getString("deliveredDate")
                )

                _uiState.update { it.copy(isLoading = false, order = order) }
            } catch (e: Exception) {
                Log.e("OrderDetailViewModel", "Erro ao carregar pedido $orderId", e)
                _uiState.update { it.copy(isLoading = false, error = "Erro ao carregar pedido. Tente novamente.") }
            }
        }
    }
}
