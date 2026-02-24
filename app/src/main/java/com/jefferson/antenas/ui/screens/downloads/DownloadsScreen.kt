package com.jefferson.antenas.ui.screens.downloads

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*
// CORREÇÃO: O nome do pacote foi corrigido de 'components' para 'componets'
import com.jefferson.antenas.ui.componets.TopAppBarCustom

@Composable
fun DownloadsScreen(onBackClick: () -> Unit) {
    var expandedBrand by remember { mutableStateOf<Int?>(null) }
    val brands = listOf(
        Triple(0, "Duosat", listOf("Joy S v2.8", "Blade HD v3.1")),
        Triple(1, "AzAmerica", listOf("S1009 HD v1.9", "Champions v1.6")),
        Triple(2, "HTV", listOf("HTV 7 v4.2", "HTV 8 v1.0")),
    )

    Column(modifier = Modifier.fillMaxSize().background(MidnightBlueStart)) {
        TopAppBarCustom(title = "Downloads", onBackClick = onBackClick, showBack = true)

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(brands.size) { index ->
                val (id, name, models) = brands[index]
                Card(
                    onClick = { expandedBrand = if (expandedBrand == id) null else id },
                    colors = CardDefaults.cardColors(containerColor = CardGradientStart),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp).animateContentSize()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Icon(if (expandedBrand == id) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = SignalOrange)
                        }
                        if (expandedBrand == id) {
                            Spacer(Modifier.height(8.dp))
                            models.forEach { model ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(model, color = TextSecondary)
                                    Icon(Icons.Default.Download, null, tint = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
