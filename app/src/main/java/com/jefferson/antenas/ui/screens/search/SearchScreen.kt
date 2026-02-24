package com.jefferson.antenas.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.ProductCard
import com.jefferson.antenas.ui.componets.SearchAppBar
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextSecondary

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val allProducts by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filtra os produtos em tempo real baseado na busca
    val filteredProducts = if (searchQuery.isBlank()) {
        emptyList() // Não mostra nada se a busca estiver vazia
    } else {
        allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            SearchAppBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        when {
            searchQuery.isBlank() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = null,
                            tint = SignalOrange.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Digite para buscar produtos",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ex: Duosat, Receptor 4K, LNB...",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            filteredProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextSecondary.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhum produto encontrado",
                            color = TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tente outro termo de busca",
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
            else -> {
                LazyVerticalGrid(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filteredProducts) { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = { productToAdd -> viewModel.addToCart(productToAdd) },
                            onClick = { onProductClick(product.id) }
                        )
                    }
                }
            }
        }
    }
}
