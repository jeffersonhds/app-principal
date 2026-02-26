package com.jefferson.antenas.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jefferson.antenas.R
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SignalOrangeDark
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))

    // ── Animatables ────────────────────────────────────────────────────────────
    val logoAlpha   = remember { Animatable(0f) }
    val logoScale   = remember { Animatable(0.4f) }
    val glowAlpha   = remember { Animatable(0f) }
    val titleAlpha  = remember { Animatable(0f) }
    val titleOffset = remember { Animatable(30f) }  // slides up from below
    val taglineAlpha = remember { Animatable(0f) }
    val progress    = remember { Animatable(0f) }
    val dotsAlpha   = remember { Animatable(0f) }
    val bgGlow      = remember { Animatable(0f) }

    // Pulsing glow behind logo
    val infiniteTransition = rememberInfiniteTransition(label = "glow_pulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.18f,
        targetValue  = 0.35f,
        animationSpec = infiniteRepeatable(
            animation   = tween(1100, easing = FastOutSlowInEasing),
            repeatMode  = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    // Dot 1/2/3 pulsing with offset delays
    val dot1 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "d1"
    )
    val dot2 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 200), RepeatMode.Reverse),
        label = "d2"
    )
    val dot3 by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600, delayMillis = 400), RepeatMode.Reverse),
        label = "d3"
    )

    // ── Sequência cinematográfica ──────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Background glow fade in
        launch { bgGlow.animateTo(1f, tween(800)) }

        // Logo: glow then scale+fade in
        launch {
            delay(100)
            glowAlpha.animateTo(1f, tween(500))
        }
        launch {
            delay(150)
            logoAlpha.animateTo(1f, tween(600))
            logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 200f))
        }

        // Title slides up after logo settles
        launch {
            delay(700)
            titleAlpha.animateTo(1f, tween(500))
            titleOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        }

        // Tagline fades after title
        launch {
            delay(1000)
            taglineAlpha.animateTo(1f, tween(500))
        }

        // Dots appear
        launch {
            delay(1100)
            dotsAlpha.animateTo(1f, tween(400))
        }

        // Progress bar fills across the full duration
        launch {
            delay(400)
            progress.animateTo(1f, tween(2800, easing = LinearEasing))
        }

        // Total splash time: 3.5s
        delay(3500)
        onSplashFinished()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A1628),
                        MidnightBlueStart,
                        Color(0xFF0C1A2E)
                    )
                )
            )
    ) {
        // Background radial glow (top)
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .clip(CircleShape)
                .alpha(bgGlow.value * 0.4f)
                .background(
                    Brush.radialGradient(
                        listOf(SignalOrange.copy(alpha = 0.20f), Color.Transparent)
                    )
                )
        )

        // Background radial glow (bottom)
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .clip(CircleShape)
                .alpha(bgGlow.value * 0.3f)
                .background(
                    Brush.radialGradient(
                        listOf(SatelliteBlue.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        // ── Central content ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Glow ring + Lottie
            Box(contentAlignment = Alignment.Center) {

                // Outer pulse ring
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .alpha(glowAlpha.value * glowPulse)
                        .background(
                            Brush.radialGradient(
                                listOf(SignalOrange.copy(alpha = 0.28f), Color.Transparent)
                            )
                        )
                )

                // Inner glow ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .alpha(glowAlpha.value * (glowPulse + 0.1f).coerceAtMost(1f))
                        .background(
                            Brush.radialGradient(
                                listOf(SignalOrange.copy(alpha = 0.18f), Color.Transparent)
                            )
                        )
                )

                // Lottie animation
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(260.dp)
                        .alpha(logoAlpha.value)
                        .scale(logoScale.value)
                )
            }

            Spacer(Modifier.height(28.dp))

            // "JEFFERSON ANTENAS" — slides up + fades
            Text(
                text = "JEFFERSON ANTENAS",
                color = TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer {
                    alpha = titleAlpha.value
                    translationY = titleOffset.value
                }
            )

            Spacer(Modifier.height(8.dp))

            // Tagline
            Text(
                text = "Conectando você ao mundo",
                color = SignalOrange,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(Modifier.height(36.dp))

            // Pulsing dots loader
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(dotsAlpha.value)
            ) {
                PulsingDot(alpha = dot1)
                PulsingDot(alpha = dot2)
                PulsingDot(alpha = dot3)
            }
        }

        // ── Bottom section ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SignalOrange.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.value)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(SignalOrange, SignalOrangeDark)
                            )
                        )
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(
                text = "v1.0.0 • Cuiabá, MT",
                color = TextTertiary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

// ── Pulsing Dot ───────────────────────────────────────────────────────────────

@Composable
private fun PulsingDot(alpha: Float) {
    Box(
        modifier = Modifier
            .size(7.dp)
            .clip(CircleShape)
            .alpha(alpha)
            .background(SignalOrange)
    )
}
