package com.jefferson.antenas.ui.screens.orders

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
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            try {
                val snapshot = firestore.collection("orders")
                    .whereEqualTo("userId", uid)
                    .get()
                    .await()

                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))
                val orderList = snapshot.documents.mapNotNull { doc ->
                    try {
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
                                unitPrice = (item["unitPrice"] as? Double) ?: 0.0
                            )
                        }

                        val shortId = doc.id.takeLast(6).uppercase()
                        Order(
                            id = doc.id,
                            number = "JA-$shortId",
                            status = status,
                            items = orderItems,
                            total = total,
                            date = dateStr,
                            createdAtEpoch = timestamp?.seconds ?: 0L,
                            trackingCode = doc.getString("trackingCode"),
                            estimatedDelivery = doc.getString("estimatedDelivery"),
                            deliveredDate = doc.getString("deliveredDate")
                        )
                    } catch (e: Exception) {
                        Log.e("OrdersViewModel", "Erro ao mapear pedido ${doc.id}", e)
                        null
                    }
                }.sortedByDescending { it.createdAtEpoch }

                _orders.value = orderList
            } catch (e: Exception) {
                Log.e("OrdersViewModel", "Erro ao carregar pedidos", e)
                _error.value = "Erro ao carregar pedidos. Tente novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
