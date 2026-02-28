package com.jefferson.antenas.ui.screens.profile

import android.content.Intent
import android.net.Uri
import com.jefferson.antenas.utils.WHATSAPP_PHONE
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.jefferson.antenas.ui.theme.AccentPink
import com.jefferson.antenas.ui.theme.BackgroundGradient
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SatelliteBlueDark
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SignalOrangeDark
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.ui.theme.WarningYellow
import java.net.URLEncoder
import kotlinx.coroutines.launch

// ── Loyalty Tiers ────────────────────────────────────────────────────────────

private data class LoyaltyTier(
    val name: String,
    val emoji: String,
    val color: Color,
    val minPoints: Int,
    val maxPoints: Int,
    val benefit: String,
    val offerTitle: String,
    val offerDesc: String
)

private val TIERS = listOf(
    LoyaltyTier(
        "Bronze", "🥉", Color(0xFFCD7F32), 0, 99,
        "Acesso a promoções exclusivas",
        "Suba para Prata!",
        "Acumule 100 pontos para desbloquear 5% de desconto em todas as compras."
    ),
    LoyaltyTier(
        "Prata", "🥈", Color(0xFFC0C0C0), 100, 499,
        "5% de desconto em todas as compras",
        "Seu desconto de 5% está ativo! 🎉",
        "Aplica automaticamente em todos os seus pedidos. Suba para Ouro!"
    ),
    LoyaltyTier(
        "Ouro", "🥇", Color(0xFFFFD700), 500, 999,
        "10% de desconto + frete grátis sempre",
        "Frete grátis ativado! 🚚",
        "Você tem 10% de desconto e frete grátis em todos os pedidos. Parabéns!"
    ),
    LoyaltyTier(
        "Diamante", "💎", Color(0xFF00D4FF), 1000, 9999,
        "15% de desconto + atendimento VIP",
        "Você é VIP Diamond! 💎",
        "Atendimento prioritário, 15% de desconto exclusivo e acesso antecipado a lançamentos."
    )
)

private fun getTier(points: Int): LoyaltyTier =
    TIERS.lastOrNull { points >= it.minPoints } ?: TIERS.firstOrNull() ?: TIERS[0]

private fun getNextTier(points: Int): LoyaltyTier? =
    TIERS.firstOrNull { points < it.minPoints }

// ── Achievements ─────────────────────────────────────────────────────────────

private data class Achievement(
    val emoji: String,
    val title: String,
    val desc: String,
    val unlocked: Boolean
)

private val ALL_ACHIEVEMENTS = listOf(
    Achievement("🛒", "Primeira Compra", "Realizou o primeiro pedido", true),
    Achievement("💬", "Cliente Ativo", "Entrou em contato com suporte", true),
    Achievement("⭐", "Avaliação 5★", "Avaliou um produto", true),
    Achievement("🏆", "100 Pontos", "Acumulou 100 pontos de fidelidade", false),
    Achievement("🥇", "Membro Ouro", "Atingiu o nível Ouro (500 pts)", false),
    Achievement("💎", "VIP Diamond", "Atingiu o nível Diamante (1000 pts)", false)
)

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onOrdersClick: () -> Unit = {},
    onFavoritesClick: () -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onFaqClick: () -> Unit = {},
    onEditProfileClick: () -> Unit = {},
    onSecurityClick: () -> Unit = {},
    onAboutClick: () -> Unit = {},
    onPrivacyClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun showComingSoon(feature: String) {
        scope.launch {
            snackbarHostState.showSnackbar("$feature — Em breve! 🚧")
        }
    }

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

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
                    onClick = { showLogoutDialog = false; viewModel.logout() },
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

    Scaffold(
        containerColor = MidnightBlueStart,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MidnightBlueCard,
                    contentColor = TextPrimary,
                    actionColor = SignalOrange
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        Text(uiState.error ?: "", color = ErrorRed, textAlign = TextAlign.Center, fontSize = 14.sp)
                    }
                }

                uiState.user != null -> {
                    val user = uiState.user ?: return@Box
                    ProfileContent(
                        user = user,
                        onWhatsApp = {
                            val phone = WHATSAPP_PHONE
                            val msg = "Olá Jefferson! Preciso de suporte na minha conta."
                            try {
                                val url = "https://api.whatsapp.com/send?phone=$phone&text=${
                                    URLEncoder.encode(msg, "UTF-8")
                                }"
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                            } catch (_: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("WhatsApp não encontrado. Instale o aplicativo.") }
                            }
                        },
                        onShareReferral = {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Baixe o app Jefferson Antenas e ganhe os melhores preços em receptores e antenas! 📡🔥"
                                    )
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Compartilhar"))
                            } catch (_: Exception) {
                                scope.launch { snackbarHostState.showSnackbar("Não foi possível compartilhar.") }
                            }
                        },
                        onLogout = { showLogoutDialog = true },
                        onOrdersClick = onOrdersClick,
                        onFavoritesClick = onFavoritesClick,
                        onDownloadsClick = onDownloadsClick,
                        onSupportClick = onSupportClick,
                        onFaqClick = onFaqClick,
                        onEditProfileClick = onEditProfileClick,
                        onSecurityClick = onSecurityClick,
                        onAboutClick = onAboutClick,
                        onPrivacyClick = onPrivacyClick
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
}

