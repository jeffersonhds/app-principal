package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.Order

interface OrderRepository {
    /** Salva um novo pedido e retorna o ID gerado pelo Firestore */
    suspend fun saveOrder(orderData: Map<String, Any>): String

    /** Busca os pedidos mais recentes do usuário (paginado) */
    suspend fun getOrders(userId: String, limit: Long = 20): List<Order>

    /** Busca mais pedidos após o último já carregado (cursor de paginação) */
    suspend fun loadMoreOrders(userId: String, afterEpoch: Long, limit: Long = 20): List<Order>

    /** Busca um pedido específico pelo ID */
    suspend fun getOrderById(orderId: String): Order
}
