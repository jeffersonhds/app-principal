package com.jefferson.antenas.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.componets.*
import com.jefferson.antenas.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    // ✅ BANNERS MODERNIZADOS - Relevantes para Antenas
    val banners: List<BannerItem> = listOf(
        BannerItem("1", "Antenas 4K Ultra HD", "Qualidade de imagem cristalina", "https://images.unsplash.com/photo-1518611505868-48510c2e1fb4?w=800&h=400&fit=crop", "Ver Modelos"),
        BannerItem("2", "Instalação Profissional", "Técnicos qualificados e experientes", "https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=800&h=400&fit=crop", "Agendar"),
        BannerItem("3", "Promoção 2026", "Desconto especial de até 25%", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&h=400&fit=crop", "Aproveitar"),
        BannerItem("4", "Receptores Inteligentes", "Controle total da sua TV", "https://images.unsplash.com/photo-1611532736579-6b16e2b50449?w=800&h=400&fit=crop", "Conhecer")
    )

    val reviews: List<ReviewItem> = listOf(
        ReviewItem("1", "Carlos Silva", 5, "Entrega super rápida!", "20/12"),
        ReviewItem("2", "Ana Souza", 5, "O técnico foi muito atencioso.", "18/12"),
        ReviewItem("3", "Roberto Lima", 4, "Produto original.", "15/12")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MidnightBlueStart,
            topBar = {
                TopAppBar(
                    title = { Text("Jefferson Antenas", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlueStart),
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar Produtos", tint = TextPrimary)
                        }
                        CartAppBarAction(cartCount = cartCount, onCartClick = onCartClick)
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ✅ BANNER COM AUTO-SCROLL
                HeroCarouselModernized(banners = banners)

                // ✅ BADGES MODERNIZADAS
                TrustBadgesModernized()

                // ✅ CARD DE SERVIÇOS COM ÍCONE ESCURO
                ServiceCallToActionCard_Interactive(onClick = onServicesClick)

                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Destaques", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                if (products.isEmpty()) {
                    repeat(4) {
                        ShimmerProductCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else {
                    products.take(4).forEach { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = {
                                viewModel.addToCart(it)
                                showToast = true
                            },
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { onCartClick() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SignalOrange)
                    ) { Text("Ver Loja Completa") }
                }
                ReviewsCarousel(reviews = reviews)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        ModernSuccessToast(
            visible = showToast,
            message = "Item adicionado!",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ✅ BANNER COM AUTO-SCROLL AUTOMÁTICO
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarouselModernized(banners: List<BannerItem>, modifier: Modifier = Modifier) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // ✅ Auto-scroll automático a cada 5 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(220.dp).padding(16.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
        ) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente para leitura
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(banner.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(banner.subtitle, fontSize = 13.sp, color = Color.LightGray)

                    Surface(
                        color = SignalOrange,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            banner.buttonText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MidnightBlueStart,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ✅ Indicadores (Bolinhas) com estilo melhorado
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(if (pagerState.currentPage == index) SignalOrange else Color.Gray, CircleShape)
                )
            }
        }
    }
}

// ✅ BADGES MODERNIZADAS - 4 ITENS EM UMA LINHA
@Composable
fun TrustBadgesModernized(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeItemModernized(Icons.Default.VerifiedUser, "Garantia", "3 Meses", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.LocalShipping, "Entrega", "Rápida", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.Lock, "Seguro", "100%", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.HeadsetMic, "Suporte", "24h", modifier = Modifier.weight(1f))
    }
}

@Composable
fun BadgeItemModernized(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = SignalOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(sub, fontSize = 10.sp, color = SignalOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ✅ CARD DE SERVIÇOS COM ÍCONE ESCURO
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isPressed = true
                onClick()
            }
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SignalOrange.copy(alpha = 0.85f),
                            SignalOrangeDark.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ ÍCONE COM COR DO FUNDO DO APP
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = MidnightBlueStart  // ✅ Cor do fundo do app
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = SignalOrange,  // Ícone em laranja para contraste
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Serviços de Instalação",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Peça um orçamento gratuito",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}