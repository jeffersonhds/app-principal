package com.jefferson.antenas.ui.screens.orders

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.AccentPink
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.ui.theme.WarningYellow
import com.jefferson.antenas.utils.WhatsAppHelper
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel

private val WHATSAPP_NUMBER get() = com.jefferson.antenas.utils.WHATSAPP_PHONE

// ── Data Models ──────────────────────────────────────────────────────────────

enum class OrderStatus(val label: String, val icon: ImageVector, val color: Color) {
    PROCESSING("Processando", Icons.Default.HourglassEmpty, WarningYellow),
    CONFIRMED("Confirmado", Icons.Default.CheckCircle, SatelliteBlue),
    SHIPPED("Em trânsito", Icons.Default.LocalShipping, SignalOrange),
    DELIVERED("Entregue", Icons.Default.CheckCircle, SuccessGreen),
    CANCELLED("Cancelado", Icons.Default.Cancel, AccentPink)
}

data class OrderItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double
)

data class Order(
    val id: String,
    val number: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val total: Double,
    val date: String,
    val createdAtEpoch: Long = 0L,
    val estimatedDelivery: String? = null,
    val trackingCode: String? = null,
    val deliveredDate: String? = null
)

// ── Screen ───────────────────────────────────────────────────────────────────

private val activeStatuses = listOf(OrderStatus.PROCESSING, OrderStatus.CONFIRMED, OrderStatus.SHIPPED)

@Composable
fun OrdersScreen(
    onBackClick: () -> Unit,
    onShopClick: () -> Unit,
    onOrderClick: (String) -> Unit = {},
    viewModel: OrdersViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val orders by viewModel.orders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Todos", "Em andamento", "Entregues", "Cancelados")

    val filteredOrders = remember(selectedTab, orders) {
        when (selectedTab) {
            1 -> orders.filter { it.status in activeStatuses }
            2 -> orders.filter { it.status == OrderStatus.DELIVERED }
            3 -> orders.filter { it.status == OrderStatus.CANCELLED }
            else -> orders
        }
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = { OrdersTopBar(count = orders.size, onBackClick = onBackClick) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalOrange)
            }
            return@Scaffold
        }
        if (error != null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(error ?: "", color = SignalOrange, textAlign = TextAlign.Center, fontSize = 14.sp)
                    Button(
                        onClick = { viewModel.loadOrders() },
                        colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                        shape = RoundedCornerShape(12.dp)
                    ) { Text("Tentar novamente", color = Color.White, fontWeight = FontWeight.Bold) }
                }
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { OrdersStatsRow(orders = orders) }

            item {
                OrdersTabRow(
                    tabs = tabs,
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    orders = orders
                )
            }

            if (filteredOrders.isEmpty()) {
                item {
                    OrdersEmptyState(selectedTab = selectedTab, onShopClick = onShopClick)
                }
            } else {
                items(filteredOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onOrderClick = { onOrderClick(order.id) },
                        onTrackClick = {
                            val msg = "Olá! Gostaria de rastrear meu pedido *#${order.number}*. Pode me ajudar?"
                            WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
                        },
                        onSupportClick = {
                            val msg = "Olá! Tenho uma dúvida sobre o pedido *#${order.number}*."
                            WhatsAppHelper.openWhatsApp(context, WHATSAPP_NUMBER, msg)
                        }
                    )
                }
            }

            item { OrdersWhatsAppCta() }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun OrdersTopBar(count: Int, onBackClick: () -> Unit) {
    Surface(color = MidnightBlueStart, shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = SignalOrange)
            }
            Column {
                Text(
                    "Meus Pedidos",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "$count pedido${if (count != 1) "s" else ""} no total",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
    }
}

// ── Stats Row ────────────────────────────────────────────────────────────────

@Composable
private fun OrdersStatsRow(orders: List<Order>) {
    val totalOrders = orders.size
    val activeOrders = orders.count { it.status in activeStatuses }
    val deliveredOrders = orders.count { it.status == OrderStatus.DELIVERED }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OrderStatCard(
            modifier = Modifier.weight(1f),
            label = "Total",
            value = "$totalOrders",
            color = SatelliteBlue,
            icon = Icons.Default.ShoppingBag
        )
        OrderStatCard(
            modifier = Modifier.weight(1f),
            label = "Em andamento",
            value = "$activeOrders",
            color = SignalOrange,
            icon = Icons.Default.LocalShipping
        )
        OrderStatCard(
            modifier = Modifier.weight(1f),
            label = "Entregues",
            value = "$deliveredOrders",
            color = SuccessGreen,
            icon = Icons.Default.CheckCircle
        )
    }
}

