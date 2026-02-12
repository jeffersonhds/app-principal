package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*

// ✅ BANNER DE PROMOÇÃO NO TOPO
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
        modifier = modifier
            .background(Color.Transparent)
            .padding(4.dp),
        color = SignalOrange.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 10.sp)
            Text(
                text = text,
                fontSize = 9.sp,
                color = SignalOrange,
                fontWeight = FontWeight.SemiBold
            )
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
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
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

// ✅ STATS DA LOJA (Produtos totais, categorias, etc)
@Composable
fun StoreStats(
    totalProducts: Int,
    totalCategories: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            number = totalProducts.toString(),
            label = "Produtos",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = totalCategories.toString(),
            label = "Categorias",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = "100%",
            label = "Originais",
            modifier = Modifier.weight(1f)
        )
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
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignalOrange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ✅ AVISO DE FRETE GRÁTIS
@Composable
fun FreeShippingBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SuccessGreen.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Frete Grátis",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "Acima de R$ 100",
                    fontSize = 11.sp,
                    color = SuccessGreen.copy(alpha = 0.8f)
                )
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
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp),
            containerColor = SignalOrange,
            contentColor = MidnightBlueStart,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Voltar ao topo",
                modifier = Modifier.size(24.dp)
            )
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
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SignalOrange.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = SignalOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$filterCount filtro${if (filterCount > 1) "s" else ""} ativo${if (filterCount > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = SignalOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.height(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpar",
                    tint = SignalOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Limpar",
                    fontSize = 10.sp,
                    color = SignalOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}