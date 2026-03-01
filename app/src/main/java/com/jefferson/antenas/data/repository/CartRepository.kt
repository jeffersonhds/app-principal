package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.flow.StateFlow

interface CartRepository {
    val items: StateFlow<List<CartItem>>
    fun addToCart(product: Product, quantityToAdd: Int = 1)
    fun updateQuantity(productId: String, quantity: Int)
    fun removeItem(productId: String)
    fun clearCart()
    fun getCartTotal(): Double
    fun getCartCount(): Int
}