// ── Profile Content ───────────────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    user: User,
    onWhatsApp: () -> Unit,
    onShareReferral: () -> Unit,
    onLogout: () -> Unit,
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onDownloadsClick: () -> Unit,
    onSupportClick: () -> Unit,
    onFaqClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSecurityClick: () -> Unit,
    onAboutClick: () -> Unit,
    onPrivacyClick: () -> Unit
) {
    val tier = getTier(user.points)
    val nextTier = getNextTier(user.points)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── 1. Header ──────────────────────────────────────────────────
        ProfileHeroHeader(user = user, tier = tier)

        // ── 2. Stats Row ───────────────────────────────────────────────
        ProfileStatsRow(points = user.points, tier = tier)

        // ── 3. Quick Actions ───────────────────────────────────────────
        QuickActionsSection(
            onOrdersClick = onOrdersClick,
            onFavoritesClick = onFavoritesClick,
            onSupportClick = onSupportClick,
            onDownloadsClick = onDownloadsClick
        )

        // ── 4. Exclusive Offer Card ────────────────────────────────────
        TierExclusiveCard(tier = tier, points = user.points, nextTier = nextTier)

        // ── 5. Loyalty Card ────────────────────────────────────────────
        LoyaltyCard(user = user, tier = tier, nextTier = nextTier)

        // ── 6. Achievements ────────────────────────────────────────────
        AchievementsSection(points = user.points)

        // ── 7. Referral Card ───────────────────────────────────────────
        ReferralCard(onShare = onShareReferral)

        // ── 8. Minha Conta ─────────────────────────────────────────────
        ProfileSection(
            title = "Minha Conta",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Person,
                iconColor = SatelliteBlue,
                title = "Editar Perfil",
                subtitle = "Nome e dados pessoais",
                onClick = onEditProfileClick
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Lock,
                iconColor = WarningYellow,
                title = "Segurança",
                subtitle = "Alterar senha",
                onClick = onSecurityClick
            )
        }

        // ── 9. Suporte ─────────────────────────────────────────────────
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
                onClick = onSupportClick
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Help,
                iconColor = SatelliteBlue,
                title = "Perguntas Frequentes",
                subtitle = "Dúvidas sobre pedidos e produtos",
                onClick = onFaqClick
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Info,
                iconColor = TextTertiary,
                title = "Sobre o Aplicativo",
                subtitle = "Versão 1.0.0 • Jefferson Antenas",
                onClick = onAboutClick
            )
            ProfileMenuDivider()
            ProfileMenuItem(
                icon = Icons.Default.Description,
                iconColor = TextTertiary,
                title = "Termos e Privacidade",
                subtitle = "Política de uso e dados",
                onClick = onPrivacyClick
            )
        }

        // ── 10. Logout ─────────────────────────────────────────────────
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Sair da Conta", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(12.dp))
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

