package com.jefferson.antenas.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.ui.componets.*
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Calendar

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onServicesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val context = LocalContext.current

    var showToast by remember { mutableStateOf(false) }
    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    // Produtos filtrados por seção
    val saleProducts = remember(products) { products.filter { (it.discount ?: 0) > 0 } }
    val newProducts = remember(products) { products.filter { it.isNew == true } }

    // Banners do carousel
    val banners = listOf(
        BannerItem("1", "Antenas 4K Ultra HD", "Qualidade de imagem cristalina",
            "https://images.unsplash.com/photo-1518611505868-48510c2e1fb4?w=800&h=400&fit=crop", "Ver Modelos"),
        BannerItem("2", "Instalação Profissional", "Técnicos qualificados e experientes",
            "https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=800&h=400&fit=crop", "Agendar"),
        BannerItem("3", "Até 25% de Desconto", "Aproveite as promoções exclusivas",
            "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&h=400&fit=crop", "Aproveitar"),
        BannerItem("4", "Receptores Inteligentes", "Controle total da sua TV",
            "https://images.unsplash.com/photo-1611532736579-6b16e2b50449?w=800&h=400&fit=crop", "Conhecer")
    )

    // Avaliações
    val reviews = listOf(
        ReviewItem("1", "Carlos Silva",  5, "Entrega super rápida e produto original. Recomendo muito!", "Jan 2025"),
        ReviewItem("2", "Ana Souza",     5, "O técnico foi muito atencioso na instalação. Serviço impecável.", "Dez 2024"),
        ReviewItem("3", "Roberto Lima",  5, "Produto de qualidade, chegou bem embalado. Ótima loja!", "Nov 2024"),
        ReviewItem("4", "Mariana Costa", 4, "Bom atendimento via WhatsApp. Produto funcionando perfeitamente.", "Out 2024")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MidnightBlueStart,
            topBar = {
                HomeTopBar(
                    cartCount = cartCount,
                    onCartClick = onCartClick,
                    onProfileClick = onProfileClick,
                    onSearchClick = onSearchClick
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(brush = BackgroundGradient)
                    .verticalScroll(rememberScrollState())
            ) {

                // ── Barra de busca clicável ──────────────────────────────────
                HomeSearchBar(onClick = onSearchClick)

                // ── Carousel de banners ──────────────────────────────────────
                HeroCarouselModernized(banners = banners)

                // ── Categorias rápidas ───────────────────────────────────────
                HomeCategoriesRow(
                    onCategoryClick = { onSearchClick() }
                )

                // ── Benefícios (4 badges) ────────────────────────────────────
                TrustBadgesModernized()

                // ── Oferta do Dia (Countdown + produtos) ─────────────────────
                if (!isLoading && saleProducts.isNotEmpty()) {
                    FlashSaleSection(
                        products = saleProducts,
                        onProductClick = onProductClick,
                        onAddToCart = {
                            viewModel.addToCart(it)
                            showToast = true
                        }
                    )
                }

                // ── Serviços CTA ─────────────────────────────────────────────
                ServiceCallToActionCard_Interactive(onClick = onServicesClick)

                // ── Destaques ────────────────────────────────────────────────
                HomeSectionHeader(
                    title = "🔥 Mais Vendidos",
                    subtitle = "Produtos favoritos dos nossos clientes",
                    onSeeAll = onSearchClick
                )

                when {
                    isLoading -> HomeProductsShimmer()
                    errorMessage != null -> HomeErrorState(
                        message = errorMessage!!,
                        onRetry = { viewModel.retry() }
                    )
                    else -> HomeProductsRow(
                        products = products.take(8),
                        onProductClick = onProductClick,
                        onAddToCart = {
                            viewModel.addToCart(it)
                            showToast = true
                        }
                    )
                }

                // ── Novidades ────────────────────────────────────────────────
                if (!isLoading && newProducts.isNotEmpty()) {
                    HomeSectionHeader(
                        title = "✨ Novidades",
                        subtitle = "Acabaram de chegar",
                        onSeeAll = onSearchClick
                    )
                    HomeProductsRow(
                        products = newProducts,
                        onProductClick = onProductClick,
                        onAddToCart = {
                            viewModel.addToCart(it)
                            showToast = true
                        }
                    )
                }

                // ── Stats / Prova social ─────────────────────────────────────
                HomeSocialProof()

                // ── Avaliações dos clientes ──────────────────────────────────
                ImprovedReviewsSection(reviews = reviews)

                // ── WhatsApp CTA ─────────────────────────────────────────────
                WhatsAppCtaSection(context = context)

                Spacer(Modifier.height(90.dp))
            }
        }

        ModernSuccessToast(
            visible = showToast,
            message = "✓ Adicionado ao carrinho!",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TOP BAR
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    cartCount: Int,
    onCartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Logo "JA" em círculo laranja
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(SignalOrange, SignalOrangeDark))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("J", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = MidnightBlueStart)
                }
                Column {
                    Text(
                        "Jefferson Antenas",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        lineHeight = 16.sp
                    )
                    Text(
                        "Sua loja de confiança",
                        color = TextTertiary,
                        fontSize = 10.sp,
                        lineHeight = 12.sp
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlueStart),
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, "Buscar", tint = TextSecondary)
            }
            // Carrinho com badge
            CartAppBarAction(cartCount = cartCount, onCartClick = onCartClick)
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.Person, "Perfil", tint = TextSecondary)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// BARRA DE BUSCA CLICÁVEL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeSearchBar(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable(onClick = onClick),
        color = CardGradientStart,
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.Search, null, tint = SignalOrange, modifier = Modifier.size(20.dp))
            Text(
                "Buscar antenas, receptores, cabos...",
                color = TextTertiary,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            Surface(
                color = SignalOrange.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    Icons.Default.Tune, null,
                    tint = SignalOrange,
                    modifier = Modifier
                        .size(30.dp)
                        .padding(6.dp)
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CAROUSEL DE BANNERS (melhorado)
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarouselModernized(banners: List<BannerItem>, modifier: Modifier = Modifier) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            coroutineScope.launch {
                pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
            }
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val banner = banners[page]
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradiente inferior
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 80f
                                )
                            )
                    )
                    // Conteúdo do banner
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(20.dp)
                    ) {
                        Text(
                            banner.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 24.sp
                        )
                        Text(
                            banner.subtitle,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            color = SignalOrange,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                banner.buttonText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                color = MidnightBlueStart,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Indicadores pill
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(banners.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(if (isSelected) 18.dp else 5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (isSelected) SignalOrange else Color.White.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CATEGORIAS RÁPIDAS
// ══════════════════════════════════════════════════════════════════════════════

private data class HomeCategory(val icon: String, val label: String)

@Composable
private fun HomeCategoriesRow(onCategoryClick: (String) -> Unit) {
    val categories = listOf(
        HomeCategory("📡", "Antenas"),
        HomeCategory("📺", "Receptores"),
        HomeCategory("🔌", "Cabos"),
        HomeCategory("🔧", "Instalação"),
        HomeCategory("💰", "Promoções"),
        HomeCategory("🆕", "Novidades")
    )

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "Categorias",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories) { cat ->
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onCategoryClick(cat.label) }
                        .width(68.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = CardGradientStart,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(cat.icon, fontSize = 24.sp)
                        }
                    }
                    Text(
                        cat.label,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TRUST BADGES (4 cartões)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun TrustBadgesModernized(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BadgeItemModernized(Icons.Default.VerifiedUser, "Garantia",     "12 Meses", Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.LocalShipping, "Entrega",     "Rápida",   Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.Lock,          "Pagamento",   "Seguro",   Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.HeadsetMic,    "Suporte",     "WhatsApp", Modifier.weight(1f))
    }
}

@Composable
fun BadgeItemModernized(
    icon: ImageVector,
    title: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(90.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                color = SignalOrange.copy(alpha = 0.14f),
                shape = CircleShape
            ) {
                Icon(
                    icon, null,
                    tint = SignalOrange,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(7.dp)
                )
            }
            Spacer(Modifier.height(5.dp))
            Text(title, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(sub, fontSize = 9.sp, color = SignalOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SEÇÃO OFERTA DO DIA (FLASH SALE + COUNTDOWN)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun FlashSaleSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    // Countdown até meia-noite
    var secondsLeft by remember {
        mutableIntStateOf(
            run {
                val cal = Calendar.getInstance()
                val now = cal.timeInMillis
                cal.set(Calendar.HOUR_OF_DAY, 23)
                cal.set(Calendar.MINUTE, 59)
                cal.set(Calendar.SECOND, 59)
                ((cal.timeInMillis - now) / 1000).toInt().coerceAtLeast(0)
            }
        )
    }

    LaunchedEffect(Unit) {
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
    }

    val hours   = secondsLeft / 3600
    val minutes = (secondsLeft % 3600) / 60
    val seconds = secondsLeft % 60

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // Header laranja com countdown
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚡", fontSize = 18.sp)
                Column {
                    Text(
                        "Oferta do Dia",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                    Text(
                        "Preços válidos por tempo limitado",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }

            // Countdown
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                CountdownBox(String.format("%02d", hours))
                Text(":", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SignalOrange)
                CountdownBox(String.format("%02d", minutes))
                Text(":", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = SignalOrange)
                CountdownBox(String.format("%02d", seconds))
            }
        }

        Spacer(Modifier.height(8.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(products.take(6)) { product ->
                HomeProductCard(
                    product = product,
                    onClick = { onProductClick(product.id) },
                    onAddToCart = { onAddToCart(product) }
                )
            }
        }
    }
}

@Composable
private fun CountdownBox(value: String) {
    Surface(
        color = ErrorRed,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            value,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CARD DE PRODUTO HORIZONTAL (compacto)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val discountedPrice = product.getDiscountedPrice()
    val hasDiscount = (product.discount ?: 0) > 0

    Surface(
        modifier = Modifier
            .width(155.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 3.dp
    ) {
        Column {
            // Imagem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(Color.White)
            ) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
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
                            Icon(Icons.Default.Image, null, tint = Color.Gray.copy(0.4f), modifier = Modifier.size(32.dp))
                        }
                    }
                )

                // Badge de desconto
                if (hasDiscount) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopStart),
                        color = ErrorRed,
                        shape = RoundedCornerShape(topStart = 14.dp, bottomEnd = 8.dp)
                    ) {
                        Text(
                            "-${product.discount}%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
                // Badge NOVO
                if (product.isNew == true) {
                    Surface(
                        modifier = Modifier.align(Alignment.TopEnd),
                        color = SignalOrange,
                        shape = RoundedCornerShape(topEnd = 14.dp, bottomStart = 8.dp)
                    ) {
                        Text(
                            "NOVO",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlueStart,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                if (hasDiscount) {
                    Text(
                        product.price,
                        fontSize = 10.sp,
                        color = TextTertiary,
                        textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough
                    )
                }
                Text(
                    discountedPrice.toCurrency(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SignalOrange
                )
                Spacer(Modifier.height(6.dp))
                // Botão adicionar
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddToCart),
                    color = SignalOrange,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart, null,
                            tint = MidnightBlueStart,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Adicionar",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightBlueStart
                        )
                    }
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SEÇÃO HEADER COM "VER TODOS"
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeSectionHeader(
    title: String,
    subtitle: String,
    onSeeAll: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextSecondary)
        }
        Surface(
            color = SignalOrange.copy(alpha = 0.12f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.clickable(onClick = onSeeAll)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Ver todos", fontSize = 11.sp, color = SignalOrange, fontWeight = FontWeight.SemiBold)
                Icon(Icons.Default.ChevronRight, null, tint = SignalOrange, modifier = Modifier.size(14.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// LINHA HORIZONTAL DE PRODUTOS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeProductsRow(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(products) { product ->
            HomeProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                onAddToCart = { onAddToCart(product) }
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SHIMMER DE CARREGAMENTO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeProductsShimmer() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(4) {
            ShimmerProductCard(modifier = Modifier.width(155.dp))
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ESTADO DE ERRO
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeErrorState(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, ErrorRed.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Default.WifiOff, null, tint = TextSecondary, modifier = Modifier.size(42.dp))
            Text(message, color = TextSecondary, fontSize = 13.sp, textAlign = TextAlign.Center)
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Refresh, null, tint = MidnightBlueStart, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Tentar novamente", color = MidnightBlueStart, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// PROVA SOCIAL (números da loja)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun HomeSocialProof() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            Brush.horizontalGradient(listOf(SignalOrange.copy(0.4f), SatelliteBlue.copy(0.4f)))
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 14.dp)
            ) {
                Text("🏆", fontSize = 18.sp)
                Text(
                    "Por que a Jefferson Antenas?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SocialProofStat("500+", "Clientes\nAtendidos")
                Box(modifier = Modifier.width(0.5.dp).height(44.dp).background(CardBorder))
                SocialProofStat("4.9★", "Avaliação\nMédia")
                Box(modifier = Modifier.width(0.5.dp).height(44.dp).background(CardBorder))
                SocialProofStat("5+", "Anos de\nExperiência")
                Box(modifier = Modifier.width(0.5.dp).height(44.dp).background(CardBorder))
                SocialProofStat("100%", "Produtos\nOriginais")
            }
        }
    }
}

@Composable
private fun SocialProofStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SignalOrange)
        Text(label, fontSize = 9.sp, color = TextTertiary, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AVALIAÇÕES DOS CLIENTES (melhorado)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ImprovedReviewsSection(reviews: List<ReviewItem>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("⭐ O que nossos clientes dizem", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("4.9 de 5", fontSize = 12.sp, color = SignalOrange, fontWeight = FontWeight.Bold)
                    Text("•", fontSize = 12.sp, color = TextTertiary)
                    Text("${reviews.size * 30}+ avaliações", fontSize = 12.sp, color = TextSecondary)
                }
            }
            Surface(
                color = Color(0xFFFFC107).copy(alpha = 0.14f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reviews) { review ->
                ImprovedReviewCard(review = review)
            }
        }
    }
}

@Composable
private fun ImprovedReviewCard(review: ReviewItem) {
    Surface(
        modifier = Modifier.width(260.dp),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(SatelliteBlue.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.author.first().toString(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = SatelliteBlue
                        )
                    }
                    Column {
                        Text(review.author, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Row {
                            repeat(review.rating) {
                                Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(11.dp))
                            }
                            repeat(5 - review.rating) {
                                Icon(Icons.Default.Star, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                }
                Surface(
                    color = SuccessGreen.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        review.date,
                        fontSize = 9.sp,
                        color = TextTertiary,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                review.text,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 18.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                color = SuccessGreen.copy(alpha = 0.10f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = SuccessGreen, modifier = Modifier.size(10.dp))
                    Text("Compra Verificada", fontSize = 9.sp, color = SuccessGreen)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SERVIÇOS CTA (mantido com pequena melhora)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ServiceCallToActionCard_Interactive(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    val cardElevation by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 6.dp,
        label = "CardElevation"
    )
    val iconSize by animateDpAsState(
        targetValue = if (isPressed) 38.dp else 36.dp,
        label = "IconSize"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isPressed = true
                onClick()
            }
            .shadow(elevation = cardElevation, shape = RoundedCornerShape(16.dp), clip = true),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(SignalOrange.copy(alpha = 0.9f), SignalOrangeDark.copy(alpha = 0.9f))
                    )
                )
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = MidnightBlueStart
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Build, null,
                            tint = SignalOrange,
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Serviços de Instalação", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(Modifier.height(2.dp))
                    Text("Apontamento, manutenção e configuração", fontSize = 12.sp, color = Color.White.copy(0.85f))
                    Spacer(Modifier.height(4.dp))
                    Text("Peça um orçamento gratuito →", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// WHATSAPP CTA (rodapé)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun WhatsAppCtaSection(context: android.content.Context) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SuccessGreen.copy(alpha = 0.10f),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = SuccessGreen.copy(alpha = 0.15f),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.Message, null,
                    tint = SuccessGreen,
                    modifier = Modifier
                        .size(50.dp)
                        .padding(12.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Dúvidas? Fale conosco!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Atendimento rápido via WhatsApp",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = SuccessGreen,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        val phone = "5565992895296"
                        val msg = "Olá Jefferson! Vim pelo aplicativo e gostaria de mais informações."
                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                URLEncoder.encode(msg, "UTF-8")
                            }"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    }
                ) {
                    Text(
                        "Iniciar Conversa",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
