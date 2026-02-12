package com.jefferson.antenas.ui.screens.services

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import java.net.URLEncoder

// Modelo de dados simples para os serviços
data class ServiceItem(val title: String, val description: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBackClick: () -> Unit) { // Adicionado onBackClick para consistência
    val context = LocalContext.current

    // Lista dos seus serviços
    val services = listOf(
        ServiceItem("Instalação Completa", "Instalação de antenas e cabeamento residencial.", Icons.Default.Settings),
        ServiceItem("Apontamento", "Reajuste de sinal para satélites (StarOne, Sky, etc).", Icons.Default.Router),
        ServiceItem("Manutenção", "Troca de conectores, cabos e reparos.", Icons.Default.Build),
        ServiceItem("Atualização", "Update de lista de canais e sistema do receptor.", Icons.Default.Tv)
    )

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            // CORREÇÃO: Usando o componente padrão e passando a função onBackClick
            TopAppBarCustom(
                title = "Nossos Serviços",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Text(
                "Escolha um serviço para orçar no WhatsApp:",
                color = TextSecondary, // CORREÇÃO: Usando cor do tema
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services.size) { index ->
                    ServiceCard(services[index]) { service ->
                        val phone = "5565992895296"
                        val message = "Olá Jefferson! Gostaria de um orçamento para: *${service.title}*"

                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${URLEncoder.encode(message, "UTF-8")}"
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Lidar com erro se o WhatsApp não estiver instalado
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceItem, onClick: (ServiceItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick(service) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard), // CORREÇÃO: Usando cor do tema
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = null,
                tint = SignalOrange, // CORREÇÃO: Usando cor do tema
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(service.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary) // CORREÇÃO
                Spacer(modifier = Modifier.height(4.dp))
                Text(service.description, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp) // CORREÇÃO
            }
        }
    }
}
