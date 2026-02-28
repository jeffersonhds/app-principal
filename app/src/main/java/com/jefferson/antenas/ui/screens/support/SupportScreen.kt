package com.jefferson.antenas.ui.screens.support

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
import androidx.compose.material.icons.automirrored.filled.HelpCenter
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LiveHelp
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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

private data class TroubleshootItem(
    val id: Int,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val color: Color,
    val steps: List<String>
)

private data class FaqItem(
    val question: String,
    val answer: String
)

private data class FaqCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val items: List<FaqItem>
)

private data class SupportTicket(
    val id: String,
    val subject: String,
    val status: TicketStatus,
    val date: String,
    val preview: String
)

private enum class TicketStatus(val label: String, val color: Color) {
    OPEN("Aberto", SatelliteBlue),
    IN_PROGRESS("Em andamento", SignalOrange),
    RESOLVED("Resolvido", SuccessGreen),
    WAITING("Aguardando", WarningYellow)
}

// ─────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────

private val troubleshootItems = listOf(
    TroubleshootItem(
        id = 0,
        icon = Icons.Default.WifiOff,
        title = "Sem sinal na antena",
        subtitle = "Verifique cabos e apontamento",
        color = ErrorRed,
        steps = listOf(
            "Verifique se todos os cabos estão bem conectados na antena e no receptor.",
            "Confira se o LNB (cabeça da antena) não está danificado ou com água.",
            "Acesse o menu de apontamento no receptor e verifique o nível de sinal.",
            "Certifique-se de que não há obstáculos (árvores, prédios) na direção do satélite.",
            "Se o problema persistir, entre em contato com nossa equipe técnica."
        )
    ),
    TroubleshootItem(
        id = 1,
        icon = Icons.Default.Tv,
        title = "Canal codificado / sem imagem",
        subtitle = "Problemas de assinatura ou sinal",
        color = SignalOrange,
        steps = listOf(
            "Verifique se a assinatura do serviço está ativa e em dia.",
            "Aguarde alguns minutos — às vezes é instabilidade temporária do satélite.",
            "Reinicie o receptor desligando da tomada por 30 segundos.",
            "Verifique se o cabo coaxial está firme nos conectores F.",
            "Atualize a lista de canais no menu do receptor."
        )
    ),
    TroubleshootItem(
        id = 2,
        icon = Icons.Default.Info,
        title = "Receptor não liga",
        subtitle = "Sem energia ou falha de hardware",
        color = AccentPink,
        steps = listOf(
            "Verifique se a tomada está funcionando com outro aparelho.",
            "Confira se o cabo de energia está bem conectado no receptor.",
            "Verifique se o fusível interno do receptor não queimou.",
            "Tente outro cabo de energia se disponível.",
            "Se não ligar de nenhuma forma, é necessário manutenção técnica."
        )
    ),
    TroubleshootItem(
        id = 3,
        icon = Icons.Default.NetworkCheck,
        title = "Imagem pixelada / travando",
        subtitle = "Qualidade de sinal insuficiente",
        color = WarningYellow,
        steps = listOf(
            "Verifique o nível de sinal no menu do receptor (mínimo 60%).",
            "Limpe os conectores F com um pano seco — oxidação causa perda de sinal.",
            "Verifique se o cabo coaxial não está dobrado ou danificado.",
            "Em dias de chuva forte, é normal haver instabilidade — aguarde.",
            "Reaponte a antena com ajuda de um técnico para melhor sinal."
        )
    ),
    TroubleshootItem(
        id = 4,
        icon = Icons.Default.SmartToy,
        title = "Controle remoto não funciona",
        subtitle = "Pilhas ou sensor com problema",
        color = SatelliteBlue,
        steps = listOf(
            "Substitua as pilhas por novas (mesmo que pareçam boas).",
            "Limpe os contatos das pilhas com borracha ou lixa fina.",
            "Verifique se o sensor infravermelho do receptor não está obstruído.",
            "Teste apontando o controle diretamente para o sensor do receptor.",
            "Se não funcionar, o controle pode precisar de reposição."
        )
    ),
    TroubleshootItem(
        id = 5,
        icon = Icons.Default.Update,
        title = "Receptor não atualiza",
        subtitle = "Firmware ou lista de canais",
        color = SuccessGreen,
        steps = listOf(
            "Acesse Menu > Configurações > Atualização de Software.",
            "Certifique-se de que a internet está conectada (para receptores híbridos).",
            "Para atualização via USB: baixe o firmware correto em nossa tela de Downloads.",
            "Formate o pen drive como FAT32 antes de copiar o arquivo.",
            "Não desligue o receptor durante a atualização — pode corrompê-lo."
        )
    )
)

