package com.jefferson.antenas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Botões pequenos, inputs
    medium = RoundedCornerShape(12.dp), // Botões principais, cartões simples
    large = RoundedCornerShape(16.dp),  // Cards de produto, Modais
    extraLarge = RoundedCornerShape(24.dp) // Bottom Sheet, Paineis grandes
)