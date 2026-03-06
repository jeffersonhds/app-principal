package com.jefferson.antenas.ui.componets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.jefferson.antenas.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAppBarAction(
    cartCount: Int,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DA ANIMAÇÃO ---
    var previousCartCount by remember { mutableStateOf(cartCount) }
    var iconScale by remember { mutableStateOf(1f) }

    // Anima a escala do ícone suavemente
    val animatedScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = tween(durationMillis = 300),
        label = "CartIconScale"
    )

    // Efeito que dispara a animação quando um item é adicionado
    LaunchedEffect(cartCount) {
        if (cartCount > previousCartCount) {
            // Dispara a animação de "pulo"
            iconScale = 1.3f
            // Agenda a volta ao estado normal
            kotlinx.coroutines.delay(150) // Metade da duração da animação
            iconScale = 1f
        }
        // Atualiza a contagem anterior
        previousCartCount = cartCount
    }

    BadgedBox(
        modifier = modifier,
        badge = {
            if (cartCount > 0) {
                Badge { Text(text = cartCount.toString()) }
            }
        }
    ) {
        IconButton(onClick = onCartClick) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrinho de Compras",
                tint = TextPrimary,
                modifier = Modifier.scale(animatedScale) // Aplica a escala animada ao ícone
            )
        }
    }
}
