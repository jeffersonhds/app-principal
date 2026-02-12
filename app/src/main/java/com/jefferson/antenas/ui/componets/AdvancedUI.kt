package com.jefferson.antenas.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.theme.* // Certifique-se que suas cores estão importadas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modelos visuais simples
data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val actionText: String
)

data class ReviewItem(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

// --- 1. Carrossel Principal (Hero) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    banners: List<BannerItem>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll
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
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )))

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(banner.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(banner.subtitle, fontSize = 13.sp, color = Color.LightGray)

                    Surface(
                        color = Color(0xFFF59E0B), // Laranja
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            banner.actionText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF0F172A), // Azul Escuro
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Indicadores (Bolinhas)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(if (pagerState.currentPage == index) Color(0xFFF59E0B) else Color.Gray, CircleShape)
                )
            }
        }
    }
}

// --- 2. Selos de Confiança ---
@Composable
fun TrustBadges(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BadgeItem(Icons.Default.VerifiedUser, "Garantia", "1 Ano")
        BadgeItem(Icons.Default.LocalShipping, "Entrega", "Rápida")
        BadgeItem(Icons.Default.Lock, "Seguro", "100%")
        BadgeItem(Icons.Default.HeadsetMic, "Suporte", "24h")
    }
}

@Composable
fun BadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        Text(sub, fontSize = 10.sp, color = Color.Gray)
    }
}

// --- 3. Carrossel de Avaliações ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewsCarousel(
    reviews: List<ReviewItem>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { reviews.size })

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            "O que dizem nossos clientes",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val review = reviews[page]
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Azul Médio
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(review.customerName, fontWeight = FontWeight.Bold, color = Color.White)
                        Row {
                            repeat(5) { i ->
                                Icon(
                                    if (i < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i < review.rating) Color(0xFFF59E0B) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        review.comment,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3
                    )
                }
            }
        }
    }
}