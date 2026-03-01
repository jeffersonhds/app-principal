package com.jefferson.antenas.data.model

data class Order(
    val id: String,
    val statusStr: String,
    val items: List<OrderItem>,
    val total: Double,
    val date: String,
    val createdAtEpoch: Long = 0L,
    val estimatedDelivery: String? = null,
    val trackingCode: String? = null,
    val deliveredDate: String? = null
) {
    // Número de exibição formatado — não armazenado no Firestore
    val number: String get() = "JA-${id.takeLast(6).uppercase()}"
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double
)
