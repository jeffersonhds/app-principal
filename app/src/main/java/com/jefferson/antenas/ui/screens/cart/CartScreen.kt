package com.jefferson.antenas.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    val cartItems = cartRepository.items

    fun increaseQuantity(item: CartItem) {
        cartRepository.updateQuantity(item.product.id, item.quantity + 1)
    }

    fun decreaseQuantity(item: CartItem) {
        cartRepository.updateQuantity(item.product.id, item.quantity - 1)
    }

    fun removeItem(item: CartItem) {
        cartRepository.removeItem(item.product.id)
    }

    fun getTotal(): Double = cartRepository.getCartTotal()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val items by viewModel.cartItems.collectAsState()
    val total = items.sumOf { it.total }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seu Carrinho", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MidnightBlueStart
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    color = MidnightBlueCard,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text(total.toCurrency(), style = MaterialTheme.typography.titleLarge, color = SignalOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = Shapes.medium
                        ) {
                            Text("Finalizar Compra", color = MidnightBlueStart, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = TextTertiary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Seu carrinho está vazio",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextTertiary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adicione produtos para continuar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextTertiary.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(item) },
                        onDecrease = { viewModel.decreaseQuantity(item) },
                        onRemove = { viewModel.removeItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        shape = Shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(Shapes.small).background(Color.White)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1)
                Text(item.product.getDiscountedPrice().toCurrency(), style = MaterialTheme.typography.bodyMedium, color = SignalOrange)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDecrease,
                        modifier = Modifier.size(32.dp),
                        enabled = item.quantity > 1
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            null,
                            tint = if (item.quantity > 1) TextSecondary else TextSecondary.copy(alpha = 0.3f)
                        )
                    }
                    Text(
                        item.quantity.toString(),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, tint = TextSecondary)
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, null, tint = ErrorRed)
            }
        }
    }
}