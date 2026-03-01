package com.jefferson.antenas.ui.screens.profile

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueEnd
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.utils.WHATSAPP_PHONE

@Composable
fun AboutScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(MidnightBlueStart, MidnightBlueEnd)))
            .statusBarsPadding()
    ) {
        // ── TopBar ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = TextPrimary)
            }
            Text(
                "Sobre o Aplicativo",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))

            // ── Logo / Ícone ─────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(SignalOrange.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Satellite,
                    contentDescription = null,
                    tint = SignalOrange,
                    modifier = Modifier.size(52.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Jefferson Antenas",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                "Versão 1.0.0",
                color = TextTertiary,
                style = MaterialTheme.typography.bodySmall
            )

            Spacer(Modifier.height(24.dp))

            // ── Sobre a empresa ──────────────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MidnightBlueCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Sobre a Empresa",
                        color = SignalOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "A Jefferson Antenas é especializada em instalação, manutenção e venda de equipamentos de TV por assinatura, antenas e acessórios em Sapezal — MT e região.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = MaterialTheme.typography.bodySmall.fontSize * 1.6f
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Informações de contato ───────────────────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MidnightBlueCard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Contato",
                        color = SignalOrange,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(12.dp))

                    AboutContactRow(
                        icon = Icons.Default.Phone,
                        label = "WhatsApp",
                        value = "+55 (65) 9 9289-5296",
                        iconColor = SuccessGreen,
                        onClick = {
                            try {
                                val uri = Uri.parse("https://wa.me/$WHATSAPP_PHONE")
                                context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                            } catch (_: Exception) {
                                Toast.makeText(context, "WhatsApp não encontrado.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 8.dp))
                    AboutContactRow(
                        icon = Icons.Default.Place,
                        label = "Endereço",
                        value = "Sapezal — MT, Brasil",
                        iconColor = SatelliteBlue
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            Text(
                "Desenvolvido com ♥ para nossos clientes",
                color = TextTertiary,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AboutContactRow(
    icon: ImageVector,
    label: String,
    value: String,
    iconColor: Color,
    onClick: (() -> Unit)? = null
) {
    val modifier = if (onClick != null)
        Modifier.fillMaxWidth().then(
            Modifier.padding(0.dp)
        )
    else Modifier.fillMaxWidth()

    Surface(
        modifier = modifier,
        color = Color.Transparent,
        onClick = onClick ?: {}
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(label, color = TextTertiary, style = MaterialTheme.typography.labelSmall)
                Text(value, color = TextPrimary, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
