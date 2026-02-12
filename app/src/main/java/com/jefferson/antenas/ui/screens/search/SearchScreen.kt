package com.jefferson.antenas.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.ProductCard
import com.jefferson.antenas.ui.componets.SearchAppBar
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.MidnightBlueStart

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
