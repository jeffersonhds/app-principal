package com.jefferson.antenas.ui.screens.services

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HomeRepairService
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.jefferson.antenas.ui.theme.AccentPink
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueEnd
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SignalOrangeDark
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.ui.theme.WarningYellow
import com.jefferson.antenas.utils.WhatsAppHelper

// ─────────────────────────────────────────────────────────────
// Data models
// ─────────────────────────────────────────────────────────────

private data class ServiceOffer(
    val id: Int,
    val title: String,
    val emoji: String,
    val icon: ImageVector,
    val description: String,
    val includes: List<String>,
    val priceLabel: String,
    val duration: String,
    val accentColor: Color,
    val isPopular: Boolean = false,
    val isFeatured: Boolean = false
)

private data class Testimonial(
    val name: String,
    val initial: String,
    val rating: Int,
    val text: String,
    val service: String
)

// ─────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────

private val serviceOffers = listOf(
    ServiceOffer(
        id = 0,
        title = "Instalação Completa",
        emoji = "📡",
        icon = Icons.Default.Satellite,
        description = "Instalação profissional de antena parabólica, receptor e cabeamento residencial completo.",
        includes = listOf(
            "Instalação da antena parabólica",
            "Montagem e fixação do receptor",
            "Cabeamento interno completo",
            "Apontamento e alinhamento fino",
            "Configuração e teste de todos os canais",
            "Orientação de uso ao cliente"
        ),
        priceLabel = "A partir de R$ 150",
        duration = "2–4 horas",
        accentColor = SatelliteBlue,
        isPopular = true,
        isFeatured = true
    ),
    ServiceOffer(
        id = 1,
        title = "Apontamento de Antena",
        emoji = "🎯",
        icon = Icons.Default.Router,
        description = "Reajuste preciso do sinal para satélites StarOne, Sky, Claro TV e demais operadoras.",
        includes = listOf(
            "Medição do nível de sinal (dBm)",
            "Reajuste fino do apontamento",
            "Verificação e substituição do LNB",
            "Teste de qualidade de sinal",
            "Relatório técnico básico"
        ),
        priceLabel = "A partir de R$ 80",
        duration = "1–2 horas",
        accentColor = SignalOrange,
        isPopular = true
    ),
    ServiceOffer(
        id = 2,
        title = "Manutenção e Reparo",
        emoji = "🔧",
        icon = Icons.Default.Build,
        description = "Diagnóstico completo, troca de conectores, cabos defeituosos e reparos gerais do sistema.",
        includes = listOf(
            "Diagnóstico completo do sistema",
            "Troca de conectores e cabos",
            "Verificação de aterramento",
            "Limpeza da antena e LNB",
            "Teste de funcionamento pós-reparo"
        ),
        priceLabel = "A partir de R$ 100",
        duration = "1–3 horas",
        accentColor = SuccessGreen
    ),
    ServiceOffer(
        id = 3,
        title = "Atualização de Receptor",
        emoji = "⬆️",
        icon = Icons.Default.Settings,
        description = "Atualização de firmware, lista de canais e configurações completas do receptor.",
        includes = listOf(
            "Atualização do firmware oficial",
            "Lista de canais atualizada",
            "Configuração de canais favoritos",
            "Ajuste de idioma e fuso horário",
            "Teste completo de todos os canais"
        ),
        priceLabel = "A partir de R$ 60",
        duration = "30–60 min",
        accentColor = AccentPink
    ),
    ServiceOffer(
        id = 4,
        title = "Instalação de TV Box",
        emoji = "📺",
        icon = Icons.Default.Tv,
        description = "Configuração completa de TV Box Android (HTV, MXQ, etc) com aplicativos e internet.",
        includes = listOf(
            "Configuração do dispositivo",
            "Instalação de aplicativos essenciais",
            "Conexão e configuração de Wi-Fi",
            "Configuração de controle remoto",
            "Tutorial de uso para o cliente"
        ),
        priceLabel = "A partir de R$ 80",
        duration = "1–2 horas",
        accentColor = WarningYellow
    ),
    ServiceOffer(
        id = 5,
        title = "Visita Técnica",
        emoji = "🏠",
        icon = Icons.Default.HomeRepairService,
        description = "Visita para diagnóstico de problemas com emissão de laudo e orçamento detalhado.",
        includes = listOf(
            "Avaliação completa do sistema",
            "Diagnóstico de falhas e defeitos",
            "Laudo técnico detalhado",
            "Orçamento para reparo/instalação",
            "Até 1 hora de atendimento presencial"
        ),
        priceLabel = "Cobrado no serviço",
        duration = "1 hora",
        accentColor = ErrorRed
    )
)

