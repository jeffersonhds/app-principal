package com.jefferson.antenas.ui.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.SubcomposeAsyncImage
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.ui.componets.ProductCard
import com.jefferson.antenas.ui.componets.SearchAppBar
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SignalOrangeDark
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.utils.toCurrency

// ─────────────────────────────────────────────────────────────
// Enums
// ─────────────────────────────────────────────────────────────

private enum class SortOption(val label: String) {
    RELEVANCE("Relevância"),
    PRICE_LOW("Menor Preço"),
    PRICE_HIGH("Maior Preço"),
    NEWEST("Mais Novos"),
    DISCOUNT("Maior Desconto")
}

private enum class SearchState { EMPTY, LOADING, NO_RESULTS, RESULTS }

// ─────────────────────────────────────────────────────────────
// Main Screen
// ─────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val allProducts by viewModel.products.collectAsState()
    val isLoadingProducts by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var sortOption by remember { mutableStateOf(SortOption.RELEVANCE) }
    var showSortMenu by remember { mutableStateOf(false) }

    val recentSearches = remember {
        mutableStateListOf("Receptor 4K", "Antena Banda Ku", "LNB Duplo")
    }

    val trendingSearches = listOf(
        "Duosat", "Antena Digital", "Cabo HDMI 4K", "Receptor HD", "LNB Universal", "Sky HD"
    )

    val categoryIconMap: Map<String, ImageVector> = mapOf(
        "antena" to Icons.Default.Satellite,
        "receptor" to Icons.Default.Tv,
        "cabo" to Icons.Default.Cable,
        "instalação" to Icons.Default.Build,
        "acessório" to Icons.Default.Devices,
    )

    val allCategories = remember(allProducts) {
        allProducts.mapNotNull { it.category }.distinct().sorted()
    }

    val filteredProducts = remember(searchQuery, selectedCategory, sortOption, allProducts) {
        if (searchQuery.isBlank()) emptyList()
        else allProducts
            .filter { product ->
                val matchesQuery =
                    product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true) ||
                    product.category?.contains(searchQuery, ignoreCase = true) == true
                val matchesCategory =
                    selectedCategory == null || product.category == selectedCategory
                matchesQuery && matchesCategory
            }
            .let { list ->
                when (sortOption) {
                    SortOption.PRICE_LOW -> list.sortedBy { it.getDiscountedPrice() }
                    SortOption.PRICE_HIGH -> list.sortedByDescending { it.getDiscountedPrice() }
                    SortOption.NEWEST -> list.sortedByDescending { it.isNew == true }
                    SortOption.DISCOUNT -> list.sortedByDescending { it.discount ?: 0 }
                    else -> list
                }
            }
    }

    val screenState = when {
        searchQuery.isBlank() -> SearchState.EMPTY
        isLoadingProducts -> SearchState.LOADING
        filteredProducts.isEmpty() -> SearchState.NO_RESULTS
        else -> SearchState.RESULTS
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            SearchAppBar(
                searchQuery = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    if (query.isNotBlank()) selectedCategory = null
                },
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = screenState,
            transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            label = "SearchStateTransition"
        ) { state ->
            when (state) {
                SearchState.EMPTY -> SearchEmptyContent(
                    recentSearches = recentSearches,
                    trendingSearches = trendingSearches,
                    categories = allCategories,
                    categoryIconMap = categoryIconMap,
                    onSearchTerm = { searchQuery = it },
                    onRemoveRecent = { recentSearches.remove(it) }
                )
                SearchState.LOADING -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = SignalOrange)
                }
                SearchState.NO_RESULTS -> SearchNoResultsContent(
                    query = searchQuery,
                    trendingSearches = trendingSearches,
                    suggestedProducts = allProducts.take(6),
                    onSuggestionClick = { searchQuery = it },
                    onProductClick = onProductClick,
                    onAddToCart = { viewModel.addToCart(it) }
                )
                SearchState.RESULTS -> SearchResultsContent(
                    query = searchQuery,
                    products = filteredProducts,
                    allCategories = allCategories,
                    selectedCategory = selectedCategory,
                    sortOption = sortOption,
                    showSortMenu = showSortMenu,
                    onCategorySelect = { cat ->
                        selectedCategory = when {
                            cat.isEmpty() -> null
                            selectedCategory == cat -> null
                            else -> cat
                        }
                    },
                    onSortClick = { showSortMenu = true },
                    onSortDismiss = { showSortMenu = false },
                    onSortSelect = { sortOption = it; showSortMenu = false },
                    onProductClick = onProductClick,
                    onAddToCart = { viewModel.addToCart(it) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATE: EMPTY (no query typed yet)
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchEmptyContent(
    recentSearches: List<String>,
    trendingSearches: List<String>,
    categories: List<String>,
    categoryIconMap: Map<String, ImageVector>,
    onSearchTerm: (String) -> Unit,
    onRemoveRecent: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {

        // ── Recent searches ──────────────────────────────────
        if (recentSearches.isNotEmpty()) {
            SearchSectionHeader(
                icon = Icons.Default.History,
                title = "Buscas Recentes",
                iconTint = TextSecondary
            )
            recentSearches.forEach { term ->
                RecentSearchRow(
                    term = term,
                    onClick = { onSearchTerm(term) },
                    onRemove = { onRemoveRecent(term) }
                )
            }
            HorizontalDivider(
                color = CardBorder,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Trending searches ────────────────────────────────
        SearchSectionHeader(
            icon = Icons.Default.TrendingUp,
            title = "Em Alta 🔥",
            iconTint = SignalOrange
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingSearches) { term ->
                TrendingChip(text = term, onClick = { onSearchTerm(term) })
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Categories ───────────────────────────────────────
        if (categories.isNotEmpty()) {
            SearchSectionHeader(
                icon = Icons.Default.GridView,
                title = "Explorar por Categoria",
                iconTint = SatelliteBlue
            )
            Spacer(Modifier.height(4.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories) { cat ->
                    val icon = categoryIconMap.entries
                        .firstOrNull { cat.contains(it.key, ignoreCase = true) }?.value
                        ?: Icons.Default.Category
                    CategoryCard(
                        name = cat.replaceFirstChar { it.uppercase() },
                        icon = icon,
                        onClick = { onSearchTerm(cat) }
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Search tip ───────────────────────────────────────
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            color = SatelliteBlue.copy(alpha = 0.08f),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, SatelliteBlue.copy(alpha = 0.25f))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SignalOrange.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        null,
                        tint = SignalOrange,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Dica de busca",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Tente buscar por marca, modelo ou categoria.\nEx: Duosat, Receptor 4K, Antena Ku",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Popular categories cards row ─────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PopularSearchCard(
                modifier = Modifier.weight(1f),
                emoji = "📡",
                label = "Antenas",
                subtitle = "Banda Ku, C, DTH",
                gradient = Brush.verticalGradient(
                    listOf(SatelliteBlue.copy(alpha = 0.3f), SatelliteBlue.copy(alpha = 0.1f))
                ),
                onClick = { onSearchTerm("Antena") }
            )
            PopularSearchCard(
                modifier = Modifier.weight(1f),
                emoji = "📺",
                label = "Receptores",
                subtitle = "4K, Full HD, HD",
                gradient = Brush.verticalGradient(
                    listOf(SignalOrange.copy(alpha = 0.3f), SignalOrange.copy(alpha = 0.1f))
                ),
                onClick = { onSearchTerm("Receptor") }
            )
            PopularSearchCard(
                modifier = Modifier.weight(1f),
                emoji = "🔌",
                label = "Cabos",
                subtitle = "HDMI, Coaxial",
                gradient = Brush.verticalGradient(
                    listOf(SuccessGreen.copy(alpha = 0.3f), SuccessGreen.copy(alpha = 0.1f))
                ),
                onClick = { onSearchTerm("Cabo") }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATE: NO RESULTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchNoResultsContent(
    query: String,
    trendingSearches: List<String>,
    suggestedProducts: List<Product>,
    onSuggestionClick: (String) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        // ── Big icon + message ───────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(ErrorRed.copy(alpha = 0.18f), Color.Transparent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    null,
                    tint = ErrorRed.copy(alpha = 0.75f),
                    modifier = Modifier.size(52.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(
                "Nenhum resultado para",
                color = TextSecondary,
                fontSize = 15.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "\"$query\"",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "Verifique a ortografia ou tente outros termos.",
                color = TextTertiary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(32.dp))
        HorizontalDivider(color = CardBorder, modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(Modifier.height(20.dp))

        // ── Suggestion chips ─────────────────────────────────
        SearchSectionHeader(
            icon = Icons.Default.Search,
            title = "Tente buscar por:",
            iconTint = SignalOrange
        )
        Spacer(Modifier.height(4.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(trendingSearches) { term ->
                TrendingChip(text = term, onClick = { onSuggestionClick(term) })
            }
        }

        // ── Suggested products ───────────────────────────────
        if (suggestedProducts.isNotEmpty()) {
            Spacer(Modifier.height(28.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(4.dp, 20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(SignalOrange)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    "Você também pode gostar",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(suggestedProducts) { product ->
                    SuggestedProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { onAddToCart(product) }
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// STATE: RESULTS
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchResultsContent(
    query: String,
    products: List<Product>,
    allCategories: List<String>,
    selectedCategory: String?,
    sortOption: SortOption,
    showSortMenu: Boolean,
    onCategorySelect: (String) -> Unit,
    onSortClick: () -> Unit,
    onSortDismiss: () -> Unit,
    onSortSelect: (SortOption) -> Unit,
    onProductClick: (String) -> Unit,
    onAddToCart: (Product) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // ── Results info bar ─────────────────────────────────
        Surface(
            color = MidnightBlueCard,
            shadowElevation = 2.dp
        ) {
            Column {
                // Count + sort row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: result count + badge
                    Surface(
                        color = SignalOrange.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${products.size}",
                            color = SignalOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "resultado${if (products.size != 1) "s" else ""} para ",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Text(
                        "\"$query\"",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    // Sort button
                    Box {
                        Surface(
                            onClick = onSortClick,
                            color = MidnightBlueStart,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Sort,
                                    null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    sortOption.label,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = onSortDismiss,
                            containerColor = MidnightBlueCard
                        ) {
                            SortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            option.label,
                                            color = if (sortOption == option) SignalOrange else TextPrimary,
                                            fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 14.sp
                                        )
                                    },
                                    onClick = { onSortSelect(option) },
                                    leadingIcon = if (sortOption == option) {
                                        {
                                            Icon(
                                                Icons.Default.Check,
                                                null,
                                                tint = SignalOrange,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null
                                )
                            }
                        }
                    }
                }

                // ── Category filter chips ────────────────────
                if (allCategories.isNotEmpty()) {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { onCategorySelect("") },
                                label = { Text("Todos", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SignalOrange,
                                    selectedLabelColor = MidnightBlueStart,
                                    containerColor = MidnightBlueCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = selectedCategory == null,
                                    selectedBorderColor = SignalOrange,
                                    borderColor = CardBorder
                                )
                            )
                        }
                        items(allCategories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { onCategorySelect(cat) },
                                label = {
                                    Text(
                                        cat.replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SignalOrange,
                                    selectedLabelColor = MidnightBlueStart,
                                    containerColor = MidnightBlueCard,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    selectedBorderColor = SignalOrange,
                                    borderColor = CardBorder
                                )
                            )
                        }
                    }
                }
            }
        }

        // ── Product grid ─────────────────────────────────────
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize(),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = { onAddToCart(it) },
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Shared sub-composables
// ─────────────────────────────────────────────────────────────

@Composable
private fun SearchSectionHeader(
    icon: ImageVector,
    title: String,
    iconTint: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            title,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RecentSearchRow(
    term: String,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.History, null, tint = TextTertiary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(12.dp))
        Text(term, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Default.Close, null, tint = TextTertiary, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun TrendingChip(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = MidnightBlueCard,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, SignalOrange.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.TrendingUp,
                null,
                tint = SignalOrange,
                modifier = Modifier.size(13.dp)
            )
            Spacer(Modifier.width(5.dp))
            Text(text, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun CategoryCard(
    name: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.width(96.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(SatelliteBlue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = SatelliteBlue, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(
                name,
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun PopularSearchCard(
    modifier: Modifier = Modifier,
    emoji: String,
    label: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(vertical = 16.dp, horizontal = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(emoji, fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    label,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    color = TextTertiary,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SuggestedProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = MidnightBlueCard,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.width(148.dp)
    ) {
        Column {
            // Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(CardGradientStart)
            ) {
                SubcomposeAsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    loading = {
                        Box(Modifier.fillMaxSize().background(MidnightBlueCard))
                    },
                    error = {
                        Box(
                            Modifier
                                .fillMaxSize()
                                .background(MidnightBlueCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.BrokenImage,
                                null,
                                tint = TextTertiary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                )
                // Discount badge
                if (product.discount != null && product.discount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp),
                        color = ErrorRed,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "-${product.discount}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                // New badge
                if (product.isNew == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        color = SignalOrange,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            "NOVO",
                            color = MidnightBlueStart,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Info
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    product.name,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    product.getDiscountedPrice().toCurrency(),
                    color = SignalOrange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(7.dp))
                Button(
                    onClick = onAddToCart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SignalOrange.copy(alpha = 0.18f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.AddShoppingCart,
                        null,
                        tint = SignalOrange,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Adicionar",
                        color = SignalOrange,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