private val faqCategories = listOf(
    FaqCategory(
        title = "Pedidos & Entrega",
        icon = Icons.Default.Schedule,
        color = SatelliteBlue,
        items = listOf(
            FaqItem("Qual o prazo de entrega?", "O prazo varia de 3 a 10 dias úteis dependendo da sua região. Após a confirmação do pagamento, você receberá o código de rastreamento via WhatsApp."),
            FaqItem("Como rastrear meu pedido?", "Assim que seu pedido for despachado, enviaremos o código de rastreamento via WhatsApp. Acesse o site dos Correios ou da transportadora com o código para acompanhar."),
            FaqItem("Entregam em todo o Brasil?", "Sim! Entregamos para todo o território nacional. O frete é calculado no checkout com base no seu CEP."),
            FaqItem("Meu pedido não chegou, o que faço?", "Entre em contato com nossa equipe via WhatsApp informando o número do pedido. Verificaremos o status junto à transportadora.")
        )
    ),
    FaqCategory(
        title = "Produtos & Garantia",
        icon = Icons.Default.CheckCircle,
        color = SuccessGreen,
        items = listOf(
            FaqItem("Qual a garantia dos produtos?", "Todos os produtos possuem garantia mínima de 6 meses contra defeitos de fabricação. Produtos selecionados têm garantia estendida de 12 meses."),
            FaqItem("Os produtos são originais?", "Sim! Trabalhamos apenas com produtos originais de marcas reconhecidas como Duosat, AzAmerica, HTV, Globalsat e outras."),
            FaqItem("Como acionar a garantia?", "Entre em contato via WhatsApp com nota fiscal e descrição do defeito. Nossa equipe orientará o processo de devolução ou reparo."),
            FaqItem("Posso devolver um produto?", "Sim, você tem 7 dias corridos após o recebimento para solicitar devolução (Código de Defesa do Consumidor). O produto deve estar em perfeitas condições e na embalagem original.")
        )
    ),
    FaqCategory(
        title = "Pagamento",
        icon = Icons.Default.Info,
        color = SignalOrange,
        items = listOf(
            FaqItem("Quais formas de pagamento aceitam?", "Aceitamos cartão de crédito (até 12x), PIX (5% de desconto) e negociação via WhatsApp."),
            FaqItem("O PIX tem desconto?", "Sim! Pagamentos via PIX têm 5% de desconto sobre o valor total do pedido."),
            FaqItem("Parcelamento tem juros?", "Parcelamento em até 3x é sem juros. De 4x a 12x, consulte as condições no checkout ou via WhatsApp."),
            FaqItem("Meu pagamento não foi aprovado, o que faço?", "Verifique com seu banco se o cartão está habilitado para compras online. Se o problema persistir, tente via PIX ou entre em contato.")
        )
    ),
    FaqCategory(
        title = "Instalação & Técnico",
        icon = Icons.Default.Build,
        color = AccentPink,
        items = listOf(
            FaqItem("Vocês fazem instalação?", "Sim! Oferecemos instalação profissional em Cuiabá, Várzea Grande e região. Solicite orçamento pelo WhatsApp ou pela tela de Serviços no app."),
            FaqItem("Quanto custa a instalação?", "O valor varia conforme o serviço: instalação completa a partir de R$ 150, apontamento a partir de R$ 80. Consulte nossa tela de Serviços para detalhes."),
            FaqItem("Fornecem suporte técnico remoto?", "Sim! Oferecemos suporte técnico por WhatsApp para configurações básicas de receptores, atualizações e resolução de problemas."),
        )
    )
)

private val mockTickets = listOf(
    SupportTicket(
        id = "TK-2025-001",
        subject = "Receptor não está atualizando a lista",
        status = TicketStatus.RESOLVED,
        date = "15 Jan 2025",
        preview = "Problema resolvido via atualização de firmware v2.8"
    ),
    SupportTicket(
        id = "TK-2025-002",
        subject = "Produto chegou com embalagem danificada",
        status = TicketStatus.IN_PROGRESS,
        date = "20 Jan 2025",
        preview = "Aguardando análise do setor de logística"
    ),
    SupportTicket(
        id = "TK-2025-003",
        subject = "Dúvida sobre apontamento de antena",
        status = TicketStatus.WAITING,
        date = "22 Jan 2025",
        preview = "Aguardando sua resposta com as fotos do sistema"
    )
)

