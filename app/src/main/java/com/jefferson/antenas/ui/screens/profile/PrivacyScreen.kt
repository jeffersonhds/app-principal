package com.jefferson.antenas.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueEnd
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary

@Composable
fun PrivacyScreen(onBackClick: () -> Unit) {
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
                "Termos e Privacidade",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            PrivacySection(
                title = "1. Coleta de Dados",
                body = "Coletamos apenas os dados necessários para o funcionamento do aplicativo: nome, e-mail e histórico de pedidos. Esses dados são armazenados de forma segura no Firebase, serviço da Google, com criptografia em trânsito e em repouso."
            )
            PrivacySection(
                title = "2. Uso das Informações",
                body = "Suas informações são utilizadas exclusivamente para:\n• Processamento de pedidos e pagamentos\n• Comunicação sobre o status dos pedidos\n• Programa de pontos e fidelidade\n• Melhoria contínua do aplicativo"
            )
            PrivacySection(
                title = "3. Compartilhamento",
                body = "Não vendemos nem compartilhamos seus dados pessoais com terceiros, exceto quando necessário para processamento de pagamentos (Stripe) ou quando exigido por lei."
            )
            PrivacySection(
                title = "4. Pagamentos",
                body = "Os pagamentos são processados pela Stripe, empresa certificada PCI DSS. Não armazenamos dados de cartão de crédito em nossos servidores."
            )
            PrivacySection(
                title = "5. Seus Direitos",
                body = "Você pode a qualquer momento:\n• Solicitar a exclusão da sua conta e dados\n• Editar seu nome e informações de perfil\n• Entrar em contato via WhatsApp para dúvidas sobre privacidade"
            )
            PrivacySection(
                title = "6. Contato",
                body = "Para dúvidas sobre privacidade ou solicitação de exclusão de dados, entre em contato pelo WhatsApp: +55 (65) 9 9289-5296."
            )
            PrivacySection(
                title = "7. Atualizações",
                body = "Estes termos podem ser atualizados periodicamente. Em caso de mudanças significativas, você será notificado pelo aplicativo. O uso continuado do app após a notificação implica aceitação dos novos termos."
            )

            Spacer(Modifier.height(8.dp))
            Text(
                "Última atualização: Janeiro de 2025",
                color = com.jefferson.antenas.ui.theme.TextTertiary,
                fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

@Composable
private fun PrivacySection(title: String, body: String) {
    Spacer(Modifier.height(12.dp))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = SignalOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(6.dp))
            Text(body, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}
