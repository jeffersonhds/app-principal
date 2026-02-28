package com.jefferson.antenas.ui.screens.downloads

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.AccentPink
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientEnd
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

private data class FirmwareItem(
    val model: String,
    val version: String,
    val fileSize: String,
    val downloads: String,
    val date: String,
    val isNew: Boolean = false,
    val isPopular: Boolean = false
)

private data class BrandDownload(
    val id: Int,
    val name: String,
    val emoji: String,
    val description: String,
    val accentColor: Color,
    val items: List<FirmwareItem>
)

// ─────────────────────────────────────────────────────────────
// Data
// ─────────────────────────────────────────────────────────────

private val downloadBrands = listOf(
    BrandDownload(
        id = 0,
        name = "Duosat",
        emoji = "📡",
        description = "Receptores e decodificadores satellite",
        accentColor = SatelliteBlue,
        items = listOf(
            FirmwareItem("Prodigy Maxx", "v4.0", "22.1 MB", "2.3k", "Fev 2025", isNew = true, isPopular = true),
            FirmwareItem("Joy S", "v2.8", "12.4 MB", "1.2k", "Jan 2025", isPopular = true),
            FirmwareItem("Blade HD", "v3.1", "18.6 MB", "987", "Dez 2024"),
            FirmwareItem("Trend Lite", "v1.5", "9.2 MB", "654", "Nov 2024"),
        )
    ),
    BrandDownload(
        id = 1,
        name = "AzAmerica",
        emoji = "🛰️",
        description = "Set-top boxes e receptores digitais",
        accentColor = AccentPink,
        items = listOf(
            FirmwareItem("F928 Plus", "v2.2", "19.7 MB", "1.1k", "Fev 2025", isNew = true),
            FirmwareItem("S1009 HD", "v1.9", "15.8 MB", "876", "Jan 2025"),
            FirmwareItem("Champions", "v1.6", "11.3 MB", "432", "Dez 2024"),
        )
    ),
    BrandDownload(
        id = 2,
        name = "HTV",
        emoji = "📺",
        description = "Smart boxes Android TV",
        accentColor = SuccessGreen,
        items = listOf(
            FirmwareItem("HTV 8", "v1.0", "380 MB", "1.8k", "Fev 2025", isNew = true, isPopular = true),
            FirmwareItem("HTV 7", "v4.2", "340 MB", "3.2k", "Jan 2025", isPopular = true),
            FirmwareItem("HTV 5+", "v6.1", "290 MB", "892", "Nov 2024"),
        )
    ),
    BrandDownload(
        id = 3,
        name = "Globalsat",
        emoji = "🌐",
        description = "Antenas e receptores de banda larga",
        accentColor = SignalOrange,
        items = listOf(
            FirmwareItem("GS180 Pro", "v1.3", "10.2 MB", "231", "Jan 2025", isNew = true),
            FirmwareItem("GS120", "v2.0", "8.5 MB", "445", "Dez 2024"),
        )
    ),
    BrandDownload(
        id = 4,
        name = "Freesky",
        emoji = "🔧",
        description = "Receptores e equipamentos",
        accentColor = WarningYellow,
        items = listOf(
            FirmwareItem("Freesatellite", "v2.1", "16.8 MB", "389", "Fev 2025", isNew = true),
            FirmwareItem("Freeduo", "v3.5", "14.6 MB", "678", "Jan 2025"),
        )
    ),
    BrandDownload(
        id = 5,
        name = "Nazabox",
        emoji = "⚡",
        description = "Receptores ultra HD e acessórios",
        accentColor = ErrorRed,
        items = listOf(
            FirmwareItem("NZ Ultra 4K", "v2.5", "28.3 MB", "512", "Fev 2025", isNew = true),
            FirmwareItem("NZ S2020", "v1.8", "17.4 MB", "344", "Jan 2025"),
        )
    ),
)

private val WHATSAPP_NUMBER get() = com.jefferson.antenas.utils.WHATSAPP_PHONE

