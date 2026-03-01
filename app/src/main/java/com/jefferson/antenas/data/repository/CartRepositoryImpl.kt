package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.local.CartItemEntity
import com.jefferson.antenas.data.local.dao.CartDao
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao
) : CartRepository {

    // Escopo próprio do singleton — dura enquanto o app estiver vivo
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    override val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    init {
        // Restaura carrinho salvo ao abrir o app
        scope.launch {
            val saved = cartDao.getAll()
            if (saved.isNotEmpty()) {
                _items.value = saved.map { it.toCartItem() }
            }
        }
    }

    override fun addToCart(product: Product, quantityToAdd: Int) {
        if (quantityToAdd <= 0) return
        val currentList = _items.value.toMutableList()
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            updateQuantity(product.id, existingItem.quantity + quantityToAdd)
        } else {
            val newItem = CartItem(product, quantityToAdd)
            currentList.add(newItem)
            _items.value = currentList
            scope.launch { cartDao.upsert(newItem.toEntity()) }
        }
    }

    override fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }
        val currentList = _items.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val updated = currentList[index].copy(quantity = quantity)
            currentList[index] = updated
            _items.value = currentList
            scope.launch { cartDao.upsert(updated.toEntity()) }
        }
    }

    override fun removeItem(productId: String) {
        val currentList = _items.value.toMutableList()
        currentList.removeIf { it.product.id == productId }
        _items.value = currentList
        scope.launch { cartDao.deleteById(productId) }
    }

    override fun clearCart() {
        _items.value = emptyList()
        scope.launch { cartDao.clearAll() }
    }

    override fun getCartTotal(): Double = _items.value.sumOf { it.total }

    override fun getCartCount(): Int = _items.value.sumOf { it.quantity }
}

// ── Extensões de mapeamento ────────────────────────────────────────────────

private fun CartItem.toEntity() = CartItemEntity(
    productId = product.id,
    name = product.name,
    // Persiste o preço já com desconto — evita recalcular ao restaurar
    price = product.getDiscountedPrice(),
    quantity = quantity,
    imageUrl = product.imageUrl
)

private fun CartItemEntity.toCartItem(): CartItem {
    // Reconstrói um Product simplificado com o preço já descontado
    val product = Product(
        id = productId,
        name = name,
        price = price.toString(),
        imageUrl = imageUrl,
        description = "",
        category = null,
        discount = 0,   // preço já descontado — não aplicar desconto novamente
        isNew = false
    )
    return CartItem(product = product, quantity = quantity)
}
