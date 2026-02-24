package com.jefferson.antenas.ui.screens.product

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.jefferson.antenas.ui.componets.ModernSuccessToast
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay
import java.net.URLEncoder

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL
// ══════════════════════════════════════════════════════════════════════════════

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
    var toastMessage by remember { mutableStateOf("Item adicionado ao carrinho!") }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2500)
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
                        toastMessage = "✓ Adicionado ao carrinho!"
                        showToast = true
                    },
                    onBuyNow = {
                        viewModel.addToCart(product, quantity)
                        toastMessage = "Redirecionando para o checkout..."
                        showToast = true
                    },
                    onWhatsApp = {
                        val phone = "5565992895296"
                        val msg = "Olá Jefferson! Tenho interesse no produto: *${product.name}*\n" +
                                "Preço: ${(product.getDiscountedPrice() * quantity).toCurrency()}\n" +
                                "Quantidade: $quantity"
                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                URLEncoder.encode(msg, "UTF-8")
                            }"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
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
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(color = SignalOrange, strokeWidth = 3.dp)
                        Text("Carregando produto...", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                is ProductUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                        Text("Erro ao carregar produto", color = ErrorRed, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text(state.message, color = TextSecondary, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                is ProductUiState.Success -> {
                    val product = state.product
                    val discountedPrice = product.getDiscountedPrice()
                    val originalPrice = product.price.trim()
                        .replace("R$", "").replace(" ", "").replace(",", ".")
                        .toDoubleOrNull() ?: discountedPrice
                    val installmentValue = discountedPrice / 12
                    val pixPrice = discountedPrice * 0.95
                    val savings = originalPrice - discountedPrice

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 1 — IMAGEM DO PRODUTO
                        // ══════════════════════════════════════════════════════
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .background(MidnightBlueCard)
                        ) {
                            SubcomposeAsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                loading = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator(
                                            color = SignalOrange,
                                            modifier = Modifier.size(36.dp),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                },
                                error = {
                                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(Icons.Default.Image, null, tint = TextTertiary, modifier = Modifier.size(52.dp))
                                            Spacer(Modifier.height(8.dp))
                                            Text("Imagem não disponível", color = TextSecondary.copy(alpha = 0.6f), fontSize = 12.sp)
                                        }
                                    }
                                }
                            )

                            // ── Contador de imagem (canto superior esquerdo) ────
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp),
                                color = MidnightBlueStart.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "1/1",
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            // ── Share + Favorito (canto superior direito) ───────
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.93f),
                                    shadowElevation = 6.dp
                                ) {
                                    IconButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "🔥 ${product.name}\n" +
                                                            "💰 ${discountedPrice.toCurrency()}\n\n" +
                                                            "Encontrei na Jefferson Antenas — qualidade garantida! ✅"
                                                )
                                            }
                                            context.startActivity(
                                                Intent.createChooser(intent, "Compartilhar produto")
                                            )
                                        },
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            "Compartilhar",
                                            tint = MidnightBlueStart,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.93f),
                                    shadowElevation = 6.dp
                                ) {
                                    IconButton(
                                        onClick = { isFavorite = !isFavorite },
                                        modifier = Modifier.size(42.dp)
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

                            // ── Badges NOVO / Desconto (canto inferior esquerdo) ─
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (product.isNew == true) {
                                    Surface(
                                        color = SignalOrange,
                                        shape = RoundedCornerShape(6.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            "NOVO",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MidnightBlueStart
                                        )
                                    }
                                }
                                if (product.discount != null && product.discount > 0) {
                                    Surface(
                                        color = ErrorRed,
                                        shape = RoundedCornerShape(6.dp),
                                        shadowElevation = 2.dp
                                    ) {
                                        Text(
                                            "-${product.discount}% OFF",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // ── Indicador "pessoas vendo" (canto inferior direito) ─
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                color = MidnightBlueStart.copy(alpha = 0.72f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(SuccessGreen)
                                    )
                                    Text("12 vendo agora", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 2 — TÍTULO + AVALIAÇÃO + VENDIDOS
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 14.dp)
                        ) {
                            // Breadcrumb
                            product.category?.let { cat ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.Home, null, tint = TextTertiary, modifier = Modifier.size(12.dp))
                                    Text(" › $cat", fontSize = 11.sp, color = SatelliteBlue)
                                }
                                Spacer(Modifier.height(8.dp))
                            }

                            // Nome do produto
                            Text(
                                text = product.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                lineHeight = 26.sp
                            )

                            Spacer(Modifier.height(10.dp))

                            // Rating + vendidos
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    repeat(5) {
                                        Icon(
                                            Icons.Default.Star, null,
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Text(
                                    "4.9",
                                    fontSize = 13.sp,
                                    color = Color(0xFFFFC107),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    "(127 avaliações)",
                                    fontSize = 12.sp,
                                    color = SatelliteBlue,
                                    textDecoration = TextDecoration.Underline
                                )
                                Text("•", fontSize = 12.sp, color = TextTertiary)
                                Text("+200 vendidos", fontSize = 12.sp, color = TextSecondary)
                            }

                            Spacer(Modifier.height(6.dp))

                            // SKU + Código
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    "Cód: ${product.id.take(8).uppercase()}",
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                                Text(
                                    "Disponibilidade: Imediata",
                                    fontSize = 10.sp,
                                    color = SuccessGreen
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 3 — PREÇO + PARCELAMENTO + PAGAMENTOS
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Preço original riscado + economia
                            if (product.discount != null && product.discount > 0) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        "De: ${product.price}",
                                        fontSize = 13.sp,
                                        color = TextTertiary,
                                        textDecoration = TextDecoration.LineThrough
                                    )
                                    if (savings > 0) {
                                        Surface(
                                            color = ErrorRed.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "Economize ${savings.toCurrency()}",
                                                fontSize = 11.sp,
                                                color = ErrorRed,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Preço principal em destaque
                            Text(
                                text = discountedPrice.toCurrency(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SignalOrange,
                                lineHeight = 38.sp
                            )

                            // Desconto no PIX
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = SuccessGreen,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "PIX",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            "${pixPrice.toCurrency()} à vista",
                                            fontSize = 14.sp,
                                            color = SuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "5% de desconto pagando com PIX",
                                            fontSize = 11.sp,
                                            color = SuccessGreen.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            // Parcelamento no cartão
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CreditCard, null, tint = SatelliteBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    "12x de ${installmentValue.toCurrency()} sem juros",
                                    fontSize = 13.sp,
                                    color = TextSecondary
                                )
                            }

                            Spacer(Modifier.height(4.dp))

                            // Formas de pagamento
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("Aceitos:", fontSize = 11.sp, color = TextTertiary)
                                PaymentBadge("PIX", SuccessGreen)
                                PaymentBadge("Crédito", SatelliteBlue)
                                PaymentBadge("Débito", SatelliteBlue)
                                PaymentBadge("Boleto", TextTertiary)
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 4 — ESTOQUE E DISPONIBILIDADE
                        // ══════════════════════════════════════════════════════
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                color = SuccessGreen.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Text(
                                        "Em estoque",
                                        fontSize = 12.sp,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                "Pronta entrega • Envio imediato após pagamento",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 5 — ENTREGA E FRETE
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Entrega e Frete",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            ShippingRow(
                                icon = Icons.Default.LocalShipping,
                                iconColor = SuccessGreen,
                                title = "Enviamos para todo o Brasil",
                                subtitle = "Estimativa: 5 a 10 dias úteis após aprovação"
                            )

                            if (discountedPrice >= 100.0) {
                                ShippingRow(
                                    icon = Icons.Default.CheckCircle,
                                    iconColor = SuccessGreen,
                                    title = "Frete Grátis neste pedido!",
                                    subtitle = "Você se qualificou ao frete gratuito",
                                    titleColor = SuccessGreen
                                )
                            } else {
                                val missing = 100.0 - discountedPrice
                                ShippingRow(
                                    icon = Icons.Default.Info,
                                    iconColor = SignalOrange,
                                    title = "Frete será calculado no checkout",
                                    subtitle = "Adicione mais ${missing.toCurrency()} e ganhe frete grátis"
                                )
                            }

                            ShippingRow(
                                icon = Icons.Default.Refresh,
                                iconColor = SatelliteBlue,
                                title = "Devolução garantida",
                                subtitle = "Devolva grátis em até 7 dias após o recebimento"
                            )

                            ShippingRow(
                                icon = Icons.Default.Security,
                                iconColor = SignalOrange,
                                title = "Compra 100% segura",
                                subtitle = "Dados protegidos e pagamento criptografado"
                            )
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 6 — SELOS DE CONFIANÇA (4 cards)
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                "Compra Garantida",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TrustBadgeCard("🔒", "Pagamento\nSeguro", Modifier.weight(1f))
                                TrustBadgeCard("✅", "Produto\nOriginal", Modifier.weight(1f))
                                TrustBadgeCard("↩", "Devolução\nGrátis", Modifier.weight(1f))
                                TrustBadgeCard("💬", "Suporte\nWhatsApp", Modifier.weight(1f))
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 7 — CARD DA LOJA / REPUTAÇÃO DO VENDEDOR
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Text(
                                "Vendido e entregue por",
                                fontSize = 12.sp,
                                color = TextTertiary
                            )
                            Spacer(Modifier.height(10.dp))

                            Surface(
                                color = MidnightBlueCard,
                                shape = RoundedCornerShape(14.dp),
                                shadowElevation = 4.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {

                                    // Header: avatar + nome + badge
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(56.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    Brush.linearGradient(
                                                        listOf(SignalOrange, SignalOrangeDark)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                "J",
                                                fontSize = 26.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MidnightBlueStart
                                            )
                                        }

                                        Spacer(Modifier.width(12.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    "Jefferson Antenas",
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary,
                                                    fontSize = 15.sp
                                                )
                                                Surface(
                                                    color = SuccessGreen.copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(4.dp)
                                                ) {
                                                    Text(
                                                        "VERIFICADO",
                                                        fontSize = 8.sp,
                                                        color = SuccessGreen,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Spacer(Modifier.height(3.dp))
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                repeat(5) {
                                                    Icon(
                                                        Icons.Default.Star, null,
                                                        tint = Color(0xFFFFC107),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    "100% avaliações positivas",
                                                    fontSize = 11.sp,
                                                    color = TextSecondary
                                                )
                                            }
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                "Respondemos em menos de 1 hora",
                                                fontSize = 10.sp,
                                                color = SuccessGreen
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))
                                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                                    Spacer(Modifier.height(14.dp))

                                    // Stats da loja
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        StoreStatItem("200+", "Vendas")
                                        Box(
                                            modifier = Modifier
                                                .width(0.5.dp)
                                                .height(36.dp)
                                                .background(CardBorder)
                                        )
                                        StoreStatItem("4.9★", "Avaliação")
                                        Box(
                                            modifier = Modifier
                                                .width(0.5.dp)
                                                .height(36.dp)
                                                .background(CardBorder)
                                        )
                                        StoreStatItem("< 1h", "Resposta")
                                        Box(
                                            modifier = Modifier
                                                .width(0.5.dp)
                                                .height(36.dp)
                                                .background(CardBorder)
                                        )
                                        StoreStatItem("5 anos", "No mercado")
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    // Barra de reputação estilo Mercado Livre
                                    ReputationBar()

                                    Spacer(Modifier.height(14.dp))

                                    // Botão WhatsApp
                                    OutlinedButton(
                                        onClick = {
                                            val phone = "5565992895296"
                                            val msg = "Olá Jefferson! Gostaria de tirar dúvidas sobre um produto."
                                            try {
                                                val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                                    URLEncoder.encode(msg, "UTF-8")
                                                }"
                                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                            } catch (_: Exception) {}
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(42.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = SuccessGreen
                                        ),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp, SuccessGreen
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Message, null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text(
                                            "Falar com o Vendedor",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 8 — ESPECIFICAÇÕES TÉCNICAS (expansível)
                        // ══════════════════════════════════════════════════════
                        var specsExpanded by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { specsExpanded = !specsExpanded },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Default.List, null,
                                        tint = SignalOrange,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Especificações Técnicas",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                }
                                Icon(
                                    if (specsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column(modifier = Modifier.animateContentSize(animationSpec = tween(300))) {
                                if (specsExpanded) {
                                    Spacer(Modifier.height(14.dp))
                                    val specs = buildProductSpecs(product.category)
                                    specs.forEachIndexed { index, (key, value) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    if (index % 2 == 0)
                                                        CardGradientStart.copy(alpha = 0.5f)
                                                    else Color.Transparent
                                                )
                                                .padding(horizontal = 10.dp, vertical = 9.dp)
                                        ) {
                                            Text(
                                                key,
                                                fontSize = 12.sp,
                                                color = TextTertiary,
                                                modifier = Modifier.weight(0.42f)
                                            )
                                            Text(
                                                value,
                                                fontSize = 12.sp,
                                                color = TextPrimary,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.weight(0.58f)
                                            )
                                        }
                                    }
                                } else {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        "Toque para ver as especificações completas →",
                                        fontSize = 12.sp,
                                        color = SatelliteBlue
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 9 — SOBRE O PRODUTO (expansível)
                        // ══════════════════════════════════════════════════════
                        var descExpanded by remember { mutableStateOf(false) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info, null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Sobre o Produto",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(Modifier.height(10.dp))

                            Column(modifier = Modifier.animateContentSize(animationSpec = tween(300))) {
                                Text(
                                    text = product.description,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    lineHeight = 21.sp,
                                    maxLines = if (descExpanded) Int.MAX_VALUE else 4,
                                    overflow = if (descExpanded) TextOverflow.Clip else TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.height(6.dp))

                            TextButton(
                                onClick = { descExpanded = !descExpanded },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text(
                                    if (descExpanded) "▲ Ver menos" else "▼ Ver descrição completa",
                                    color = SatelliteBlue,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 10 — AVALIAÇÕES DOS CLIENTES
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Avaliações dos Clientes",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(Modifier.height(14.dp))

                            // Visão geral: nota grande + barras de distribuição
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                // Nota grande
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "4.9",
                                        fontSize = 52.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = SignalOrange,
                                        lineHeight = 52.sp
                                    )
                                    Row {
                                        repeat(5) {
                                            Icon(
                                                Icons.Default.Star, null,
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text("127 avaliações", fontSize = 10.sp, color = TextTertiary)
                                }

                                // Barras de distribuição
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    RatingBar(stars = 5, percent = 0.85f, count = 108)
                                    RatingBar(stars = 4, percent = 0.10f, count = 13)
                                    RatingBar(stars = 3, percent = 0.03f, count = 4)
                                    RatingBar(stars = 2, percent = 0.01f, count = 1)
                                    RatingBar(stars = 1, percent = 0.01f, count = 1)
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Tags de destaque das avaliações
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ReviewTagChip("Ótima qualidade", 89)
                                ReviewTagChip("Entrega rápida", 72)
                                ReviewTagChip("Vale o preço", 65)
                            }

                            Spacer(Modifier.height(16.dp))
                            HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                            Spacer(Modifier.height(14.dp))

                            // Avaliações individuais
                            ReviewCard(
                                name = "Carlos M.",
                                rating = 5,
                                date = "Jan 2025",
                                tag = "Compra Verificada",
                                comment = "Produto excelente! Chegou bem embalado e no prazo combinado. " +
                                        "O sinal melhorou muito depois da instalação. Jefferson tirou todas as dúvidas pelo WhatsApp. Recomendo!"
                            )
                            Spacer(Modifier.height(10.dp))
                            ReviewCard(
                                name = "Ana R.",
                                rating = 5,
                                date = "Dez 2024",
                                tag = "Compra Verificada",
                                comment = "Atendimento nota 10! Recebi o produto em perfeitas condições. " +
                                        "Qualidade muito boa e o suporte pós-venda foi excepcional."
                            )
                            Spacer(Modifier.height(10.dp))
                            ReviewCard(
                                name = "Roberto S.",
                                rating = 5,
                                date = "Nov 2024",
                                tag = "Compra Verificada",
                                comment = "Segunda compra com o Jefferson. Sempre entrega no prazo e o produto é original. " +
                                        "Loja de confiança, pode comprar sem medo!"
                            )

                            Spacer(Modifier.height(10.dp))

                            // Ver mais avaliações
                            OutlinedButton(
                                onClick = { /* navigate to reviews */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = SatelliteBlue
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SatelliteBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    "Ver todas as 127 avaliações",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        HorizontalDivider(color = CardBorder, thickness = 0.5.dp)

                        // ══════════════════════════════════════════════════════
                        // SEÇÃO 11 — POLÍTICA DE GARANTIA
                        // ══════════════════════════════════════════════════════
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Shield, null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Garantia e Suporte",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }

                            Spacer(Modifier.height(12.dp))

                            Surface(
                                color = CardGradientStart,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GuaranteeRow(
                                        icon = Icons.Default.Verified,
                                        text = "12 meses de garantia contra defeitos de fabricação"
                                    )
                                    GuaranteeRow(
                                        icon = Icons.Default.SupportAgent,
                                        text = "Suporte técnico via WhatsApp incluído"
                                    )
                                    GuaranteeRow(
                                        icon = Icons.Default.Replay,
                                        text = "Devolução em até 7 dias se não gostar"
                                    )
                                    GuaranteeRow(
                                        icon = Icons.Default.Build,
                                        text = "Instalação e configuração disponível na região"
                                    )
                                }
                            }
                        }

                        // Espaço extra para a bottom bar não cobrir o conteúdo
                        Spacer(Modifier.height(160.dp))
                    }
                }
            }

            // Toast de confirmação
            ModernSuccessToast(
                visible = showToast,
                message = toastMessage,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTES PRIVADOS DE SUPORTE
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PaymentBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            label,
            fontSize = 10.sp,
            color = color,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ShippingRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    titleColor: Color = TextPrimary
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            color = iconColor.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                icon, null,
                tint = iconColor,
                modifier = Modifier
                    .size(34.dp)
                    .padding(7.dp)
            )
        }
        Column {
            Text(title, fontSize = 13.sp, color = titleColor, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
        }
    }
}

@Composable
private fun TrustBadgeCard(icon: String, label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CardGradientStart,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 20.sp)
            Spacer(Modifier.height(5.dp))
            Text(
                label,
                fontSize = 9.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun StoreStatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = SignalOrange
        )
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 9.sp, color = TextTertiary, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReputationBar() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Reputação do Vendedor", fontSize = 11.sp, color = TextTertiary)
            Text("Excelente", fontSize = 11.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(6.dp))

        // Barra de 5 segmentos: vermelho → verde (estilo Mercado Livre)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            val segmentColors = listOf(
                ErrorRed,
                Color(0xFFFF7043),
                WarningYellow,
                Color(0xFF66BB6A),
                SuccessGreen
            )
            segmentColors.forEachIndexed { index, color ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(
                            when (index) {
                                0 -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)
                                4 -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                else -> RoundedCornerShape(0.dp)
                            }
                        )
                        .background(color)
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Novo", fontSize = 9.sp, color = TextTertiary)
            Text("Experiente", fontSize = 9.sp, color = TextTertiary)
            Text("Excelente ★", fontSize = 9.sp, color = SuccessGreen, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RatingBar(stars: Int, percent: Float, count: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            "$stars★",
            fontSize = 11.sp,
            color = TextSecondary,
            modifier = Modifier.width(26.dp)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(7.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardGradientStart)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percent)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFFFFC107))
            )
        }
        Text(
            "$count",
            fontSize = 10.sp,
            color = TextTertiary,
            modifier = Modifier.width(24.dp)
        )
    }
}

@Composable
private fun ReviewTagChip(label: String, count: Int) {
    Surface(
        color = SatelliteBlue.copy(alpha = 0.12f),
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SatelliteBlue.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("👍", fontSize = 10.sp)
            Text(label, fontSize = 10.sp, color = TextSecondary)
            Text("($count)", fontSize = 9.sp, color = TextTertiary)
        }
    }
}

@Composable
private fun ReviewCard(
    name: String,
    rating: Int,
    date: String,
    tag: String,
    comment: String
) {
    Surface(
        color = MidnightBlueCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabeçalho: avatar + nome + nota
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SatelliteBlue.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        name.first().toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SatelliteBlue
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(rating) {
                            Icon(
                                Icons.Default.Star, null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(date, fontSize = 10.sp, color = TextTertiary)
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            tag,
                            fontSize = 8.sp,
                            color = SuccessGreen,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // Texto da avaliação
            Text(
                comment,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(8.dp))

            // Botão "Útil?"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Útil?", fontSize = 10.sp, color = TextTertiary)
                Surface(
                    color = CardGradientStart,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.ThumbUp, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                        Text("Sim", fontSize = 10.sp, color = TextTertiary)
                    }
                }
                Surface(
                    color = CardGradientStart,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.clickable { }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(Icons.Default.ThumbDown, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                        Text("Não", fontSize = 10.sp, color = TextTertiary)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuaranteeRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
        Text(text, fontSize = 12.sp, color = TextSecondary, lineHeight = 18.sp)
    }
}

// Gera specs técnicas com base na categoria do produto
private fun buildProductSpecs(category: String?): List<Pair<String, String>> {
    return when (category?.lowercase()) {
        "antenas", "antena" -> listOf(
            "Marca" to "Jefferson Antenas",
            "Tipo" to "Parabólica Digital",
            "Frequência" to "10.7 – 12.75 GHz",
            "Ganho" to "39 dBi",
            "Polarização" to "Linear e Circular",
            "Diâmetro" to "60 cm",
            "Material" to "Alumínio / ABS",
            "Instalação" to "Externa ou Interna",
            "Conector" to "F-Type (Rosca)",
            "Garantia" to "12 meses"
        )
        "receptor", "receptores" -> listOf(
            "Marca" to "Jefferson Antenas",
            "Tipo" to "Receptor Digital Full HD",
            "Resolução" to "1080p Full HD",
            "Entradas" to "HDMI, USB 2.0, Satélite",
            "Wi-Fi" to "Integrado 2.4 GHz",
            "Gravação" to "Via USB (PVR)",
            "Idioma" to "Português BR",
            "Tensão" to "Bivolt 100–240V",
            "Consumo" to "≤ 15W",
            "Garantia" to "12 meses"
        )
        "cabo", "cabos" -> listOf(
            "Marca" to "Jefferson Antenas",
            "Tipo" to "Coaxial RG-6",
            "Impedância" to "75 Ohm",
            "Blindagem" to "Quádrupla",
            "Atenuação" to "< 5 dB/100m",
            "Conector" to "F-Type / BNC",
            "Condutor" to "Cobre estanhado",
            "Cobertura" to "PVC preto UV",
            "Comprimento" to "Variável (sob pedido)",
            "Garantia" to "6 meses"
        )
        else -> listOf(
            "Marca" to "Jefferson Antenas",
            "Origem" to "Nacional",
            "Garantia" to "12 meses",
            "Suporte" to "WhatsApp Incluso",
            "Entrega" to "Todo o Brasil",
            "Pagamento" to "Pix, Cartão, Boleto",
            "Devolução" to "7 dias sem custo",
            "Nota Fiscal" to "Emitida no ato"
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BARRA INFERIOR DE COMPRA (3 ações + seletor de quantidade)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun BottomPurchaseBar(
    price: Double,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit,
    onBuyNow: () -> Unit = {},
    onWhatsApp: () -> Unit = {}
) {
    Surface(
        color = MidnightBlueCard,
        shadowElevation = 24.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            // Linha 1: Seletor de quantidade + preço total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Seletor de quantidade
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MidnightBlueStart)
                ) {
                    IconButton(
                        onClick = { onQuantityChange(quantity - 1) },
                        enabled = quantity > 1,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove, "Remover",
                            tint = if (quantity > 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        quantity.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    IconButton(
                        onClick = { onQuantityChange(quantity + 1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Add, "Adicionar",
                            tint = SignalOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Preço total
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", fontSize = 10.sp, color = TextTertiary)
                    Text(
                        price.toCurrency(),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SignalOrange
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Linha 2: Botões de ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão WhatsApp (quadrado)
                Surface(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onWhatsApp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Message, "WhatsApp",
                            tint = SuccessGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Adicionar ao Carrinho (outlined)
                OutlinedButton(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SignalOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Carrinho", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Comprar Agora (sólido)
                Button(
                    onClick = onBuyNow,
                    modifier = Modifier
                        .weight(1.4f)
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "Comprar Agora",
                        color = MidnightBlueStart,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}
