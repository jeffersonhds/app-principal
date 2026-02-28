package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    fun addToCart(product: Product, quantityToAdd: Int = 1) {
        if (quantityToAdd <= 0) return
        val currentList = _items.value.toMutableList()
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            // Se já existe, soma a nova quantidade
            updateQuantity(product.id, existingItem.quantity + quantityToAdd)
        } else {
            // Se não, adiciona com a quantidade escolhida
            currentList.add(CartItem(product, quantityToAdd))
            _items.value = currentList
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }

        val currentList = _items.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }

        if (index != -1) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _items.value = currentList
        }
    }

    fun removeItem(productId: String) {
        val currentList = _items.value.toMutableList()
        currentList.removeIf { it.product.id == productId }
        _items.value = currentList
    }

    fun getCartTotal(): Double {
        return _items.value.sumOf { it.total }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getCartCount(): Int {
        return _items.value.sumOf { it.quantity }
    }
}