// ─────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun DownloadsScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current
    var expandedId by remember { mutableStateOf<Int?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredBrands = remember(searchQuery) {
        if (searchQuery.isBlank()) downloadBrands
        else downloadBrands.filter { brand ->
            brand.name.contains(searchQuery, ignoreCase = true) ||
            brand.items.any { it.model.contains(searchQuery, ignoreCase = true) }
        }
    }

    val totalFirmwares = downloadBrands.sumOf { it.items.size }
    val totalBrands = downloadBrands.size

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            DownloadsTopBar(onBackClick = onBackClick)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // ── Hero header ──────────────────────────────────
            item {
                DownloadsHeroHeader(
                    totalBrands = totalBrands,
                    totalFirmwares = totalFirmwares
                )
            }

            // ── Alert card ───────────────────────────────────
            item {
                DownloadsAlertCard()
                Spacer(Modifier.height(4.dp))
            }

            // ── Search bar ───────────────────────────────────
            item {
                DownloadsSearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it }
                )
                Spacer(Modifier.height(8.dp))
            }

            // ── Section header ───────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp, 18.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SignalOrange)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (searchQuery.isBlank()) "Todas as Marcas"
                        else "${filteredBrands.size} resultado(s) para \"$searchQuery\"",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            // ── Brand cards ──────────────────────────────────
            if (filteredBrands.isEmpty()) {
                item { SearchEmptyResult(query = searchQuery) }
            } else {
                items(filteredBrands) { brand ->
                    BrandCard(
                        brand = brand,
                        isExpanded = expandedId == brand.id,
                        onToggle = {
                            expandedId = if (expandedId == brand.id) null else brand.id
                        },
                        onRequestDownload = { model, version ->
                            val msg = "Olá! Gostaria de solicitar o firmware:\n\n" +
                                "📟 *Marca:* ${brand.name}\n" +
                                "📱 *Modelo:* $model\n" +
                                "🔖 *Versão:* $version\n\n" +
                                "Por favor, pode me enviar o link de download?"
                            WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
                        }
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }

            // ── Help CTA ─────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                DownloadsHelpCard(
                    onWhatsAppClick = {
                        val msg = "Olá! Preciso de ajuda para encontrar o firmware do meu receptor. Pode me ajudar?"
                        WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
                    }
                )
            }

            // ── Disclaimer ───────────────────────────────────
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "⚠️ Jefferson Antenas não se responsabiliza por danos causados por atualizações incorretas. Realize o procedimento por sua conta e risco ou solicite suporte técnico.",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Top Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun DownloadsTopBar(onBackClick: () -> Unit) {
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
                "Central de Downloads",
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
private fun DownloadsHeroHeader(totalBrands: Int, totalFirmwares: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        SatelliteBlue.copy(alpha = 0.25f),
                        MidnightBlueStart
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(SatelliteBlue, SatelliteBlue.copy(alpha = 0.6f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CloudDownload,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Central de Downloads",
                        color = TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                    Text(
                        "Firmwares e atualizações oficiais",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Stats row
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DownloadStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Groups,
                    value = "$totalBrands",
                    label = "Marcas",
                    color = SatelliteBlue
                )
                DownloadStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Folder,
                    value = "$totalFirmwares",
                    label = "Firmwares",
                    color = SignalOrange
                )
                DownloadStatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Download,
                    value = "10k+",
                    label = "Downloads",
                    color = SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun DownloadStatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
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
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(label, color = TextTertiary, fontSize = 11.sp)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Alert Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun DownloadsAlertCard() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = WarningYellow.copy(alpha = 0.08f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, WarningYellow.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Default.Warning,
                null,
                tint = WarningYellow,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 1.dp)
            )
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    "Leia antes de atualizar",
                    color = WarningYellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(4.dp))
                val tips = listOf(
                    "Faça backup das configurações antes de atualizar",
                    "Use um cabo de energia estabilizado durante o processo",
                    "Não desligue o aparelho durante a atualização",
                    "Em caso de dúvidas, solicite ajuda pelo WhatsApp"
                )
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("• ", color = WarningYellow, fontSize = 12.sp)
                        Text(tip, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Search Bar
// ─────────────────────────────────────────────────────────────

@Composable
private fun DownloadsSearchBar(query: String, onQueryChange: (String) -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = SignalOrange, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    color = TextPrimary,
                    fontSize = 14.sp
                ),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text("Buscar marca ou modelo...", color = TextTertiary, fontSize = 14.sp)
                    }
                    inner()
                }
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        null,
                        tint = TextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Brand Card
