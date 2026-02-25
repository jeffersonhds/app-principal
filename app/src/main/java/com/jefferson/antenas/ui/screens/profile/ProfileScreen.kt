package com.jefferson.antenas.ui.screens.profile

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.data.model.User
import com.jefferson.antenas.ui.theme.*
import java.net.URLEncoder

// ══════════════════════════════════════════════════════════════════════════════
// MODELOS DE NÍVEL DE FIDELIDADE
// ══════════════════════════════════════════════════════════════════════════════

private data class LoyaltyTier(
    val name: String,
    val emoji: String,
    val color: Color,
    val minPoints: Int,
    val maxPoints: Int,
    val benefit: String
)

private val TIERS = listOf(
    LoyaltyTier("Bronze",   "🥉", Color(0xFFCD7F32), 0,    99,   "Acesso a promoções exclusivas"),
    LoyaltyTier("Prata",    "🥈", Color(0xFFC0C0C0), 100,  499,  "5% de desconto em todas as compras"),
    LoyaltyTier("Ouro",     "🥇", Color(0xFFFFD700), 500,  999,  "10% de desconto + frete grátis sempre"),
    LoyaltyTier("Diamante", "💎", Color(0xFF00D4FF), 1000, 9999, "15% de desconto + atendimento VIP")
)

private fun getTier(points: Int): LoyaltyTier =
    TIERS.lastOrNull { points >= it.minPoints } ?: TIERS.first()

private fun getNextTier(points: Int): LoyaltyTier? =
    TIERS.firstOrNull { points < it.minPoints }

