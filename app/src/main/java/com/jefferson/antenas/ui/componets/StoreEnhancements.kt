package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*
import kotlinx.coroutines.delay

// ✅ BANNER DE PROMOÇÃO (mantido para compatibilidade)
@Composable
fun PromotionBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SignalOrange,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎉 $text",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightBlueStart
            )
        }
    }
}

// ✅ CARD COM INFO DE FRETE/PROMOÇÃO
@Composable
fun ProductBenefitBadge(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.background(Color.Transparent).padding(4.dp),
        color = SignalOrange.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 10.sp)
            Text(text, fontSize = 9.sp, color = SignalOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ✅ CHIP DE FILTRO COM ANIMAÇÃO
@Composable
fun AnimatedFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(label, fontSize = 12.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal)
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SignalOrange,
            selectedLabelColor = MidnightBlueStart,
            containerColor = CardGradientStart,
            labelColor = TextSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// ✅ STATS DA LOJA (mantido para compatibilidade)
@Composable
fun StoreStats(
    totalProducts: Int,
    totalCategories: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(number = totalProducts.toString(), label = "Produtos", modifier = Modifier.weight(1f))
        StatCard(number = totalCategories.toString(), label = "Categorias", modifier = Modifier.weight(1f))
        StatCard(number = "100%", label = "Originais", modifier = Modifier.weight(1f))
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(number, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = SignalOrange)
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
        }
    }
}

// ✅ AVISO DE FRETE GRÁTIS (mantido para compatibilidade)
@Composable
fun FreeShippingBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        color = SuccessGreen.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.LocalShipping, null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
            Column {
                Text("Frete Grátis", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SuccessGreen)
                Text("Acima de R$ 100", fontSize = 11.sp, color = SuccessGreen.copy(alpha = 0.8f))
            }
        }
    }
}

// ✅ BOTÃO "VOLTAR AO TOPO" (flutuante)
@Composable
fun ScrollToTopButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)),
        exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.padding(16.dp).size(48.dp),
            containerColor = SignalOrange,
            contentColor = MidnightBlueStart,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Voltar ao topo", modifier = Modifier.size(24.dp))
        }
    }
}

// ✅ FILTRO ATIVO INDICATOR
@Composable
fun ActiveFiltersIndicator(
    hasActiveFilters: Boolean,
    filterCount: Int,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasActiveFilters) return

    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        color = SignalOrange.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.FilterList, null, tint = SignalOrange, modifier = Modifier.size(16.dp))
                Text(
                    "$filterCount filtro${if (filterCount > 1) "s" else ""} ativo${if (filterCount > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = SignalOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }
            TextButton(onClick = onClearFilters, modifier = Modifier.height(24.dp)) {
                Icon(Icons.Default.Close, "Limpar", tint = SignalOrange, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Limpar", fontSize = 10.sp, color = SignalOrange, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NOVOS COMPONENTES — estilo marketplace profissional
// ─────────────────────────────────────────────────────────────────────────────

// ✅ CARROSSEL DE PROMOÇÕES COM AUTO-SCROLL
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PromoCarousel(modifier: Modifier = Modifier) {
    data class PromoSlide(
        val title: String,
        val subtitle: String,
        val startColor: Color,
        val endColor: Color
    )

    val slides = listOf(
        PromoSlide("Até 25% OFF 🎉", "Em produtos selecionados", SignalOrange, Color(0xFFBF360C)),
        PromoSlide("Frete Grátis 🚚", "Em compras acima de R$ 100", Color(0xFF2E7D32), SuccessGreen),
        PromoSlide("Novos Lançamentos ✨", "Confira as últimas novidades", SatelliteBlue, Color(0xFF0D47A1))
    )

    val pagerState = rememberPagerState(pageCount = { slides.size })

    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            pagerState.animateScrollToPage((pagerState.currentPage + 1) % slides.size)
        }
    }

    Column(modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val slide = slides[page]
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(listOf(slide.startColor, slide.endColor))
                        )
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column {
                        Text(
                            slide.title,
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            slide.subtitle,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Indicadores estilo "pílula"
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(slides.size) { i ->
                    val isSelected = pagerState.currentPage == i
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isSelected) 20.dp else 6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) Color.White else Color.White.copy(alpha = 0.4f)
                            )
                    )
                }
            }
        }
    }
}

// ✅ LINHA DE BENEFÍCIOS (3 chips compactos)
@Composable
fun BenefitsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BenefitChip("🚚", "Frete Grátis\nacima de R$100", Modifier.weight(1f))
        BenefitChip("✅", "Produtos\nOriginais", Modifier.weight(1f))
        BenefitChip("💬", "Suporte\nWhatsApp", Modifier.weight(1f))
    }
}

@Composable
fun BenefitChip(icon: String, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = CardGradientStart,
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 9.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

// ✅ LINK PARA SERVIÇOS — card compacto e discreto
@Composable
fun ServicesLinkCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        color = SatelliteBlue.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Build, null, tint = SignalOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Instalação & Serviços",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    "Apontamento, manutenção e configuração",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(Icons.Default.ArrowForward, null, tint = SignalOrange, modifier = Modifier.size(16.dp))
        }
    }
}