// ── Hero Header ───────────────────────────────────────────────────────────────

@Composable
private fun ProfileHeroHeader(user: User, tier: LoyaltyTier) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        tier.color.copy(alpha = 0.20f),
                        SignalOrange.copy(alpha = 0.10f),
                        MidnightBlueStart
                    )
                )
            )
            .padding(top = 40.dp, bottom = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(SuccessGreen),
                contentAlignment = Alignment.Center
            ) {}

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(SignalOrange, SignalOrangeDark))
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

            // Online dot + tier badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SuccessGreen)
                        )
                        Text("Online", fontSize = 10.sp, color = SuccessGreen, fontWeight = FontWeight.SemiBold)
                    }
                }
                Surface(
                    color = tier.color.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(tier.emoji, fontSize = 12.sp)
                        Text(
                            "Membro ${tier.name}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlueStart
                        )
                    }
                }
            }

            // Name
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
                Icon(Icons.Default.Email, null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                Text(user.email, fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}

// ── Stats Row ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileStatsRow(points: Int, tier: LoyaltyTier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            emoji = "⭐",
            value = "$points",
            label = "Pontos",
            color = SignalOrange
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            emoji = "🛒",
            value = "5",
            label = "Pedidos",
            color = SatelliteBlue
        )
        ProfileStatCard(
            modifier = Modifier.weight(1f),
            emoji = tier.emoji,
            value = tier.name,
            label = "Nível",
            color = tier.color
        )
    }
}

@Composable
private fun ProfileStatCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.20f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 18.sp)
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, maxLines = 1)
            Text(label, color = TextSecondary, fontSize = 10.sp)
        }
    }
}

// ── Quick Actions ─────────────────────────────────────────────────────────────

@Composable
private fun QuickActionsSection(
    onOrdersClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSupportClick: () -> Unit,
    onDownloadsClick: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        SectionTitle("Acesso Rápido")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.ShoppingBag,
                label = "Meus Pedidos",
                color = SatelliteBlue,
                modifier = Modifier.weight(1f),
                onClick = onOrdersClick
            )
            QuickActionCard(
                icon = Icons.Default.Favorite,
                label = "Favoritos",
                color = ErrorRed,
                modifier = Modifier.weight(1f),
                onClick = onFavoritesClick
            )
            QuickActionCard(
                icon = Icons.Default.Message,
                label = "Suporte",
                color = SuccessGreen,
                modifier = Modifier.weight(1f),
                onClick = onSupportClick
            )
            QuickActionCard(
                icon = Icons.Default.Download,
                label = "Downloads",
                color = SignalOrange,
                modifier = Modifier.weight(1f),
                onClick = onDownloadsClick
            )
        }
    }
}

// ── Tier Exclusive Card ───────────────────────────────────────────────────────