// ─────────────────────────────────────────────────────────────

@Composable
private fun BrandCard(
    brand: BrandDownload,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onRequestDownload: (model: String, version: String) -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isExpanded) brand.accentColor.copy(alpha = 0.6f) else CardBorder,
        animationSpec = tween(300),
        label = "BrandBorder"
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
            // ── Brand header (always visible) ────────────────
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
                    // Logo circle
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(brand.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(brand.emoji, fontSize = 22.sp)
                    }

                    Spacer(Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                brand.name,
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            // Model count badge
                            Surface(
                                color = brand.accentColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    "${brand.items.size} modelo${if (brand.items.size > 1) "s" else ""}",
                                    color = brand.accentColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            brand.description,
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }

                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        null,
                        tint = if (isExpanded) brand.accentColor else TextTertiary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── Firmware list (expandable) ───────────────────
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(tween(280)) + fadeIn(tween(280)),
                exit = shrinkVertically(tween(240)) + fadeOut(tween(200))
            ) {
                Column {
                    HorizontalDivider(
                        color = brand.accentColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(horizontal = 14.dp)
                    )
                    brand.items.forEachIndexed { index, item ->
                        FirmwareRow(
                            item = item,
                            accentColor = brand.accentColor,
                            onDownloadClick = {
                                onRequestDownload(item.model, item.version)
                            }
                        )
                        if (index < brand.items.lastIndex) {
                            HorizontalDivider(
                                color = CardBorder.copy(alpha = 0.5f),
                                modifier = Modifier.padding(horizontal = 14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Firmware Row
// ─────────────────────────────────────────────────────────────

@Composable
private fun FirmwareRow(
    item: FirmwareItem,
    accentColor: Color,
    onDownloadClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        // Top: model name + badges
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                item.model,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.weight(1f)
            )
            if (item.isNew) {
                Spacer(Modifier.width(6.dp))
                Surface(
                    color = SuccessGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "NOVO",
                        color = SuccessGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (item.isPopular) {
                Spacer(Modifier.width(4.dp))
                Surface(
                    color = SignalOrange.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "🔥 POPULAR",
                        color = SignalOrange,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // Meta info row
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Version chip
            Surface(
                color = accentColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    item.version,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
            // Size
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Storage,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(item.fileSize, color = TextTertiary, fontSize = 11.sp)
            }
            // Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarMonth,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(item.date, color = TextTertiary, fontSize = 11.sp)
            }
            // Downloads count
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Download,
                    null,
                    tint = TextTertiary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(item.downloads, color = TextTertiary, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(10.dp))

        // Download button
        Button(
            onClick = onDownloadClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor.copy(alpha = 0.18f)
            ),
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                null,
                tint = accentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Solicitar Download",
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Empty search result
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchEmptyResult(query: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            "Nenhum resultado para \"$query\"",
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Verifique o nome da marca ou modelo e tente novamente.",
            color = TextTertiary,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Help CTA
// ─────────────────────────────────────────────────────────────

@Composable
private fun DownloadsHelpCard(onWhatsAppClick: () -> Unit) {
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
                        listOf(SuccessGreen.copy(alpha = 0.12f), MidnightBlueCard)
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
                        Icon(
                            Icons.Default.Chat,
                            null,
                            tint = SuccessGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Não encontrou seu firmware?",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Nossa equipe pode ajudar você!",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                val benefits = listOf(
                    "✅ Suporte especializado em receptores",
                    "✅ Envio do firmware em minutos",
                    "✅ Orientação passo a passo da atualização",
                )
                benefits.forEach { b ->
                    Text(b, color = TextSecondary, fontSize = 12.sp, lineHeight = 20.sp)
                }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onWhatsAppClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
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
                        "Solicitar via WhatsApp",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
