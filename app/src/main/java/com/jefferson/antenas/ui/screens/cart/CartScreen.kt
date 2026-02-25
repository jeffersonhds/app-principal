package com.jefferson.antenas.ui.screens.cart

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.SubcomposeAsyncImage
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import java.net.URLEncoder
import javax.inject.Inject

// ══════════════════════════════════════════════════════════════════════════════
// VIEWMODEL (lógica inalterada)
// ══════════════════════════════════════════════════════════════════════════════

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

    fun clearAll(items: List<CartItem>) {
        items.forEach { cartRepository.removeItem(it.product.id) }
    }

    fun getTotal(): Double = cartRepository.getCartTotal()
}

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL
// ══════════════════════════════════════════════════════════════════════════════

private const val FREE_SHIPPING_THRESHOLD = 100.0
private const val PIX_DISCOUNT = 0.05
private val VALID_COUPONS = mapOf("JEFF10" to 0.10, "ANTENAS15" to 0.15)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val items by viewModel.cartItems.collectAsState()

    var couponCode by remember { mutableStateOf("") }
    var couponApplied by remember { mutableStateOf(false) }
    var couponDiscount by remember { mutableStateOf(0.0) }
    var couponError by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    val subtotal = items.sumOf { it.total }
    val discountAmount = if (couponApplied) subtotal * couponDiscount else 0.0
    val total = subtotal - discountAmount
    val pixTotal = total * (1.0 - PIX_DISCOUNT)
    val installment = total / 12.0
    val freeShipping = total >= FREE_SHIPPING_THRESHOLD
    val missingForFreeShip = (FREE_SHIPPING_THRESHOLD - total).coerceAtLeast(0.0)

    // Diálogo de confirmação "Limpar carrinho"
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = MidnightBlueCard,
            icon = {
                Icon(Icons.Default.DeleteForever, null, tint = ErrorRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Limpar carrinho?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Todos os ${items.size} itens serão removidos.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAll(items)
                        showClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Limpar tudo", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Carrinho",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        if (items.isNotEmpty()) {
                            Surface(
                                color = SignalOrange,
                                shape = CircleShape
                            ) {
                                Text(
                                    items.sumOf { it.quantity }.toString(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MidnightBlueStart,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (items.isNotEmpty()) {
                        TextButton(onClick = { showClearDialog = true }) {
                            Text("Limpar", color = ErrorRed, fontSize = 13.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MidnightBlueStart
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                CartBottomBar(
                    total = total,
                    pixTotal = pixTotal,
                    installment = installment,
                    onCheckout = onCheckoutClick,
                    onWhatsApp = {
                        val phone = "5565992895296"
                        val itemList = items.joinToString("\n") {
                            "• ${it.product.name} x${it.quantity} — ${it.total.toCurrency()}"
                        }
                        val msg = "Olá Jefferson! Gostaria de finalizar o pedido:\n\n$itemList\n\n*Total: ${total.toCurrency()}*"
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
        if (items.isEmpty()) {
            EmptyCartContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                onGoToStore = onBackClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(brush = BackgroundGradient),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                // ── Banner de frete grátis (progresso) ──────────────────────
                item {
                    FreeShippingBanner(
                        total = total,
                        freeShipping = freeShipping,
                        missing = missingForFreeShip
                    )
                }

                // ── Header "Meus Itens" ──────────────────────────────────────
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Meus Itens",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            "${items.size} produto${if (items.size != 1) "s" else ""}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // ── Cards de itens ───────────────────────────────────────────
                items(items) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(item) },
                        onDecrease = { viewModel.decreaseQuantity(item) },
                        onRemove = { viewModel.removeItem(item) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                // ── Separador ────────────────────────────────────────────────
                item { Spacer(Modifier.height(8.dp)) }

                // ── Seção de cupom ───────────────────────────────────────────
                item {
                    CouponSection(
                        couponCode = couponCode,
                        onCouponChange = {
                            couponCode = it.uppercase()
                            couponError = false
                        },
                        couponApplied = couponApplied,
                        couponDiscount = couponDiscount,
                        couponError = couponError,
                        onApply = {
                            val discount = VALID_COUPONS[couponCode.trim()]
                            if (discount != null) {
                                couponDiscount = discount
                                couponApplied = true
                                couponError = false
                            } else {
                                couponError = true
                                couponApplied = false
                            }
                        },
                        onRemoveCoupon = {
                            couponApplied = false
                            couponDiscount = 0.0
                            couponCode = ""
                            couponError = false
                        },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }

                // ── Resumo do pedido ─────────────────────────────────────────
                item {
                    OrderSummaryCard(
                        subtotal = subtotal,
                        discountAmount = discountAmount,
                        couponApplied = couponApplied,
                        couponCode = couponCode,
                        couponPercent = (couponDiscount * 100).toInt(),
                        freeShipping = freeShipping,
                        total = total,
                        pixTotal = pixTotal,
                        installment = installment,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Selos de confiança ───────────────────────────────────────
                item {
                    CartTrustBadges(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // ── Espaço para a bottom bar ────────────────────────────────
                item { Spacer(Modifier.height(120.dp)) }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BANNER DE PROGRESSO PARA FRETE GRÁTIS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FreeShippingBanner(
    total: Double,
    freeShipping: Boolean,
    missing: Double,
    modifier: Modifier = Modifier
) {
    val progress = (total / FREE_SHIPPING_THRESHOLD).coerceIn(0.0, 1.0).toFloat()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = if (freeShipping) SuccessGreen.copy(alpha = 0.12f) else CardGradientStart,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (freeShipping) SuccessGreen.copy(alpha = 0.4f) else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalShipping, null,
                    tint = if (freeShipping) SuccessGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                if (freeShipping) {
                    Text(
                        "🎉 Frete Grátis garantido neste pedido!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SuccessGreen
                    )
                } else {
                    Text(
                        "Falta ",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Text(
                        missing.toCurrency(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalOrange
                    )
                    Text(
                        " para frete grátis",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            if (!freeShipping) {
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MidnightBlueStart)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(SignalOrange, SuccessGreen)
                                )
                            )
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("R$ 0", fontSize = 9.sp, color = TextTertiary)
                    Text("R$ 100", fontSize = 9.sp, color = TextTertiary)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CARD DE ITEM DO CARRINHO (redesenhado)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    val discountedPrice = item.product.getDiscountedPrice()
    val hasDiscount = (item.product.discount ?: 0) > 0

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ── Linha 1: Imagem + Infos do produto ──────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Imagem
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White)
                ) {
                    SubcomposeAsyncImage(
                        model = item.product.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(6.dp),
                        loading = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(
                                    color = SignalOrange,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                        },
                        error = {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Image, null,
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    )

                    // Badge de desconto na imagem
                    if (hasDiscount) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopStart),
                            color = ErrorRed,
                            shape = RoundedCornerShape(topStart = 10.dp, bottomEnd = 6.dp)
                        ) {
                            Text(
                                "-${item.product.discount}%",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(12.dp))

                // Infos do produto
                Column(modifier = Modifier.weight(1f)) {
                    // Categoria
                    item.product.category?.let { cat ->
                        Text(
                            cat.uppercase(),
                            fontSize = 9.sp,
                            color = SatelliteBlue,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(Modifier.height(2.dp))
                    }

                    // Nome
                    Text(
                        text = item.product.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    Spacer(Modifier.height(6.dp))

                    // Preço unitário
                    if (hasDiscount) {
                        Text(
                            "Un: ${item.product.price}",
                            fontSize = 11.sp,
                            color = TextTertiary,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                    Text(
                        "Un: ${discountedPrice.toCurrency()}",
                        fontSize = 13.sp,
                        color = SignalOrange,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(4.dp))

                    // Badge "Em estoque"
                    Surface(
                        color = SuccessGreen.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle, null,
                                tint = SuccessGreen,
                                modifier = Modifier.size(9.dp)
                            )
                            Text("Em estoque", fontSize = 9.sp, color = SuccessGreen)
                        }
                    }
                }

                // Botão remover (canto direito)
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.Delete, "Remover",
                        tint = ErrorRed.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(10.dp))

            // ── Linha 2: Seletor de quantidade + subtotal ────────────────────
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
                        onClick = onDecrease,
                        enabled = item.quantity > 1,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove, null,
                            tint = if (item.quantity > 1) TextPrimary else TextSecondary.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        item.quantity.toString(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(
                        onClick = onIncrease,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            Icons.Default.Add, null,
                            tint = SignalOrange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Subtotal do item
                Column(horizontalAlignment = Alignment.End) {
                    Text("Subtotal", fontSize = 10.sp, color = TextTertiary)
                    Text(
                        item.total.toCurrency(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    if (hasDiscount) {
                        val originalTotal = (item.product.price.trim()
                            .replace("R$", "").replace(" ", "").replace(",", ".")
                            .toDoubleOrNull() ?: item.total) * item.quantity
                        val savedAmount = originalTotal - item.total
                        if (savedAmount > 0.01) {
                            Text(
                                "Economizou ${savedAmount.toCurrency()}",
                                fontSize = 9.sp,
                                color = SuccessGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SEÇÃO DE CUPOM DE DESCONTO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CouponSection(
    couponCode: String,
    onCouponChange: (String) -> Unit,
    couponApplied: Boolean,
    couponDiscount: Double,
    couponError: Boolean,
    onApply: () -> Unit,
    onRemoveCoupon: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (couponApplied) SuccessGreen.copy(alpha = 0.5f)
            else if (couponError) ErrorRed.copy(alpha = 0.5f)
            else CardBorder
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.LocalOffer, null,
                    tint = SignalOrange,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    "Cupom de Desconto",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Spacer(Modifier.height(10.dp))

            if (couponApplied) {
                // Cupom aplicado com sucesso
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SuccessGreen.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle, null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    couponCode,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SuccessGreen
                                )
                            }
                        }
                        Text(
                            "${(couponDiscount * 100).toInt()}% de desconto aplicado!",
                            fontSize = 12.sp,
                            color = SuccessGreen
                        )
                    }
                    IconButton(
                        onClick = onRemoveCoupon,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Close, "Remover cupom",
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Campo de cupom
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponCode,
                        onValueChange = onCouponChange,
                        placeholder = {
                            Text(
                                "Ex: JEFF10",
                                color = TextTertiary,
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MidnightBlueStart,
                            unfocusedContainerColor = MidnightBlueStart,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = if (couponError) ErrorRed else SignalOrange,
                            unfocusedBorderColor = if (couponError) ErrorRed else CardBorder
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            focusManager.clearFocus()
                            onApply()
                        }),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onApply()
                        },
                        enabled = couponCode.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SignalOrange,
                            disabledContainerColor = CardGradientStart
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(
                            "Aplicar",
                            fontWeight = FontWeight.Bold,
                            color = if (couponCode.isNotBlank()) MidnightBlueStart else TextTertiary,
                            fontSize = 13.sp
                        )
                    }
                }

                if (couponError) {
                    Spacer(Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Error, null,
                            tint = ErrorRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            "Cupom inválido ou expirado.",
                            fontSize = 11.sp,
                            color = ErrorRed
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Dica: use JEFF10 para 10% de desconto",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// RESUMO DO PEDIDO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun OrderSummaryCard(
    subtotal: Double,
    discountAmount: Double,
    couponApplied: Boolean,
    couponCode: String,
    couponPercent: Int,
    freeShipping: Boolean,
    total: Double,
    pixTotal: Double,
    installment: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Resumo do Pedido",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(14.dp))

            // Subtotal
            SummaryRow("Subtotal", subtotal.toCurrency(), TextSecondary, TextSecondary)

            // Desconto do cupom
            if (couponApplied && discountAmount > 0) {
                Spacer(Modifier.height(6.dp))
                SummaryRow(
                    label = "Cupom $couponCode (-$couponPercent%)",
                    value = "-${discountAmount.toCurrency()}",
                    labelColor = SuccessGreen,
                    valueColor = SuccessGreen
                )
            }

            // Frete
            Spacer(Modifier.height(6.dp))
            SummaryRow(
                label = "Frete",
                value = if (freeShipping) "Grátis 🎉" else "A calcular",
                labelColor = TextSecondary,
                valueColor = if (freeShipping) SuccessGreen else TextSecondary
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
            Spacer(Modifier.height(14.dp))

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Total",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    total.toCurrency(),
                    fontSize = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SignalOrange
                )
            }

            Spacer(Modifier.height(10.dp))

            // Opções de pagamento vantajosas
            Surface(
                color = SuccessGreen.copy(alpha = 0.10f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = SuccessGreen,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "PIX",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            "${pixTotal.toCurrency()} com 5% de desconto",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SuccessGreen
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.CreditCard, null,
                            tint = SatelliteBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            "ou 12x de ${installment.toCurrency()} sem juros",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, labelColor: Color, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = labelColor)
        Text(value, fontSize = 13.sp, color = valueColor, fontWeight = FontWeight.SemiBold)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SELOS DE CONFIANÇA DO CARRINHO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CartTrustBadges(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Shield, null, tint = SignalOrange, modifier = Modifier.size(16.dp))
                Text("Sua compra está protegida", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MiniTrustBadge("🔒", "Pagamento\nSeguro", Modifier.weight(1f))
                MiniTrustBadge("✅", "Produto\nOriginal", Modifier.weight(1f))
                MiniTrustBadge("↩", "Devolução\n7 dias", Modifier.weight(1f))
                MiniTrustBadge("💬", "Suporte\nWhatsApp", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniTrustBadge(icon: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 9.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ESTADO VAZIO DO CARRINHO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun EmptyCartContent(
    modifier: Modifier = Modifier,
    onGoToStore: () -> Unit
) {
    Column(
        modifier = modifier
            .background(brush = BackgroundGradient)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Ilustração do carrinho vazio
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(CardGradientStart),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.ShoppingCart,
                null,
                tint = TextTertiary.copy(alpha = 0.4f),
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Seu carrinho está vazio",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Explore nossa loja e encontre\nprodutos com a melhor qualidade",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = onGoToStore,
            colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp)
        ) {
            Icon(Icons.Default.Store, null, tint = MidnightBlueStart, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "Ir para a Loja",
                color = MidnightBlueStart,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(32.dp))

        // Benefícios rápidos
        Surface(
            color = CardGradientStart,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Por que comprar conosco?", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                EmptyStateFeature("🚚", "Frete grátis acima de R$ 100")
                EmptyStateFeature("✅", "Produtos originais e com garantia")
                EmptyStateFeature("💬", "Suporte via WhatsApp incluído")
                EmptyStateFeature("↩", "Devolução grátis em 7 dias")
            }
        }
    }
}

@Composable
private fun EmptyStateFeature(icon: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(icon, fontSize = 16.sp)
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// BARRA INFERIOR DE COMPRA
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun CartBottomBar(
    total: Double,
    pixTotal: Double,
    installment: Double,
    onCheckout: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Surface(
        color = MidnightBlueCard,
        shadowElevation = 24.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding()
        ) {
            // Linha de preços (total + pix)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total a pagar", fontSize = 11.sp, color = TextTertiary)
                    Text(
                        total.toCurrency(),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SignalOrange
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "PIX ${pixTotal.toCurrency()} (-5%)",
                        fontSize = 11.sp,
                        color = SuccessGreen,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "12x ${installment.toCurrency()} s/juros",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Botões
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão WhatsApp
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onWhatsApp),
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.5f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Message, "WhatsApp",
                            tint = SuccessGreen,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Botão finalizar compra
                Button(
                    onClick = onCheckout,
                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Default.ShoppingCartCheckout, null,
                        tint = MidnightBlueStart,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Finalizar Compra",
                        color = MidnightBlueStart,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