@Composable
private fun TierExclusiveCard(tier: LoyaltyTier, points: Int, nextTier: LoyaltyTier?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, tier.color.copy(alpha = 0.35f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(tier.color.copy(alpha = 0.12f), SignalOrange.copy(alpha = 0.08f))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tier.emoji, fontSize = 32.sp)
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tier.offerTitle,
                        color = tier.color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        tier.offerDesc,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    if (nextTier != null) {
                        Spacer(Modifier.height(8.dp))
                        val remaining = nextTier.minPoints - points
                        val progress = ((points - tier.minPoints).toFloat() /
                                (nextTier.minPoints - tier.minPoints)).coerceIn(0f, 1f)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(MidnightBlueStart)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(listOf(tier.color, nextTier.color))
                                    )
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Faltam $remaining pts para ${nextTier.emoji} ${nextTier.name}",
                            color = TextTertiary,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// ── Loyalty Card ──────────────────────────────────────────────────────────────

@Composable
private fun LoyaltyCard(user: User, tier: LoyaltyTier, nextTier: LoyaltyTier?) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(18.dp),
        shadowElevation = 6.dp,
        border = BorderStroke(
            1.dp,
            Brush.horizontalGradient(
                listOf(tier.color.copy(alpha = 0.6f), SignalOrange.copy(alpha = 0.3f))
            )
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                    border = BorderStroke(0.5.dp, tier.color.copy(alpha = 0.4f))
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

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    user.points.toString(),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = SignalOrange,
                    lineHeight = 52.sp
                )
                Text(
                    "pontos",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

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

            if (nextTier != null) {
                val progress = ((user.points - tier.minPoints).toFloat() /
                        (nextTier.minPoints - tier.minPoints)).coerceIn(0f, 1f)
                val remaining = nextTier.minPoints - user.points

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${tier.emoji} ${tier.name}", fontSize = 10.sp, color = TextTertiary)
                    Text("${nextTier.emoji} ${nextTier.name}", fontSize = 10.sp, color = TextTertiary)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Default.Info, null, tint = TextTertiary, modifier = Modifier.size(13.dp))
                Text(
                    "Ganhe 1 ponto a cada R\$10,00 em compras",
                    fontSize = 11.sp,
                    color = TextTertiary
                )
            }
        }
    }
}

// ── Achievements ──────────────────────────────────────────────────────────────

@Composable
private fun AchievementsSection(points: Int) {
    val achievements = ALL_ACHIEVEMENTS.map { ach ->
        when (ach.title) {
            "100 Pontos" -> ach.copy(unlocked = points >= 100)
            "Membro Ouro" -> ach.copy(unlocked = points >= 500)
            "VIP Diamond" -> ach.copy(unlocked = points >= 1000)
            else -> ach
        }
    }

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionTitle("Conquistas")
            val unlocked = achievements.count { it.unlocked }
            Text(
                "$unlocked/${achievements.size}",
                color = SignalOrange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(achievements) { ach ->
                AchievementCard(achievement = ach)
            }
        }
    }
}

@Composable
private fun AchievementCard(achievement: Achievement) {
    Surface(
        modifier = Modifier.width(96.dp),
        color = if (achievement.unlocked)
            CardGradientStart
        else
            MidnightBlueCard.copy(alpha = 0.5f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            1.dp,
            if (achievement.unlocked) SignalOrange.copy(alpha = 0.35f)
            else CardBorder.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (achievement.unlocked)
                            SignalOrange.copy(alpha = 0.15f)
                        else
                            CardBorder.copy(alpha = 0.3f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (achievement.unlocked) achievement.emoji else "🔒",
                    fontSize = 22.sp
                )
            }
            Text(
                achievement.title,
                color = if (achievement.unlocked) TextPrimary else TextTertiary,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                lineHeight = 13.sp
            )
            if (achievement.unlocked) {
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        "✓ Obtida",
                        color = SuccessGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            } else {
                Text(
                    achievement.desc,
                    color = TextTertiary,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 12.sp,
                    maxLines = 2
                )
            }
        }
    }
}

// ── Referral Card ─────────────────────────────────────────────────────────────

@Composable
private fun ReferralCard(onShare: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, SatelliteBlue.copy(alpha = 0.30f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(SatelliteBlue.copy(alpha = 0.10f), SatelliteBlueDark.copy(alpha = 0.06f))
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SatelliteBlue.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎁", fontSize = 22.sp)
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Indique e Ganhe Pontos!",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Compartilhe o app com amigos e ganhe 50 pontos por indicação aprovada.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = onShare,
                    colors = ButtonDefaults.buttonColors(containerColor = SatelliteBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Indicar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Private Components ────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(4.dp, 16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(SignalOrange)
        )
        Spacer(Modifier.width(8.dp))
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

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
        border = BorderStroke(0.5.dp, CardBorder)
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
        SectionTitle(title)
        Spacer(Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MidnightBlueCard,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(0.5.dp, CardBorder),
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(subtitle, fontSize = 11.sp, color = TextTertiary)
        }
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
