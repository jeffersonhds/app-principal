package com.jefferson.antenas.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    // Calcula o total deste item (Preço x Quantidade)
    val total: Double
        get() = product.getDiscountedPrice() * quantity
}