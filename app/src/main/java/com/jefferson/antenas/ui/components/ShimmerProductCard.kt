package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard)
    ) {
        Column(modifier = Modifier.shimmer()) { // Aplica o efeito shimmer a tudo dentro desta coluna
            // Placeholder para a imagem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color(0xFF2A3544))
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Placeholder para o nome do produto
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(24.dp)
                        .background(Color(0xFF2A3544))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder para o preço
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .background(Color(0xFF2A3544))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder para o botão
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF2A3544))
                )
            }
        }
    }
}
