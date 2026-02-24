package com.jefferson.antenas.ui.screens.product

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
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
    val context = LocalContext.current
    var quantity by remember { mutableStateOf(1) }
    var isFavorite by remember { mutableStateOf(false) }
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
            val title = if (uiState is ProductUiState.Success)
                (uiState as ProductUiState.Success).product.name else "Produto"
            TopAppBarCustom(title = title, onBackClick = onBackClick)
        },
        bottomBar = {
            if (uiState is ProductUiState.Success) {
                val product = (uiState as ProductUiState.Success).product
                BottomPurchaseBar(
                    price = product.getDiscountedPrice() * quantity,
                    quantity = quantity,
                    onQuantityChange = { if (it > 0) quantity = it },
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
                    val discountedPrice = product.getDiscountedPrice()
                    val installmentValue = discountedPrice / 12

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        // ── Imagem com botões sobrepostos ───────────────────
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MidnightBlueCard)
                        ) {
                            SubcomposeAsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                loading = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(color = SignalOrange, modifier = Modifier.size(32.dp))
                                    }
                                },
                                error = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Imagem não disponível", color = TextSecondary.copy(alpha = 0.6f), fontSize = 13.sp)
                                    }
                                }
                            )

                            // Botões Compartilhar + Favoritar (canto superior direito)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f), shadowElevation = 4.dp) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "${product.name} por ${discountedPrice.toCurrency()} — Jefferson Antenas"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(intent, "Compartilhar produto"))
                                        },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Default.Share, "Compartilhar", tint = MidnightBlueStart, modifier = Modifier.size(20.dp))
                                    }
                                }

                                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.92f), shadowElevation = 4.dp) {
                                    IconButton(
                                        onClick = { isFavorite = !isFavorite },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Favoritar",
                                            tint = if (isFavorite) ErrorRed else MidnightBlueStart,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            // Badges NOVO / Desconto (canto inferior esquerdo)
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (product.isNew == true) {
                                    Surface(color = SignalOrange, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "NOVO",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MidnightBlueStart
                                        )
                                    }
                                }
                                if (product.discount != null && product.discount > 0) {
                                    Surface(color = ErrorRed, shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            "-${product.discount}%",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // ── Informações principais ──────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                text = product.category?.uppercase() ?: "GERAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = SatelliteBlue
                            )

                            Spacer(Modifier.height(6.dp))

                            Text(
                                text = product.name,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = 26.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            // Avaliação da loja
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) {
                                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(15.dp))
                                }
                                Spacer(Modifier.width(6.dp))
                                Text("Produto Oficial", fontSize = 12.sp, color = TextSecondary)
                            }

                            Spacer(Modifier.height(20.dp))

                            // Preço original riscado
                            if (product.discount != null && product.discount > 0) {
                                Text(
                                    text = product.price,
                                    fontSize = 14.sp,
                                    color = TextTertiary,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            }

                            // Preço com desconto (destaque)
                            Text(
                                text = discountedPrice.toCurrency(),
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalOrange,
                                lineHeight = 38.sp
                            )

                            Spacer(Modifier.height(4.dp))

                            // Parcelamento sem juros
                            Text(
                                text = "em 12x de ${installmentValue.toCurrency()} sem juros",
                                fontSize = 13.sp,
                                color = SuccessGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // ── Envio Garantido ─────────────────────────────────
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            color = SuccessGreen.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Default.LocalShipping, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                                    Text("Envio Garantido", fontWeight = FontWeight.Bold, color = SuccessGreen, fontSize = 14.sp)
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Receba em até 7 dias úteis", fontSize = 13.sp, color = TextSecondary)
                                Spacer(Modifier.height(4.dp))
                                if (discountedPrice >= 100.0) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Frete Grátis", fontSize = 13.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                } else {
                                    Text("Frete calculado no checkout", fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // ── Card da Loja / Reputação ────────────────────────
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 4.dp),
                            color = MidnightBlueCard,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(SignalOrange),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("J", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = MidnightBlueStart)
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Jefferson Antenas", fontWeight = FontWeight.Bold, color = TextPrimary, fontSize = 15.sp)
                                    Spacer(Modifier.height(3.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        repeat(5) {
                                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                                        }
                                    }
                                    Spacer(Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        StoreTag("✓ Verificado")
                                        StoreTag("📦 Envio Rápido")
                                    }
                                }

                                Icon(Icons.Default.ArrowForward, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        // ── Sobre o Produto ─────────────────────────────────
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Sobre o Produto",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                lineHeight = 24.sp
                            )
                        }

                        Spacer(Modifier.height(120.dp))
                    }
                }
            }

            ModernSuccessToast(
                visible = showToast,
                message = "Item adicionado ao carrinho!",
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// ── Tag pequena usada no card da loja ──────────────────────────────────────
@Composable
private fun StoreTag(text: String) {
    Surface(
        color = MidnightBlueStart,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// ── Barra inferior de compra ───────────────────────────────────────────────
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Seletor de quantidade
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MidnightBlueStart)
            ) {
                IconButton(
                    onClick = { onQuantityChange(quantity - 1) },
                    enabled = quantity > 1,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        "Remover",
                        tint = if (quantity > 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp)
                )
                IconButton(
                    onClick = { onQuantityChange(quantity + 1) },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Add, "Adicionar", tint = TextPrimary, modifier = Modifier.size(16.dp))
                }
            }

            Spacer(Modifier.width(12.dp))

            // Botão adicionar ao carrinho
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = MidnightBlueStart, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "Adicionar ao Carrinho",
                        color = MidnightBlueStart,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        price.toCurrency(),
                        color = MidnightBlueStart.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
