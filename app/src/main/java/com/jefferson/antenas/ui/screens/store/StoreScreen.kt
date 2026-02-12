package com.jefferson.antenas.ui.screens.store

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    // ✅ ESTADOS DA LOJA
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedSort by remember { mutableStateOf("popular") }
    var showToast by remember { mutableStateOf(false) }

    // ✅ AUTO-HIDE TOAST
    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    // ✅ EXTRAIR CATEGORIAS DOS PRODUTOS
    val categories = remember(products) {
        products
            .mapNotNull { it.category }
            .distinct()
            .sorted()
    }

    val categoryFilters = remember(categories) {
        categories.map { FilterOption(it, it) }
    }

    // ✅ FILTRAR E ORDENAR PRODUTOS
    val filteredProducts = remember(products, searchQuery, selectedCategory, selectedSort) {
        var filtered = products

        // Filtro por busca
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                        it.description.contains(searchQuery, ignoreCase = true)
            }
        }

        // Filtro por categoria
        if (selectedCategory != null) {
            filtered = filtered.filter { it.category == selectedCategory }
        }

        // Ordenação
        filtered = when (selectedSort) {
            "preco_baixo" -> filtered.sortedBy { it.getDiscountedPrice() }
            "preco_alto" -> filtered.sortedByDescending { it.getDiscountedPrice() }
            "novo" -> filtered.sortedByDescending { it.isNew }
            "desconto" -> filtered.sortedByDescending { it.discount ?: 0 }
            else -> filtered // "popular" (padrão)
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MidnightBlueStart)
            ) {
                // ✅ PROMOÇÃO NO TOPO
                PromotionBanner(text = "Até 25% de desconto em produtos selecionados!")

                // ✅ STATS DA LOJA
                StoreStats(
                    totalProducts = products.size,
                    totalCategories = categories.size
                )

                // ✅ FRETE GRÁTIS
                FreeShippingBanner()

                // ✅ HEADER COM BUSCA
                StoreHeader(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    onSearchClick = { }
                )

                // ✅ BOTÃO DE SERVIÇOS
                TextButton(
                    onClick = onServicesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Build, contentDescription = "Serviços", tint = SignalOrange)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Conheça nossos serviços de instalação", color = TextPrimary)
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = SignalOrange)
                    }
                }

                // ✅ FILTROS HORIZONTAIS
                if (categories.isNotEmpty()) {
                    HorizontalFilters(
                        filters = categoryFilters,
                        selectedFilter = selectedCategory,
                        onFilterSelected = { selectedCategory = it }
                    )
                }

                // ✅ INDICADOR DE FILTROS ATIVOS
                ActiveFiltersIndicator(
                    hasActiveFilters = selectedCategory != null || searchQuery.isNotBlank(),
                    filterCount = (if (selectedCategory != null) 1 else 0) + (if (searchQuery.isNotBlank()) 1 else 0),
                    onClearFilters = {
                        selectedCategory = null
                        searchQuery = ""
                    }
                )

                // ✅ ORDENAÇÃO
                SortDropdown(
                    sortOptions = sortOptions,
                    selectedSort = selectedSort,
                    onSortSelected = { selectedSort = it }
                )

                // ✅ INFO DE RESULTADOS
                ResultsInfo(
                    totalProducts = products.size,
                    filteredProducts = filteredProducts.size
                )

                // ✅ GRID DE PRODUTOS
                if (filteredProducts.isEmpty()) {
                    // Empty State
                    EmptyStoreState(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    )
                } else if (products.isEmpty()) {
                    // Loading State
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(8) {
                            ShimmerProductCard()
                        }
                    }
                } else {
                    // Produtos carregados
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredProducts) { product ->
                            ProductCard(
                                product = product,
                                onAddToCart = { productToAdd ->
                                    viewModel.addToCart(productToAdd)
                                    showToast = true
                                },
                                onClick = { onProductClick(product.id) }
                            )
                        }
                    }
                }
            }
        }

        // ✅ TOAST DE SUCESSO
        ModernSuccessToast(
            visible = showToast,
            message = "Item adicionado!",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}