@Composable
private fun OrderStatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    color: Color,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        color = CardGradientStart,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                value,
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
            Text(
                label,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

// ── Tab Row ──────────────────────────────────────────────────────────────────

@Composable
private fun OrdersTabRow(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    orders: List<Order>
) {
    val badgeCounts = listOf(
        orders.size,
        orders.count { it.status in activeStatuses },
        orders.count { it.status == OrderStatus.DELIVERED },
        orders.count { it.status == OrderStatus.CANCELLED }
    )

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = MidnightBlueStart,
        contentColor = SignalOrange,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            if (selectedTab < tabPositions.size) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[selectedTab])
                        .height(3.dp)
                        .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        .background(SignalOrange)
                )
            }
        },
        divider = { HorizontalDivider(color = CardBorder) }
    ) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        tab,
                        color = if (selectedTab == index) SignalOrange else TextSecondary,
                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp
                    )
                    if (badgeCounts[index] > 0) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (selectedTab == index) SignalOrange
                                    else TextTertiary.copy(alpha = 0.4f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${badgeCounts[index]}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Order Card ───────────────────────────────────────────────────────────────

@Composable
private fun OrderCard(
    order: Order,
    onOrderClick: () -> Unit,
    onTrackClick: () -> Unit,
    onSupportClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val borderColor by animateColorAsState(
        targetValue = if (expanded) order.status.color.copy(alpha = 0.45f) else CardBorder,
        animationSpec = tween(300),
        label = "border"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column {
            // ── Header (tappable) ────────────────────────────
            Surface(
                onClick = onOrderClick,
                color = Color.Transparent,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Order number badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(order.status.color.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                "#${order.number}",
                                color = order.status.color,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Status chip
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(order.status.color)
                            )
                            Spacer(Modifier.width(5.dp))
                            Text(
                                order.status.label,
                                color = order.status.color,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // First product preview
                    Text(
                        (order.items.firstOrNull()?.name ?: "Produto") +
                                if (order.items.size > 1) " +${order.items.size - 1} item(s)" else "",
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(order.date, color = TextTertiary, fontSize = 12.sp)
                        Text(
                            "R$ ${"%.2f".format(order.total).replace('.', ',')}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    // Delivery info line
                    when (order.status) {
                        OrderStatus.SHIPPED, OrderStatus.CONFIRMED -> {
                            order.estimatedDelivery?.let {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Schedule, null,
                                        tint = SignalOrange,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Previsão de entrega: $it",
                                        color = SignalOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        OrderStatus.DELIVERED -> {
                            order.deliveredDate?.let {
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.CheckCircle, null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Entregue em $it",
                                        color = SuccessGreen,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        else -> {}
                    }

                    // Expand toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (expanded) "Ver menos" else "Ver detalhes",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            null,
                            tint = TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ── Expanded Details ──────────────────────────────
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    HorizontalDivider(color = CardBorder)
                    Spacer(Modifier.height(14.dp))

                    // Item list
                    Text(
                        "Itens do pedido",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 5.dp)
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(TextTertiary.copy(alpha = 0.6f))
                                )
                                Spacer(Modifier.width(8.dp))
                                Column {
                                    Text(
                                        item.name,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (item.quantity > 1) {
                                        Text(
                                            "Qtd: ${item.quantity}",
                                            color = TextTertiary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "R$ ${"%.2f".format(item.unitPrice * item.quantity).replace('.', ',')}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Total
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total do pedido",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "R$ ${"%.2f".format(order.total).replace('.', ',')}",
                            color = SignalOrange,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Order timeline (only for non-cancelled)
                    if (order.status != OrderStatus.CANCELLED) {
                        Spacer(Modifier.height(18.dp))
                        OrderTimeline(status = order.status)
                    }

                    // Tracking code
                    order.trackingCode?.let { code ->
                        Spacer(Modifier.height(14.dp))
                        Surface(
                            color = SignalOrange.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SignalOrange.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(SignalOrange.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Info, null,
                                        tint = SignalOrange,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Column {
                                    Text("Código de rastreio", color = TextSecondary, fontSize = 11.sp)
                                    Text(code, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Action buttons
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (order.status in activeStatuses) {
                            Button(
                                onClick = onTrackClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(42.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.LocalShipping, null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Rastrear", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OutlinedButton(
                            onClick = onSupportClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp),
                            border = BorderStroke(1.dp, TextTertiary.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                        ) {
                            Icon(Icons.Default.Chat, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Suporte", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

// ── Order Timeline ───────────────────────────────────────────────────────────

@Composable
private fun OrderTimeline(status: OrderStatus) {
    val timelineSteps = listOf(
        OrderStatus.PROCESSING to "Realizado",
        OrderStatus.CONFIRMED to "Confirmado",
        OrderStatus.SHIPPED to "Em trânsito",
        OrderStatus.DELIVERED to "Entregue"
    )
    val currentIndex = timelineSteps.indexOfFirst { it.first == status }.coerceAtLeast(0)

    Column {
        Text(
            "Acompanhamento do pedido",
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))

        // Dots + connectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            timelineSteps.forEachIndexed { index, _ ->
                val isCompleted = index < currentIndex
                val isActive = index == currentIndex
                val dotColor = when {
                    isActive -> status.color
                    isCompleted -> SuccessGreen.copy(alpha = 0.8f)
                    else -> TextTertiary.copy(alpha = 0.25f)
                }
                Box(
                    modifier = Modifier
                        .size(if (isActive) 22.dp else 16.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isCompleted) {
                        Icon(
                            Icons.Default.Check, null,
                            tint = Color.White,
                            modifier = Modifier.size(9.dp)
                        )
                    }
                }
                if (index < timelineSteps.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (index < currentIndex)
                                    SuccessGreen.copy(alpha = 0.5f)
                                else
                                    TextTertiary.copy(alpha = 0.2f)
                            )
                    )
                }
            }
        }

        // Labels
        Spacer(Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            timelineSteps.forEachIndexed { index, (_, label) ->
                val isActive = index == currentIndex
                val isCompleted = index <= currentIndex
                Text(
                    label,
                    modifier = Modifier.weight(1f),
                    color = when {
                        isActive -> status.color
                        isCompleted -> SuccessGreen.copy(alpha = 0.75f)
                        else -> TextTertiary.copy(alpha = 0.45f)
                    },
                    fontSize = 10.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    textAlign = when (index) {
                        0 -> TextAlign.Start
                        timelineSteps.size - 1 -> TextAlign.End
                        else -> TextAlign.Center
                    },
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// ── Empty State ──────────────────────────────────────────────────────────────

@Composable
private fun OrdersEmptyState(selectedTab: Int, onShopClick: () -> Unit) {
    val (icon, title, subtitle) = when (selectedTab) {
        1 -> Triple(
            Icons.Default.LocalShipping,
            "Nenhum pedido em andamento",
            "Você não tem pedidos em processamento no momento."
        )
        2 -> Triple(
            Icons.Default.CheckCircle,
            "Nenhum pedido entregue",
            "Seus pedidos entregues aparecerão aqui."
        )
        3 -> Triple(
            Icons.Default.Cancel,
            "Nenhum pedido cancelado",
            "Ótima notícia! Você não tem pedidos cancelados."
        )
        else -> Triple(
            Icons.Default.ShoppingBag,
            "Nenhum pedido ainda",
            "Você ainda não realizou nenhuma compra.\nQue tal explorar nossa loja?"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(SignalOrange.copy(alpha = 0.18f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = SignalOrange.copy(alpha = 0.65f), modifier = Modifier.size(54.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            subtitle,
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        if (selectedTab == 0) {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onShopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Ir às Compras", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

// ── Bottom CTA ───────────────────────────────────────────────────────────────

@Composable
private fun OrdersWhatsAppCta() {
    val context = LocalContext.current
    Spacer(Modifier.height(16.dp))
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            SuccessGreen.copy(alpha = 0.12f),
                            SatelliteBlue.copy(alpha = 0.12f)
                        )
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
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(SuccessGreen.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Chat, null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Precisa de ajuda?",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "Fale com o suporte pelo WhatsApp",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = {
                        WhatsAppHelper.openWhatsApp(
                            context,
                            WHATSAPP_NUMBER,
                            "Olá! Preciso de ajuda com um pedido."
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Chat, null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Chat", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
