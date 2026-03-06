package com.jefferson.antenas.ui.screens.home

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.jefferson.antenas.utils.WHATSAPP_PHONE
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
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.componets.*
import com.jefferson.antenas.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.util.Calendar
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import kotlin.math.roundToInt

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL — Porta de entrada focada em serviços
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onServicesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onStoreClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val cartCount by viewModel.cartItemCount.collectAsState()
    val context = LocalContext.current

    // ── Banners focados em serviços (sem menção a produtos/promoções) ─────────
    val banners = listOf(
        BannerItem("1", "Instalação Profissional", "Apontamento, manutenção e configuração",
            "", "Agendar pelo WhatsApp", "📡"),
        BannerItem("2", "Manutenção & Reparo", "Diagnóstico rápido no mesmo dia",
            "", "Solicitar Visita", "🔧"),
        BannerItem("3", "Configuração de Receptores", "SKY, Claro TV+ e similares",
            "", "Agendar pelo WhatsApp", "📺"),
        BannerItem("4", "Internet via Satélite", "Starlink e outras soluções",
            "", "Saiba Mais", "🌐")
    )

    // ── Avaliações — focadas em serviço prestado ──────────────────────────────
    val reviews = listOf(
        ReviewItem("1", "Carlos Silva",  5, "O técnico foi muito atencioso na instalação. Serviço impecável.", "Jan 2025"),
        ReviewItem("2", "Ana Souza",     5, "Resolveu o problema no mesmo dia. Atendimento excelente!", "Dez 2024"),
        ReviewItem("3", "Roberto Lima",  5, "Pontual, profissional e fez um serviço perfeito. Recomendo!", "Nov 2024"),
        ReviewItem("4", "Mariana Costa", 4, "Bom atendimento via WhatsApp e serviço funcionando perfeitamente.", "Out 2024")
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

                // ── Faixa de urgência (atendimento, não frete) ───────────────
                ServiceUrgencyStrip()

                // ── Carousel de banners — serviços ───────────────────────────
                HeroCarouselModernized(
                    banners = banners,
                    onButtonClick = { bannerId ->
                        // Todos os slides abrem WhatsApp ou Serviços
                        when (bannerId) {
                            "4" -> onServicesClick()
                            else -> {
                                val phone = WHATSAPP_PHONE
                                val msg = when (bannerId) {
                                    "1" -> "Olá Jefferson! Vim pelo aplicativo e gostaria de agendar uma instalação."
                                    "2" -> "Olá Jefferson! Vim pelo aplicativo e preciso de manutenção/reparo."
                                    "3" -> "Olá Jefferson! Vim pelo aplicativo e preciso configurar meu receptor."
                                    else -> "Olá Jefferson! Vim pelo aplicativo e gostaria de mais informações."
                                }
                                try {
                                    val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                        URLEncoder.encode(msg, "UTF-8")
                                    }"
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (_: Exception) {
                                    Toast.makeText(context, "WhatsApp não encontrado. Instale o app e tente novamente.", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                )

                // ── Card principal de serviços (CTA interativo) ──────────────
                ServiceCallToActionCard_Interactive(onClick = onServicesClick)

                // ── Prova social (números da empresa) ────────────────────────
                HomeSocialProof()

                // ── Avaliações dos clientes ──────────────────────────────────
                ImprovedReviewsSection(reviews = reviews)

                // ── WhatsApp CTA ─────────────────────────────────────────────
                WhatsAppCtaSection(context = context)

                // ── Gancho discreto da Loja ──────────────────────────────────
                StoreGanchoCard(onClick = onStoreClick)

                Spacer(Modifier.height(90.dp))
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// TOP BAR (sem alteração)
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
                    val greeting = remember {
                        when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
                            in 0..11 -> "Bom dia! ☀️"
                            in 12..17 -> "Boa tarde! 🌤️"
                            else -> "Boa noite! 🌙"
                        }
                    }
                    Text(
                        greeting,
                        color = SignalOrange.copy(alpha = 0.85f),
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
            CartAppBarAction(cartCount = cartCount, onCartClick = onCartClick)
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.Person, "Perfil", tint = TextSecondary)
            }
        }
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// BARRA DE BUSCA CLICÁVEL (sem alteração)
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
// FAIXA DE URGÊNCIA — focada em serviços (substituiu a de frete)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ServiceUrgencyStrip() {
    val infiniteTransition = rememberInfiniteTransition(label = "urgency")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.07f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "urgency_pulse"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, SignalOrange.copy(alpha = pulseAlpha), Color.Transparent)
                )
            )
            .padding(vertical = 7.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚡", fontSize = 12.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                "Atendimento em até 24h",
                color = SignalOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text("  •  ", color = TextTertiary, fontSize = 12.sp)
            Text(
                "Sapezal — MT e região",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// GANCHO DISCRETO DA LOJA (novo — substitui toda a vitrine de produtos)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun StoreGanchoCard(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Surface(
                color = SignalOrange.copy(alpha = 0.12f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingBag, null,
                    tint = SignalOrange,
                    modifier = Modifier
                        .size(44.dp)
                        .padding(10.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Loja de Equipamentos",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    "Antenas, receptores, cabos e acessórios",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward, null,
                tint = TextTertiary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CAROUSEL DE BANNERS (sem alteração no componente, só nos dados acima)
// ══════════════════════════════════════════════════════════════════════════════

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarouselModernized(
    banners: List<BannerItem>,
    modifier: Modifier = Modifier,
    onButtonClick: (String) -> Unit = {}
) {
    if (banners.isEmpty()) return

    val bannerGradients = listOf(
        listOf(Color(0xFFB45309), Color(0xFF78350F), Color(0xFF1C1917)),
        listOf(Color(0xFF1D4ED8), Color(0xFF1E3A8A), Color(0xFF0F172A)),
        listOf(Color(0xFF047857), Color(0xFF064E3B), Color(0xFF0F172A)),
        listOf(Color(0xFFBE123C), Color(0xFF9D174D), Color(0xFF0F172A))
    )

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(5000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % banners.size)
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
                val gradColors = bannerGradients.getOrElse(page) { bannerGradients[0] }

                Box(modifier = Modifier.fillMaxSize()) {

                    if (banner.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = banner.imageUrl,
                            contentDescription = banner.title,
                            modifier = Modifier.fillMaxSize(),
                        )
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
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Brush.linearGradient(gradColors))
                        )
                        Text(
                            banner.icon,
                            fontSize = 110.sp,
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(end = 8.dp)
                                .alpha(0.20f)
                        )
                    }

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
                        Spacer(Modifier.height(10.dp))
                        Surface(
                            color = SignalOrange,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.clickable { onButtonClick(banner.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    banner.buttonText,
                                    color = MidnightBlueStart,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForward, null,
                                    tint = MidnightBlueStart,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

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
// PROVA SOCIAL (sem alteração)
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
                SocialProofStat("100%", "Satisfação\nGarantida")
            }
        }
    }
}

@Composable
private fun SocialProofStat(value: String, label: String) {
    val numStr = value.filter { it.isDigit() || it == '.' }
    val suffix = value.filter { !it.isDigit() && it != '.' }
    val target = numStr.toFloatOrNull() ?: 0f
    val isDecimal = numStr.contains('.')

    var animVal by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        val steps = 40
        for (i in 1..steps) {
            delay(30L)
            animVal = target * i / steps.toFloat()
        }
        animVal = target
    }

    val display = if (isDecimal) "${String.format("%.1f", animVal)}$suffix"
    else "${animVal.roundToInt()}$suffix"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(display, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = SignalOrange)
        Text(label, fontSize = 9.sp, color = TextTertiary, textAlign = TextAlign.Center, lineHeight = 12.sp)
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// AVALIAÇÕES DOS CLIENTES (sem alteração)
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ImprovedReviewsSection(reviews: List<ReviewItem>) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
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
                    repeat(5) {
                        Icon(Icons.Default.Star, null, tint = Color(0xFFFFC107), modifier = Modifier.size(13.dp))
                    }
                    Text(" 4.9 · ${reviews.size} avaliações", fontSize = 11.sp, color = TextSecondary)
                }
            }
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(reviews, key = { it.id }) { review ->
                ReviewCard(review)
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItem) {
    Surface(
        modifier = Modifier.width(260.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    color = SignalOrange.copy(alpha = 0.15f),
                    shape = CircleShape
                ) {
                    Text(
                        review.author.first().toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalOrange,
                        modifier = Modifier
                            .size(36.dp)
                            .padding(top = 9.dp),
                        textAlign = TextAlign.Center
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
                Spacer(Modifier.weight(1f))
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
                    Text("Serviço Verificado", fontSize = 9.sp, color = SuccessGreen)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SERVIÇOS CTA INTERATIVO (sem alteração)
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
// WHATSAPP CTA (sem alteração)
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
                    Icons.AutoMirrored.Filled.Message, null,
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
                        val phone = WHATSAPP_PHONE
                        val msg = "Olá Jefferson! Vim pelo aplicativo e gostaria de mais informações."
                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                URLEncoder.encode(msg, "UTF-8")
                            }"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {
                            Toast.makeText(context, "WhatsApp não encontrado. Instale o app e tente novamente.", Toast.LENGTH_LONG).show()
                        }
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
