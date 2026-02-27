package com.jefferson.antenas.ui.screens.store

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.*
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun StoreScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onServicesClick: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf("popular") }
    var showToast by remember { mutableStateOf(false) }
    var displayedCount by remember { mutableIntStateOf(20) }

    LaunchedEffect(searchQuery, selectedCategory, selectedSort) {
        displayedCount = 20
    }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    val categories = remember(products) {
        products.mapNotNull { it.category }.distinct().sorted()
    }

    val categoryFilters = remember(categories) {
        categories.map { FilterOption(it, it) }
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedSort) {
        var filtered = products

        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedCategory != null) {
            filtered = filtered.filter { it.category == selectedCategory }
        }

        filtered = when (selectedSort) {
            "preco_baixo" -> filtered.sortedBy { it.getDiscountedPrice() }
            "preco_alto" -> filtered.sortedByDescending { it.getDiscountedPrice() }
            "novo" -> filtered.sortedByDescending { it.isNew }
            "desconto" -> filtered.sortedByDescending { it.discount ?: 0 }
            else -> filtered
        }

        filtered
    }

    val sortOptions = listOf(
        SortOption("popular", "Mais Popular"),
        SortOption("novo", "Mais Novo"),
        SortOption("desconto", "Maior Desconto"),
        SortOption("preco_baixo", "Menor Preço"),
        SortOption("preco_alto", "Maior Preço")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MidnightBlueStart,
            topBar = {
                TopAppBarCustom(
                    title = "Loja",
                    onBackClick = onBackClick,
                    actions = {
                        CartAppBarAction(cartCount = cartCount, onCartClick = onCartClick)
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MidnightBlueStart),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {

                // ── Barra de busca ──────────────────────────────────────────
                item {
                    StoreSearchBar(
                        searchQuery = searchQuery,
                        onSearchChange = { searchQuery = it },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }

                // ── Carrossel de promoções ──────────────────────────────────
                item {
                    PromoCarousel()
                }

                // ── Benefícios (3 chips) ────────────────────────────────────
                item {
                    BenefitsRow()
                }

                // ── Filtros de categoria ────────────────────────────────────
                if (categories.isNotEmpty()) {
                    item {
                        HorizontalFilters(
                            filters = categoryFilters,
                            selectedFilter = selectedCategory,
                            onFilterSelected = { selectedCategory = it }
                        )
                    }
                }

                // ── Indicador de filtros ativos ─────────────────────────────
                if (selectedCategory != null || searchQuery.isNotBlank()) {
                    item {
                        ActiveFiltersIndicator(
                            hasActiveFilters = true,
                            filterCount = (if (selectedCategory != null) 1 else 0) +
                                    (if (searchQuery.isNotBlank()) 1 else 0),
                            onClearFilters = {
                                selectedCategory = null
                                searchQuery = ""
                            }
                        )
                    }
                }

                // ── Serviços (card discreto) ────────────────────────────────
                item {
                    ServicesLinkCard(onClick = onServicesClick)
                }

                // ── Contagem + ordenação ────────────────────────────────────
                item {
                    SortAndResultsRow(
                        filteredCount = filteredProducts.size,
                        sortOptions = sortOptions,
                        selectedSort = selectedSort,
                        onSortSelected = { selectedSort = it }
                    )
                }

                // ── Grid de produtos ────────────────────────────────────────
                when {
                    isLoading -> {
                        items(4) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ShimmerProductCard(modifier = Modifier.weight(1f))
                                ShimmerProductCard(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    errorMessage != null -> {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 48.dp, horizontal = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = SignalOrange.copy(alpha = 0.6f),
                                    modifier = Modifier.size(52.dp)
                                )
                                Text(
                                    errorMessage!!,
                                    color = TextSecondary,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { viewModel.retry() },
                                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Tentar novamente",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    filteredProducts.isEmpty() -> {
                        item {
                            EmptyStoreState(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            )
                        }
                    }

                    else -> {
                        items(filteredProducts.take(displayedCount).chunked(2)) { rowProducts ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ProductCard(
                                    product = rowProducts[0],
                                    onAddToCart = {
                                        viewModel.addToCart(it)
                                        showToast = true
                                    },
                                    onClick = { onProductClick(rowProducts[0].id) },
                                    modifier = Modifier.weight(1f)
                                )
                                if (rowProducts.size > 1) {
                                    ProductCard(
                                        product = rowProducts[1],
                                        onAddToCart = {
                                            viewModel.addToCart(it)
                                            showToast = true
                                        },
                                        onClick = { onProductClick(rowProducts[1].id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (filteredProducts.size > displayedCount) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    OutlinedButton(
                                        onClick = { displayedCount += 20 },
                                        border = BorderStroke(1.dp, SignalOrange.copy(alpha = 0.6f)),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange)
                                    ) {
                                        Text(
                                            "Ver mais ${filteredProducts.size - displayedCount} produtos",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Toast de confirmação ────────────────────────────────────────────
        ModernSuccessToast(
            visible = showToast,
            message = "Item adicionado!",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}