package com.jefferson.antenas.ui.componets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jefferson.antenas.ui.theme.*

@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = CardGradientStart,
        contentColor = TextSecondary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // O "SALÃO DO TESOURO" FOI ADICIONADO AQUI
        val items = listOf(
            Triple("home", "Início", Icons.Default.Home),
            Triple("store", "Loja", Icons.Default.ShoppingBag),
            Triple("downloads", "Baixar", Icons.Default.Download),
            Triple("support", "Suporte", Icons.Default.HeadsetMic)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                label = "IconScaleAnimation"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale),
                        tint = if (isSelected) SignalOrange else TextSecondary
                    )
                },
                label = {
                    Text(
                        label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SignalOrange else TextSecondary
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Mantém o comportamento de navegação padrão
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MidnightBlueEnd
                )
            )
        }
    }
}