private val testimonials = listOf(
    Testimonial(
        name = "Carlos Mendes",
        initial = "C",
        rating = 5,
        text = "Serviço excelente! O técnico foi super profissional e deixou tudo funcionando perfeitamente.",
        service = "Instalação Completa"
    ),
    Testimonial(
        name = "Ana Souza",
        initial = "A",
        rating = 5,
        text = "Rápido e eficiente. Resolveu o problema do sinal em menos de 1 hora. Recomendo muito!",
        service = "Apontamento de Antena"
    ),
    Testimonial(
        name = "Roberto Lima",
        initial = "R",
        rating = 5,
        text = "Preço justo e serviço de qualidade. Já é a segunda vez que contrato e sempre ótimo atendimento.",
        service = "Manutenção e Reparo"
    )
)

private const val WHATSAPP_NUMBER = "5565992895296"

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun ServicesScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<Int?>(null) }

    fun requestService(service: ServiceOffer) {
        val msg = "Olá Jefferson Antenas! 👋\n\n" +
            "Gostaria de solicitar um orçamento para o serviço:\n\n" +
            "🔧 *${service.title}*\n" +
            "📋 ${service.description}\n\n" +
            "Poderia me informar disponibilidade e valores?"
        WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
    }

    fun contactGeneral() {
        val msg = "Olá Jefferson Antenas! 👋\n\nGostaria de informações sobre os serviços disponíveis."
        WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = { ServicesTopBar(onBackClick = onBackClick) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Hero ─────────────────────────────────────────
            item { ServicesHeroHeader() }

            // ── Benefits ─────────────────────────────────────
            item { ServicesBenefitsSection() }

            // ── Services header ───────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 20.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SignalOrange)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            "Nossos Serviços",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            "Toque para ver detalhes e solicitar orçamento",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // ── Service cards ─────────────────────────────────
            items(serviceOffers) { service ->
                ServiceCard(
                    service = service,
                    isExpanded = expandedId == service.id,
                    onToggle = {
                        expandedId = if (expandedId == service.id) null else service.id
                    },
                    onRequest = { requestService(service) }
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Coverage area ─────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                ServicesCoverageCard()
            }

            // ── Testimonials ──────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                ServicesTestimonialsSection()
            }

            // ── Bottom CTA ────────────────────────────────────
            item {
                Spacer(Modifier.height(20.dp))
                ServicesBottomCta(onClick = { contactGeneral() })
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesTopBar(onBackClick: () -> Unit) {
    Surface(
        color = MidnightBlueStart,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    "Voltar",
                    tint = SignalOrange
                )
            }
            Text(
                "Nossos Serviços",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Hero Header
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SignalOrange.copy(alpha = 0.22f),
                        SatelliteBlue.copy(alpha = 0.10f),
                        MidnightBlueStart
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 28.dp)
    ) {
        Column {
            // Verified badge
            Surface(
                color = SignalOrange.copy(alpha = 0.2f),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, SignalOrange.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        null,
                        tint = SignalOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Técnico Certificado • Desde 2014",
                        color = SignalOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Serviços Profissionais\nde Antenas e Receptores",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                lineHeight = 28.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Atendimento residencial e comercial com garantia de serviço e suporte pós-instalação.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HeroStatCard(
                    modifier = Modifier.weight(1f),
                    value = "10+",
                    label = "Anos de\nExperiência",
                    color = SignalOrange
                )
                HeroStatCard(
                    modifier = Modifier.weight(1f),
                    value = "500+",
                    label = "Clientes\nAtendidos",
                    color = SatelliteBlue
                )
                HeroStatCard(
                    modifier = Modifier.weight(1f),
                    value = "4.9★",
                    label = "Avaliação\nMédia",
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun HeroStatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(2.dp))
            Text(label, color = TextTertiary, fontSize = 10.sp, textAlign = TextAlign.Center, lineHeight = 13.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Benefits Section
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesBenefitsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth()
            .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SatelliteBlue)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Por que nos escolher?",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        val benefits = listOf(
            Triple(Icons.Default.EmojiEvents, "Experiência Comprovada", SatelliteBlue),
            Triple(Icons.Default.CheckCircle, "Garantia de Serviço", SuccessGreen),
            Triple(Icons.Default.FlashOn, "Atendimento Rápido", SignalOrange),
            Triple(Icons.Default.SupportAgent, "Suporte Pós-Serviço", AccentPink)
        )

        // 2x2 grid
        benefits.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                row.forEach { (icon, label, color) ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .padding(bottom = 10.dp),
                        color = color.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                label,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Service Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServiceCard(
    service: ServiceOffer,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRequest: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) service.accentColor.copy(alpha = 0.6f) else CardBorder,
        animationSpec = tween(300),
        label = "ServiceBorder"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
            // ── Header row (always visible) ──────────────────
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon circle
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        service.accentColor.copy(alpha = 0.25f),
                                        service.accentColor.copy(alpha = 0.08f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(service.emoji, fontSize = 24.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                service.title,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            if (service.isPopular) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    color = SignalOrange.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(5.dp)
                                ) {
                                    Text(
                                        "🔥 POPULAR",
                                        color = SignalOrange,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(3.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Price chip
                            Surface(
                                color = service.accentColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(5.dp)
                            ) {
                                Text(
                                    service.priceLabel,
                                    color = service.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            // Duration chip
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    null,
                                    tint = TextTertiary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(Modifier.width(2.dp))
                                Text(service.duration, color = TextTertiary, fontSize = 10.sp)
                            }
                        }
                    }

                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = if (isExpanded) service.accentColor else TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── Expanded detail ──────────────────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(280)) + fadeIn(tween(280)),
                exit = shrinkVertically(tween(240)) + fadeOut(tween(200))
            ) {
                Column {
                    HorizontalDivider(
                        color = service.accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Description
                        Text(
                            service.description,
                            color = TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        Spacer(Modifier.height(14.dp))

                        // What's included
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint = service.accentColor,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "O que está incluído:",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        service.includes.forEach { item ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(service.accentColor.copy(alpha = 0.7f))
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(item, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Request button
                        Button(
                            onClick = onRequest,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = service.accentColor
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Chat,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Solicitar Orçamento Grátis",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Coverage Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesCoverageCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = SatelliteBlue.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SatelliteBlue.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SatelliteBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    null,
                    tint = SatelliteBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "Área de Atendimento",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "Cuiabá, Várzea Grande e Região Metropolitana",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "Atendimento disponível agora",
                        color = SuccessGreen,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Testimonials
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesTestimonialsSection() {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp, 18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(SuccessGreen)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "O que nossos clientes dizem",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text("Avaliações verificadas", color = TextTertiary, fontSize = 11.sp)
            }
        }

        testimonials.forEach { t ->
            TestimonialCard(testimonial = t)
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TestimonialCard(testimonial: Testimonial) {
    Surface(
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(SignalOrange, SignalOrangeDark)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        testimonial.initial,
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        testimonial.name,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(testimonial.rating) {
                            Icon(
                                Icons.Default.Star,
                                null,
                                tint = SignalOrange,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                // Verified badge
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(
                        "✓ Verificado",
                        color = SuccessGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Text(
                "\"${testimonial.text}\"",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Settings,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(testimonial.service, color = TextTertiary, fontSize = 11.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Bottom CTA
// ─────────────────────────────────────────────────────────────

@Composable
private fun ServicesBottomCta(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(SuccessGreen.copy(alpha = 0.15f), SatelliteBlue.copy(alpha = 0.10f))
                    )
                )
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💬", fontSize = 36.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    "Precisa de um serviço personalizado?",
                    color = TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Fale diretamente com nosso técnico pelo WhatsApp e receba um orçamento gratuito e sem compromisso.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(
                        Icons.Default.Chat,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Falar com o Técnico Agora",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        null,
                        tint = TextTertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Geralmente respondemos em menos de 5 minutos",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