// ══════════════════════════════════════════════════════════════════════════════
// TELA PRINCIPAL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    // Diálogo de confirmação de logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = MidnightBlueCard,
            icon = {
                Icon(Icons.Default.ExitToApp, null, tint = ErrorRed, modifier = Modifier.size(32.dp))
            },
            title = {
                Text("Sair da conta?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Você precisará fazer login novamente para acessar sua conta.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Sair", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = BackgroundGradient)
    ) {
        when {
            uiState.isLoading -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(color = SignalOrange, strokeWidth = 3.dp)
                    Text("Carregando perfil...", color = TextSecondary, fontSize = 13.sp)
                }
            }

            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                    Text(uiState.error!!, color = ErrorRed, textAlign = TextAlign.Center, fontSize = 14.sp)
                }
            }

            uiState.user != null -> {
                ProfileContent(
                    user = uiState.user!!,
                    onWhatsApp = {
                        val phone = "5565992895296"
                        val msg = "Olá Jefferson! Preciso de suporte na minha conta."
                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                URLEncoder.encode(msg, "UTF-8")
                            }"
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                        } catch (_: Exception) {}
                    },
                    onLogout = { showLogoutDialog = true }
                )
            }

            else -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Person, null, tint = TextTertiary, modifier = Modifier.size(52.dp))
                    Text("Perfil não disponível", color = TextSecondary, fontSize = 15.sp)
                }
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// CONTEÚDO DO PERFIL
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun ProfileContent(
    user: User,
    onWhatsApp: () -> Unit,
    onLogout: () -> Unit
) {
    val tier = getTier(user.points)
    val nextTier = getNextTier(user.points)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // ════════════════════════════════════════════════════════════════
        // 1. HEADER — Avatar + Nome + Tier
        // ════════════════════════════════════════════════════════════════
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            SignalOrange.copy(alpha = 0.25f),
                            MidnightBlueStart
                        )
                    )
                )
                .padding(top = 48.dp, bottom = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Avatar com borda gradiente
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SignalOrange, SignalOrangeDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.name.firstOrNull()?.uppercase() ?: "U",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MidnightBlueStart
                    )
                }

                // Badge de nível sobre o avatar (offset)
                Surface(
                    color = tier.color.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(tier.emoji, fontSize = 13.sp)
                        Text(
                            "Membro ${tier.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlueStart
                        )
                    }
                }

                // Nome
                Text(
                    text = user.name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Email
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(Icons.Default.Email, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                    Text(user.email, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        // ════════════════════════════════════════════════════════════════
        // 2. CARD DE PONTOS DE FIDELIDADE
        // ════════════════════════════════════════════════════════════════
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = MidnightBlueCard,
            shape = RoundedCornerShape(18.dp),
            shadowElevation = 6.dp,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.horizontalGradient(listOf(tier.color.copy(alpha = 0.6f), SignalOrange.copy(alpha = 0.3f)))
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {

                // Header do card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Star, null, tint = SignalOrange, modifier = Modifier.size(20.dp))
                        Text(
                            "Programa de Fidelidade",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Surface(
                        color = tier.color.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, tier.color.copy(alpha = 0.4f))
                    ) {
                        Text(
                            "${tier.emoji} ${tier.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = tier.color,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Saldo de pontos
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        user.points.toString(),
                        fontSize = 56.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SignalOrange,
                        lineHeight = 56.sp
                    )
                    Text(
                        "pontos",
                        fontSize = 16.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                // Benefício atual
                Surface(
                    color = tier.color.copy(alpha = 0.10f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = tier.color, modifier = Modifier.size(15.dp))
                        Text(tier.benefit, fontSize = 12.sp, color = TextSecondary)
                    }
                }

                // Progresso para próximo nível
                if (nextTier != null) {
                    val progress = ((user.points - tier.minPoints).toFloat() /
                            (nextTier.minPoints - tier.minPoints)).coerceIn(0f, 1f)
                    val remaining = nextTier.minPoints - user.points

                    Spacer(Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${tier.emoji} ${tier.name}",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                        Text(
                            "${nextTier.emoji} ${nextTier.name}",
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MidnightBlueStart)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(progress)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.horizontalGradient(listOf(tier.color, nextTier.color))
                                )
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    Text(
                        "Faltam $remaining pontos para ${nextTier.emoji} ${nextTier.name}",
                        fontSize = 11.sp,
                        color = TextTertiary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // Nível máximo
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏆", fontSize = 18.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Você atingiu o nível máximo!",
                            fontSize = 13.sp,
                            color = tier.color,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(10.dp))

                // Como ganhar pontos
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Info, null, tint = TextTertiary, modifier = Modifier.size(14.dp))
                    Text(
                        "Ganhe 1 ponto a cada R\$10,00 em compras",
                        fontSize = 11.sp,
                        color = TextTertiary
                    )
                }
            }
        }

        // ════════════════════════════════════════════════════════════════
        // 3. AÇÕES RÁPIDAS (2x2 grid)
        // ════════════════════════════════════════════════════════════════
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Acesso Rápido",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.ShoppingBag,
                    label = "Meus Pedidos",
                    color = SatelliteBlue,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                QuickActionCard(
                    icon = Icons.Default.Favorite,
                    label = "Favoritos",
                    color = ErrorRed,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
                QuickActionCard(
                    icon = Icons.Default.Message,
                    label = "Suporte",
                    color = SuccessGreen,
                    modifier = Modifier.weight(1f),
                    onClick = onWhatsApp
                )
                QuickActionCard(
                    icon = Icons.Default.Download,
                    label = "Downloads",
                    color = SignalOrange,
                    modifier = Modifier.weight(1f),
                    onClick = {}
                )
            }
        }

        // ════════════════════════════════════════════════════════════════
        // 4. SEÇÃO "MINHA CONTA"
        // ════════════════════════════════════════════════════════════════
        ProfileSection(
            title = "Minha Conta",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                iconColor = SatelliteBlue,
                title = "Editar Perfil",
                subtitle = "Nome, foto e dados pessoais",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Notifications,
                iconColor = SignalOrange,
                title = "Notificações",
                subtitle = "Promoções, pedidos e novidades",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.LocationOn,
                iconColor = SuccessGreen,
                title = "Meus Endereços",
                subtitle = "Gerenciar endereços de entrega",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.CreditCard,
                iconColor = AccentPink,
                title = "Formas de Pagamento",
                subtitle = "Cartões e métodos salvos",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Lock,
                iconColor = WarningYellow,
                title = "Segurança",
                subtitle = "Senha e autenticação",
                onClick = {}
            )
        }

        // ════════════════════════════════════════════════════════════════
        // 5. SEÇÃO "SUPORTE"
        // ════════════════════════════════════════════════════════════════
        ProfileSection(
            title = "Suporte & Informações",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Message,
                iconColor = SuccessGreen,
                title = "Falar com Suporte",
                subtitle = "Atendimento via WhatsApp",
                badge = "Online",
                badgeColor = SuccessGreen,
                onClick = onWhatsApp
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Help,
                iconColor = SatelliteBlue,
                title = "Perguntas Frequentes",
                subtitle = "Dúvidas sobre pedidos e produtos",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Info,
                iconColor = TextTertiary,
                title = "Sobre o Aplicativo",
                subtitle = "Versão 1.0.0 • Jefferson Antenas",
                onClick = {}
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Description,
                iconColor = TextTertiary,
                title = "Termos e Privacidade",
                subtitle = "Política de uso e dados",
                onClick = {}
            )
        }

        // ════════════════════════════════════════════════════════════════
        // 6. CARD "COMO FUNCIONA O PROGRAMA"
        // ════════════════════════════════════════════════════════════════
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            color = SignalOrange.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, SignalOrange.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("⭐", fontSize = 18.sp)
                    Text(
                        "Como funciona o Programa de Pontos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Spacer(Modifier.height(12.dp))
                PointsInfoRow("🛒", "A cada R\$10 em compras, ganhe 1 ponto")
                Spacer(Modifier.height(6.dp))
                PointsInfoRow("🥉", "Bronze: acesso a promoções exclusivas")
                Spacer(Modifier.height(6.dp))
                PointsInfoRow("🥈", "Prata (100 pts): 5% de desconto")
                Spacer(Modifier.height(6.dp))
                PointsInfoRow("🥇", "Ouro (500 pts): 10% + frete grátis")
                Spacer(Modifier.height(6.dp))
                PointsInfoRow("💎", "Diamante (1000 pts): 15% + VIP")
            }
        }

        // ════════════════════════════════════════════════════════════════
        // 7. BOTÃO DE LOGOUT
        // ════════════════════════════════════════════════════════════════
        Spacer(Modifier.height(8.dp))

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sair da Conta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(12.dp))

        // Versão do app
        Text(
            "Jefferson Antenas App • v1.0.0",
            fontSize = 10.sp,
            color = TextTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// COMPONENTES PRIVADOS
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = color.copy(alpha = 0.14f),
                shape = CircleShape
            ) {
                Icon(
                    icon, null,
                    tint = color,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(9.dp)
                )
            }
            Text(
                label,
                fontSize = 10.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ProfileSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MidnightBlueCard,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, CardBorder),
            shadowElevation = 2.dp
        ) {
            Column(content = content)
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    badge: String? = null,
    badgeColor: Color = SuccessGreen,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Ícone com fundo colorido
        Surface(
            color = iconColor.copy(alpha = 0.14f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                icon, null,
                tint = iconColor,
                modifier = Modifier
                    .size(36.dp)
                    .padding(8.dp)
            )
        }

        // Textos
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextTertiary)
        }

        // Badge (opcional)
        if (badge != null) {
            Surface(
                color = badgeColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    badge,
                    fontSize = 10.sp,
                    color = badgeColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Icon(
            Icons.Default.ChevronRight, null,
            tint = TextTertiary,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileMenuDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp),
        color = CardBorder,
        thickness = 0.5.dp
    )
}

@Composable
private fun PointsInfoRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(emoji, fontSize = 14.sp)
        Text(text, fontSize = 12.sp, color = TextSecondary)
    }
}
