package com.jefferson.antenas.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jefferson.antenas.data.model.Order
import com.jefferson.antenas.data.model.OrderItem
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OrderRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("pt", "BR"))

    override suspend fun saveOrder(orderData: Map<String, Any>): String {
        val docRef = firestore.collection("orders").add(orderData).await()
        Log.d("OrderRepository", "Pedido salvo: ${docRef.id}")
        return docRef.id
    }

    override suspend fun getOrders(userId: String, limit: Long): List<Order> {
        val snapshot = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try { doc.toOrder(dateFormat) } catch (e: Exception) {
                Log.e("OrderRepository", "Erro ao mapear pedido ${doc.id}", e)
                null
            }
        }
    }

    override suspend fun loadMoreOrders(userId: String, afterEpoch: Long, limit: Long): List<Order> {
        val snapshot = firestore.collection("orders")
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .whereLessThan("createdAt", com.google.firebase.Timestamp(afterEpoch, 0))
            .limit(limit)
            .get()
            .await()
        return snapshot.documents.mapNotNull { doc ->
            try { doc.toOrder(dateFormat) } catch (e: Exception) {
                Log.e("OrderRepository", "Erro ao mapear pedido ${doc.id}", e)
                null
            }
        }
    }

    override suspend fun getOrderById(orderId: String): Order {
        val doc = firestore.collection("orders").document(orderId).get().await()
        if (!doc.exists()) throw Exception("Pedido não encontrado.")
        return doc.toOrder(dateFormat) ?: throw Exception("Erro ao processar pedido.")
    }
}

// ── Extensão privada de mapeamento ────────────────────────────────────────────

private fun com.google.firebase.firestore.DocumentSnapshot.toOrder(
    dateFormat: SimpleDateFormat
): Order? {
    val timestamp = getTimestamp("createdAt")
    val dateStr = timestamp?.toDate()?.let { dateFormat.format(it) } ?: "—"

    @Suppress("UNCHECKED_CAST")
    val rawItems = (get("items") as? List<*>)?.filterIsInstance<Map<String, Any>>() ?: emptyList()
    val items = rawItems.map { item ->
        OrderItem(
            name = item["productName"] as? String ?: "Produto",
            quantity = (item["quantity"] as? Long)?.toInt() ?: 1,
            unitPrice = item["unitPrice"] as? Double ?: 0.0
        )
    }

    return Order(
        id = id,
        statusStr = getString("status") ?: "paid",
        items = items,
        total = getDouble("total") ?: 0.0,
        date = dateStr,
        createdAtEpoch = timestamp?.seconds ?: 0L,
        trackingCode = getString("trackingCode"),
        estimatedDelivery = getString("estimatedDelivery"),
        deliveredDate = getString("deliveredDate")
    )
}
