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
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
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
import com.jefferson.antenas.ui.theme.TextTertiary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ── Particle data ─────────────────────────────────────────────────────────────

private data class Particle(
    val xFrac: Float,
    val phase: Float,
    val sizeDp: Float,
    val baseAlpha: Float,
    val isBlue: Boolean = false
)

private val PARTICLES = listOf(
    Particle(0.08f, 0.00f, 5f, 0.28f),
    Particle(0.18f, 0.15f, 3f, 0.20f, isBlue = true),
    Particle(0.30f, 0.35f, 6f, 0.22f),
    Particle(0.42f, 0.55f, 4f, 0.20f, isBlue = true),
    Particle(0.55f, 0.10f, 5f, 0.25f),
    Particle(0.65f, 0.45f, 3f, 0.18f),
    Particle(0.75f, 0.70f, 5f, 0.22f, isBlue = true),
    Particle(0.85f, 0.25f, 4f, 0.20f),
    Particle(0.92f, 0.85f, 3f, 0.16f, isBlue = true),
    Particle(0.25f, 0.60f, 4f, 0.18f)
)

private const val TITLE_TEXT = "JEFFERSON ANTENAS"

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))

    // ── Animatables ───────────────────────────────────────────────────────────
    val logoAlpha        = remember { Animatable(0f) }
    val logoScale        = remember { Animatable(0.4f) }
    val glowAlpha        = remember { Animatable(0f) }
    val taglineAlpha     = remember { Animatable(0f) }
    val progress         = remember { Animatable(0f) }
    val dotsAlpha        = remember { Animatable(0f) }
    val bgGlow           = remember { Animatable(0f) }
    val particleProgress = remember { Animatable(0f) }

    // One Animatable per character for letter-by-letter reveal
    val charAlphas = remember { TITLE_TEXT.indices.map { Animatable(0f) } }

    // ── Infinite transitions ──────────────────────────────────────────────────
    val infiniteTransition = rememberInfiniteTransition(label = "splash")

    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.18f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_pulse"
    )
    // Radar ring rotates 360° every 3 seconds
    val radarRotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(3000, easing = LinearEasing), RepeatMode.Restart),
        label = "radar"
    )
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

    // ── Cinematic sequence ────────────────────────────────────────────────────
    LaunchedEffect(Unit) {
        // Background glow fade in
        launch { bgGlow.animateTo(1f, tween(800)) }

        // Particles loop continuously
        launch {
            while (true) {
                particleProgress.snapTo(0f)
                particleProgress.animateTo(1f, tween(4000, easing = LinearEasing))
            }
        }

        // Logo: glow appears, then logo fades+scales in (parallel)
        launch { delay(100); glowAlpha.animateTo(1f, tween(500)) }
        launch {
            delay(150)
            launch { logoAlpha.animateTo(1f, tween(700)) }
            logoScale.animateTo(1f, spring(dampingRatio = 0.5f, stiffness = 200f))
        }

        // Title: letter-by-letter (55ms per char, 200ms fade each)
        launch {
            delay(700)
            charAlphas.forEachIndexed { index, anim ->
                launch {
                    delay(index * 55L)
                    anim.animateTo(1f, tween(200))
                }
            }
        }

        // Tagline and dots
        launch { delay(1000); taglineAlpha.animateTo(1f, tween(500)) }
        launch { delay(1100); dotsAlpha.animateTo(1f, tween(400)) }

        // Progress bar
        launch { delay(400); progress.animateTo(1f, tween(4300, easing = LinearEasing)) }

        // Total: 5s
        delay(5000)
        onSplashFinished()
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A1628), MidnightBlueStart, Color(0xFF0C1A2E))
                )
            )
    ) {

        // ── 1. Floating particles (drawn behind everything) ────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(bgGlow.value)
        ) {
            PARTICLES.forEach { p ->
                val pY = (particleProgress.value + p.phase) % 1f
                val screenY = size.height * (1f - pY)
                val screenX = size.width * p.xFrac
                val fade = (1f - pY * 0.55f).coerceIn(0f, 1f)
                val color = if (p.isBlue) SatelliteBlue else SignalOrange
                drawCircle(
                    color = color.copy(alpha = p.baseAlpha * fade),
                    radius = p.sizeDp.dp.toPx() / 2f,
                    center = Offset(screenX, screenY)
                )
            }
        }

        // ── 2. Background radial glows ────────────────────────────────────
        Box(
            modifier = Modifier
                .size(400.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-80).dp)
                .clip(CircleShape)
                .alpha(bgGlow.value * 0.4f)
                .background(Brush.radialGradient(listOf(SignalOrange.copy(alpha = 0.20f), Color.Transparent)))
        )
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .clip(CircleShape)
                .alpha(bgGlow.value * 0.3f)
                .background(Brush.radialGradient(listOf(SatelliteBlue.copy(alpha = 0.15f), Color.Transparent)))
        )

        // ── 3. Central content ────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo zone: glow rings + radar + Lottie
            Box(contentAlignment = Alignment.Center) {

                // Outer pulsing glow ring
                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .clip(CircleShape)
                        .alpha(glowAlpha.value * glowPulse)
                        .background(Brush.radialGradient(listOf(SignalOrange.copy(alpha = 0.28f), Color.Transparent)))
                )

                // Inner glow ring
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .alpha(glowAlpha.value * (glowPulse + 0.1f).coerceAtMost(1f))
                        .background(Brush.radialGradient(listOf(SignalOrange.copy(alpha = 0.18f), Color.Transparent)))
                )

                // Radar ring — rotating arc drawn with Canvas
                Canvas(
                    modifier = Modifier
                        .size(300.dp)
                        .graphicsLayer { rotationZ = radarRotation }
                        .alpha(glowAlpha.value * 0.9f)
                ) {
                    val strokePx = 2.5.dp.toPx()
                    val inset = strokePx

                    // Sweep arc: 0° = trailing (transparent), 90° = leading (orange)
                    // sweepGradient fraction: 90°/360° = 0.25
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f    to Color.Transparent,
                            0.10f to SignalOrange.copy(alpha = 0.08f),
                            0.20f to SignalOrange.copy(alpha = 0.45f),
                            0.25f to SignalOrange.copy(alpha = 0.90f)
                        ),
                        startAngle = 0f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round),
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2)
                    )

                    // Bright dot at the leading edge (90° = bottom of circle)
                    val radius = size.minDimension / 2f - inset
                    val endRad = PI / 2.0
                    drawCircle(
                        color = SignalOrange,
                        radius = 4.dp.toPx(),
                        center = Offset(
                            size.width / 2f + radius * cos(endRad).toFloat(),
                            size.height / 2f + radius * sin(endRad).toFloat()
                        )
                    )
                }

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

            // Letter-by-letter title reveal
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TITLE_TEXT.forEachIndexed { index, char ->
                    Text(
                        text = char.toString(),
                        color = TextPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = if (char != ' ') 2.sp else 0.sp,
                        modifier = Modifier.graphicsLayer {
                            alpha = charAlphas[index].value
                            translationY = (1f - charAlphas[index].value) * 20f
                        }
                    )
                }
            }

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

            // Pulsing dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.alpha(dotsAlpha.value)
            ) {
                PulsingDot(dot1)
                PulsingDot(dot2)
                PulsingDot(dot3)
            }
        }

        // ── 4. Bottom progress bar + version ─────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                        .background(Brush.horizontalGradient(listOf(SignalOrange, SignalOrangeDark)))
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
