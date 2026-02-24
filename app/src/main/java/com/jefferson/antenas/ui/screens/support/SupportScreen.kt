package com.jefferson.antenas.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.WhatsAppHelper

@Composable
fun SupportScreen(
    onBackClick: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
    ) {
        TopAppBarCustom(title = "Suporte", onBackClick = onBackClick, showBack = true)

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = CardGradientStart,
            contentColor = SignalOrange,
            divider = {}
        ) {
            listOf("Ajuda", "Meus Chamados", "FAQ").forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontSize = 14.sp, fontWeight = if(activeTab == index) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = SignalOrange,
                    unselectedContentColor = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (activeTab) {
                0 -> HelpTab()
                1 -> TicketsTab()
                2 -> FaqTab()
            }
        }
    }
}

@Composable
fun HelpTab() {
    // --- LÓGICA DO WHATSAPP ---
    val context = LocalContext.current
    // IMPORTANTE: Substitua pelo seu número de telefone com código do país (ex: 55119XXXXXXXX)
    val supportPhoneNumber = "5565992895296"
    val defaultMessage = "Olá! Vim pelo app Jefferson Antenas e preciso de ajuda."

    Text("Solucionador de Problemas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    val items = listOf(
        "Sem Sinal?" to "Verifique os cabos e o apontamento da antena.",
        "Canal Codificado?" to "Verifique sua conexão com a internet e assinatura.",
        "Controle não funciona?" to "Troque as pilhas ou verifique o sensor.",
        "Imagem travando?" to "Reinicie o roteador e o receptor."
    )

    items.forEach { (title, desc) ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardGradientStart)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(desc, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    Button(
        onClick = { 
            WhatsAppHelper.openWhatsApp(context, supportPhoneNumber, defaultMessage)
        },
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Phone, contentDescription = null, tint = TextPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Falar no WhatsApp", color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TicketsTab() {
    Text("Meus Chamados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("#TK001", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SignalOrange)
                Surface(color = SuccessGreen, shape = RoundedCornerShape(4.dp)) {
                    Text("Resolvido", fontSize = 10.sp, color = TextPrimary, modifier = Modifier.padding(4.dp))
                }
            }
            Text("Receptor não atualiza", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun FaqTab() {
    Text("Perguntas Frequentes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    val faqs = listOf(
        "Qual a garantia?" to "6 meses contra defeitos de fabricação.",
        "Fazem instalação?" to "Sim, agende pelo app ou WhatsApp.",
        "Aceitam cartão?" to "Sim, parcelamos em até 12x."
    )

    faqs.forEach { (q, a) ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardGradientStart)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(q, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(a, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
