package com.jefferson.antenas.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- CORES SÓLIDAS ---

// Tons Escuros (Fundo)
val MidnightBlueStart = Color(0xFF0F172A)
val MidnightBlueEnd = Color(0xFF1E293B)
val MidnightBlueCard = Color(0xFF1E293B)

// Laranja (Destaque Principal)
val SignalOrange = Color(0xFFF59E0B)
val SignalOrangeDark = Color(0xFFD97706)

// Azul (Secundário/Tech)
val SatelliteBlue = Color(0xFF3B82F6)
val SatelliteBlueDark = Color(0xFF2563EB)

// Status
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningYellow = Color(0xFFFBBF24)
val AccentPink = Color(0xFFEC4899)

// Textos
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFCBD5E1)
val TextTertiary = Color(0xFF94A3B8)

// Bordas e Cartões
val CardBorder = Color(0xFF3D4A5C)
val CardGradientStart = Color(0xFF2A3544)
val CardGradientEnd = Color(0xFF1E2836)

// --- DEGRADÊS (GRADIENTS) ---

// Fundo dos Cartões (Efeito Premium)
val CardPremiumGradient = Brush.verticalGradient(
    colors = listOf(CardGradientStart, CardGradientEnd)
)

// Botões Principais (Laranja Brilhante)
val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(SignalOrange, SignalOrangeDark)
)

// Fundo Geral das Telas (CORREÇÃO AQUI)
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(MidnightBlueStart, MidnightBlueEnd)
)