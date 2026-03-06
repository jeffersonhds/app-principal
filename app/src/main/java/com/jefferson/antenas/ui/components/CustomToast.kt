package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary

/**
 * Toast customizado profissional com animação suave
 *
 * Tipos disponíveis:
 * - ToastType.SUCCESS (verde)
 * - ToastType.ERROR (vermelho)
 * - ToastType.WARNING (laranja)
 * - ToastType.INFO (azul)
 */
enum class ToastType {
    SUCCESS, ERROR, WARNING, INFO
}

data class ToastConfig(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000L
)

@Composable
fun CustomToast(
    visible: Boolean,
    message: String,
    type: ToastType = ToastType.ERROR,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 400)
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // ✅ CORRIGIDO: Sem destructuring, declaração clara
        val backgroundColor: Color
        val iconColor: Color
        val icon: ImageVector
        val textColor: Color

        when (type) {
            ToastType.SUCCESS -> {
                backgroundColor = SuccessGreen.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.CheckCircle
                textColor = Color.White
            }
            ToastType.ERROR -> {
                backgroundColor = ErrorRed.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.Error
                textColor = Color.White
            }
            ToastType.WARNING -> {
                backgroundColor = SignalOrange.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.Warning
                textColor = Color.White
            }
            ToastType.INFO -> {
                backgroundColor = MidnightBlueCard.copy(alpha = 0.95f)
                iconColor = SignalOrange
                icon = Icons.Default.Info
                textColor = TextPrimary
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}