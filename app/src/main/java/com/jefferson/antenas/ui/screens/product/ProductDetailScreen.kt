package com.jefferson.antenas.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.componets.ModernSuccessToast
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var quantity by remember { mutableStateOf(1) }

    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            if (uiState is ProductUiState.Success) {
                val product = (uiState as ProductUiState.Success).product
                TopAppBarCustom(title = product.name, onBackClick = onBackClick)
            }
        },
        bottomBar = {
            if (uiState is ProductUiState.Success) {
                val product = (uiState as ProductUiState.Success).product
                BottomPurchaseBar(
                    price = product.getDiscountedPrice() * quantity,
                    quantity = quantity,
                    onQuantityChange = { newQuantity ->
                        if (newQuantity > 0) {
                            quantity = newQuantity
                        }
                    },
                    onAddToCart = {
                        viewModel.addToCart(product, quantity)
                        showToast = true
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = BackgroundGradient)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SignalOrange
                    )
                }
                is ProductUiState.Error -> {
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProductUiState.Success -> {
                    val product = state.product

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MidnightBlueCard)
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product.category?.uppercase() ?: "GERAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SatelliteBlue
                                )
                                if (product.isNew == true) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = SignalOrange,
                                        shape = Shapes.small
                                    ) {
                                        Text(
                                            text = "NOVO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MidnightBlueStart
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (product.discount != null && product.discount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "-${product.discount}%",
                                        color = ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(ErrorRed.copy(alpha = 0.1f), Shapes.small)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = product.price.toCurrency(),
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = TextTertiary
                                    )
                                }
                            }
                            Text(
                                text = product.getDiscountedPrice().toCurrency(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SignalOrange
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = CardBorder
                            )
                            Text(
                                text = "Sobre o Produto",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }

            // CORREÇÃO: Toast movido para dentro do Box para ter o escopo correto
            ModernSuccessToast(
                visible = showToast,
                message = "Item adicionado!",
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun BottomPurchaseBar(
    price: Double,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        color = MidnightBlueCard,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(quantity - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Remover um", tint = TextPrimary)
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar um", tint = TextPrimary)
                }
            }
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = Shapes.medium,
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MidnightBlueStart)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comprar Agora",
                    color = MidnightBlueStart,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
