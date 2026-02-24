package com.jefferson.antenas.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jefferson.antenas.R
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // 1. Carrega o arquivo JSON (Certifique-se que o nome é splash_anim.json na pasta raw)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))

    // 2. Estados de Animação APENAS para o Texto
    val textAlpha = remember { Animatable(0f) } // Começa invisível
    val textScale = remember { Animatable(0.8f) } // Começa um pouco menor

    LaunchedEffect(key1 = true) {
        // Inicia a animação dos textos
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        launch {
            textScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        // Tempo total que a Splash fica na tela (2.5 segundos)
        delay(2500)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart), // Fundo Azul Escuro Oficial
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // 📡 AQUI ENTRA O SEU JSON (Já animado)
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever, // Fica repetindo enquanto carrega
                modifier = Modifier
                    .size(350.dp) // Tamanho da animação (Ajuste se ficar grande/pequeno)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✍️ TÍTULO (Animado via Código: Fade In + Zoom)
            Text(
                text = "Jefferson Antenas",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✍️ SUBTÍTULO (Animado igual)
            Text(
                text = "Conectando você ao mundo",
                color = SignalOrange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )
        }
    }
}