private val WHATSAPP_NUMBER get() = com.jefferson.antenas.utils.WHATSAPP_PHONE

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun SupportScreen(onBackClick: () -> Unit) {
    var activeTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        Triple(Icons.AutoMirrored.Filled.HelpCenter, "Ajuda", 0),
        Triple(Icons.Default.LiveHelp, "FAQ", 1),
        Triple(Icons.Default.ConfirmationNumber, "Chamados", 2)
    )

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = { SupportTopBar(onBackClick = onBackClick) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ── Tab Row ──────────────────────────────────────
            TabRow(
                selectedTabIndex = activeTab,
                containerColor = MidnightBlueCard,
                contentColor = SignalOrange,
                indicator = { tabPositions ->
                    Box(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[activeTab])
                            .height(3.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                            .background(SignalOrange)
                    )
                },
                divider = {
                    HorizontalDivider(color = CardBorder, thickness = 0.5.dp)
                }
            ) {
                tabs.forEach { (icon, label, index) ->
                    Tab(
                        selected = activeTab == index,
                        onClick = { activeTab = index },
                        selectedContentColor = SignalOrange,
                        unselectedContentColor = TextTertiary
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = if (activeTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // ── Tab Content ──────────────────────────────────
            when (activeTab) {
                0 -> HelpTabContent()
                1 -> FaqTabContent()
                2 -> TicketsTabContent()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun SupportTopBar(onBackClick: () -> Unit) {
    Surface(color = MidnightBlueStart, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = SignalOrange)
            }
            Column {
                Text("Central de Suporte", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text("Online agora", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 1: AJUDA (Troubleshooter)
// ─────────────────────────────────────────────────────────────

@Composable
private fun HelpTabContent() {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Hero
        item { SupportHeroHeader() }

        // Contact options
        item { ContactOptionsRow() }

        // Troubleshooter header
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
                    Text("Solucionador de Problemas", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Toque para ver a solução passo a passo", color = TextTertiary, fontSize = 11.sp)
                }
            }
        }

        // Troubleshoot cards
        items(troubleshootItems) { item ->
            TroubleshootCard(
                item = item,
                isExpanded = expandedId == item.id,
                onToggle = { expandedId = if (expandedId == item.id) null else item.id },
                onWhatsApp = {
                    val msg = "Olá! Estou com o seguinte problema: *${item.title}*\n\nJá tentei as soluções do app mas o problema persiste. Podem me ajudar?"
                    WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
                }
            )
            Spacer(Modifier.height(8.dp))
        }

        // WhatsApp CTA
        item {
            Spacer(Modifier.height(8.dp))
            SupportWhatsAppCta()
        }
    }
}

@Composable
private fun SupportHeroHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SuccessGreen.copy(alpha = 0.18f),
                        MidnightBlueStart
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(listOf(SuccessGreen, SuccessGreen.copy(alpha = 0.6f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.HelpCenter,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text("Como podemos ajudar?", color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Encontre a solução ou fale com um técnico",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(Modifier.height(6.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Seg-Sex 8h–18h  •  Sáb 8h–12h",
                            color = SuccessGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactOptionsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val context = LocalContext.current
        ContactOptionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Chat,
            label = "WhatsApp",
            sublabel = "Resposta rápida",
            color = SuccessGreen,
            onClick = {
                WhatsAppHelper.openWhatsApp(
                    context, WHATSAPP_NUMBER,
                    "Olá! Vim pelo app Jefferson Antenas e preciso de suporte."
                )
            }
        )
        ContactOptionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Sensors,
            label = "Técnico",
            sublabel = "Visita presencial",
            color = SatelliteBlue,
            onClick = {
                WhatsAppHelper.openWhatsApp(
                    context, WHATSAPP_NUMBER,
                    "Olá! Gostaria de agendar uma visita técnica. Qual a disponibilidade?"
                )
            }
        )
        ContactOptionCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.ConfirmationNumber,
            label = "Chamado",
            sublabel = "Abrir ticket",
            color = SignalOrange,
            onClick = {
                WhatsAppHelper.openWhatsApp(
                    context, WHATSAPP_NUMBER,
                    "Olá! Gostaria de abrir um chamado de suporte. Pode me ajudar?"
                )
            }
        )
    }
}

@Composable
private fun ContactOptionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    sublabel: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Text(sublabel, color = TextTertiary, fontSize = 10.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TroubleshootCard(
    item: TroubleshootItem,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onWhatsApp: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) item.color.copy(alpha = 0.5f) else CardBorder,
        animationSpec = tween(300),
        label = "border"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(item.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(item.icon, null, tint = item.color, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(item.subtitle, color = TextTertiary, fontSize = 11.sp)
                    }
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = if (isExpanded) item.color else TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(280)) + fadeIn(tween(280)),
                exit = shrinkVertically(tween(240)) + fadeOut(tween(200))
            ) {
                Column {
                    HorizontalDivider(
                        color = item.color.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        item.steps.forEachIndexed { index, step ->
                            Row(
                                modifier = Modifier.padding(vertical = 5.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(item.color.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "${index + 1}",
                                        color = item.color,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(step, color = TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = onWhatsApp,
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Problema persiste? Falar com técnico", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportWhatsAppCta() {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = Color.Transparent,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(SuccessGreen.copy(alpha = 0.14f), MidnightBlueCard)
                    )
                )
                .padding(18.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Chat, null, tint = SuccessGreen, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Não encontrou a solução?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Nossa equipe responde em minutos!", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(14.dp))
                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsApp(
                            context, WHATSAPP_NUMBER,
                            "Olá! Preciso de suporte técnico pelo app Jefferson Antenas."
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Falar com Suporte no WhatsApp", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 2: FAQ
// ─────────────────────────────────────────────────────────────

@Composable
private fun FaqTabContent() {
    var expandedKey by remember { mutableStateOf<String?>(null) }

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                color = SatelliteBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, SatelliteBlue.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.LiveHelp, null, tint = SatelliteBlue, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Perguntas Frequentes — encontre respostas rápidas",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }

        faqCategories.forEach { category ->
            item {
                FaqCategoryHeader(category = category)
            }
            items(category.items) { faq ->
                val key = "${category.title}:${faq.question}"
                FaqCard(
                    item = faq,
                    isExpanded = expandedKey == key,
                    onToggle = { expandedKey = if (expandedKey == key) null else key }
                )
                Spacer(Modifier.height(6.dp))
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun FaqCategoryHeader(category: FaqCategory) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(category.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(category.icon, null, tint = category.color, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(category.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Spacer(Modifier.weight(1f))
        Surface(
            color = category.color.copy(alpha = 0.15f),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                "${category.items.size} itens",
                color = category.color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun FaqCard(item: FaqItem, isExpanded: Boolean, onToggle: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            0.5.dp,
            if (isExpanded) SignalOrange.copy(alpha = 0.4f) else CardBorder
        )
    ) {
        Column {
            Surface(
                onClick = onToggle,
                color = Color.Transparent,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        item.question,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        modifier = Modifier.weight(1f),
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = if (isExpanded) SignalOrange else TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(250)) + fadeIn(tween(250)),
                exit = shrinkVertically(tween(200)) + fadeOut(tween(180))
            ) {
                Column {
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 14.dp))
                    Text(
                        item.answer,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TAB 3: CHAMADOS (Tickets)
// ─────────────────────────────────────────────────────────────

@Composable
private fun TicketsTabContent() {
    val context = LocalContext.current

    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        // Header with new ticket button
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Meus Chamados", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Histórico de atendimentos", color = TextTertiary, fontSize = 11.sp)
                }
                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsApp(
                            context, WHATSAPP_NUMBER,
                            "Olá! Gostaria de abrir um novo chamado de suporte."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Chat, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(5.dp))
                    Text("Novo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Stats row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val stats = listOf(
                    Triple("3", "Total", CardBorder),
                    Triple("1", "Aberto", SatelliteBlue),
                    Triple("1", "Em andamento", SignalOrange),
                    Triple("1", "Resolvido", SuccessGreen)
                )
                stats.forEach { (count, label, color) ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        color = color.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(count, color = if (color == CardBorder) TextPrimary else color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            Text(label, color = TextTertiary, fontSize = 9.sp, textAlign = TextAlign.Center, lineHeight = 12.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Ticket cards
        items(mockTickets) { ticket ->
            TicketCard(ticket = ticket)
            Spacer(Modifier.height(10.dp))
        }

        // Help text
        item {
            Spacer(Modifier.height(8.dp))
            Text(
                "Para abrir um novo chamado ou acompanhar um existente, entre em contato via WhatsApp.",
                color = TextTertiary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = {
                    WhatsAppHelper.openWhatsApp(
                        context, WHATSAPP_NUMBER,
                        "Olá! Gostaria de acompanhar meu chamado de suporte."
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(46.dp),
                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = SuccessGreen)
            ) {
                Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text("Acompanhar via WhatsApp", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TicketCard(ticket: SupportTicket) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, ticket.status.color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = ticket.status.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        ticket.id,
                        color = ticket.status.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Spacer(Modifier.weight(1f))
                // Status badge
                Surface(
                    color = ticket.status.color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(ticket.status.color)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            ticket.status.label,
                            color = ticket.status.color,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(ticket.subject, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(4.dp))
            Text(ticket.preview, color = TextTertiary, fontSize = 12.sp, lineHeight = 16.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = TextTertiary, modifier = Modifier.size(11.dp))
                Spacer(Modifier.width(4.dp))
                Text(ticket.date, color = TextTertiary, fontSize = 11.sp)
            }
        }
    }
}
