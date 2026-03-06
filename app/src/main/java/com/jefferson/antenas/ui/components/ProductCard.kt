package com.jefferson.antenas.ui.componets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    // Efeito para reverter o estado do botão após um tempo
    if (isAdded) {
        LaunchedEffect(isAdded) {
            delay(2000)
            isAdded = false
        }
    }

    // Reverte o efeito hover após a navegação
    if (isHovered) {
        LaunchedEffect(isHovered) {
            delay(300)
            isHovered = false
        }
    }

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 16.dp else 8.dp,
        label = "CardElevation"
    )

    val imageAlpha by animateColorAsState(
        targetValue = if (isHovered) Color.Black.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0f),
        label = "ImageOverlay"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isHovered = true
                onClick()
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ✅ IMAGEM COM OVERLAY E BADGES
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay ao passar o mouse
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(imageAlpha)
                )

                // ✅ BADGE DE DESCONTO (canto superior esquerdo)
                if (product.discount != null && product.discount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = ErrorRed,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "-${product.discount}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ✅ BADGE "NOVO" (canto superior direito)
                if (product.isNew == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = SignalOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "NOVO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlueStart,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ✅ BOTÃO DE FAVORITO (canto inferior direito)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isFavorite = !isFavorite }
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (isFavorite) ErrorRed else TextSecondary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }

            // ✅ CONTEÚDO
            Column(modifier = Modifier.padding(12.dp)) {
                // Nome do Produto
                Text(
                    text = product.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ✅ PREÇOS (com desconto tachado)
                if (product.discount != null && product.discount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = product.price.toCurrency(),
                            color = TextTertiary,
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.getDiscountedPrice().toCurrency(),
                            color = SignalOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = product.getDiscountedPrice().toCurrency(),
                        color = SignalOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ✅ BOTÃO ANIMADO COM MAIS ESTILO
                Button(
                    onClick = {
                        if (!isAdded) {
                            onAddToCart(product)
                            isAdded = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdded) SuccessGreen.copy(alpha = 0.2f) else SignalOrange.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Crossfade(targetState = isAdded, label = "AddToCartAnimation") { added ->
                        if (added) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.sp.value.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Adicionado!",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(16.sp.value.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Adicionar",
                                    color = SignalOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}