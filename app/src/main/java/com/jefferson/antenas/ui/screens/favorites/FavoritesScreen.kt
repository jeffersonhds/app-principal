package com.jefferson.antenas.ui.screens.favorites

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.ui.componets.ProductCard
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.AccentPink
import com.jefferson.antenas.ui.theme.CardBorder
import com.jefferson.antenas.ui.theme.CardGradientStart
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SatelliteBlue
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.TextTertiary
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay

// ── Sort Options ─────────────────────────────────────────────────────────────

enum class FavSortOption(val label: String) {
    RELEVANCE("Relevância"),
    PRICE_LOW("Menor preço"),
    DISCOUNT("Maior desconto"),
    NEWEST("Mais novo")
}

// ── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun FavoritesScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    onShopClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    favoritesViewModel: FavoritesViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val favoriteIds by favoritesViewModel.favoriteIds.collectAsState()
    val isFavLoading by favoritesViewModel.isLoading.collectAsState()
    val syncError by favoritesViewModel.syncError.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(syncError) {
        syncError?.let { snackbarHostState.showSnackbar(it) }
    }

    var selectedFilter by remember { mutableStateOf("Todos") }
    var sortOption by remember { mutableStateOf(FavSortOption.RELEVANCE) }
    var showSortMenu by remember { mutableStateOf(false) }
    var isAddingAll by remember { mutableStateOf(false) }

    val savedProducts = products.filter { it.id in favoriteIds }

    val categories = remember(savedProducts) {
        savedProducts.mapNotNull { it.category }.distinct().take(3)
    }

    val filteredProducts = remember(savedProducts, selectedFilter, sortOption) {
        val base = when (selectedFilter) {
            "Promoção" -> savedProducts.filter { (it.discount ?: 0) > 0 }
            "Novidades" -> savedProducts.filter { it.isNew == true }
            "Todos" -> savedProducts
            else -> savedProducts.filter {
                it.category?.contains(selectedFilter, ignoreCase = true) == true
            }
        }
        when (sortOption) {
            FavSortOption.PRICE_LOW -> base.sortedBy { it.getDiscountedPrice() }
            FavSortOption.DISCOUNT -> base.sortedByDescending { it.discount ?: 0 }
            FavSortOption.NEWEST -> base.sortedByDescending { it.isNew == true }
            FavSortOption.RELEVANCE -> base
        }
    }

    val maxDiscount = savedProducts.maxOfOrNull { it.discount ?: 0 } ?: 0
    val onSaleCount = savedProducts.count { (it.discount ?: 0) > 0 }
    val suggestions = products.filter { it.id !in favoriteIds }.take(6)

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            FavoritesTopBar(count = favoriteIds.size, onBackClick = onBackClick)
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data, containerColor = MidnightBlueStart.copy(alpha = 0.95f), contentColor = TextPrimary, actionColor = SignalOrange)
            }
        }
    ) { padding ->
        if (isLoading || isFavLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = SignalOrange)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                FavoritesHeroHeader(
                    totalSaved = favoriteIds.size,
                    maxDiscount = maxDiscount,
                    onSaleCount = onSaleCount
                )
            }

            item {
                FavoritesFilterRow(
                    categories = categories,
                    selectedFilter = selectedFilter,
                    onFilterChange = { selectedFilter = it },
                    sortOption = sortOption,
                    showSortMenu = showSortMenu,
                    onSortClick = { showSortMenu = !showSortMenu },
                    onSortSelect = { sortOption = it; showSortMenu = false },
                    onDismissSort = { showSortMenu = false }
                )
            }

            if (filteredProducts.isEmpty()) {
                item {
                    FavoritesEmptyState(
                        selectedFilter = selectedFilter,
                        onShopClick = onShopClick
                    )
                }
            } else {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${filteredProducts.size} produto${if (filteredProducts.size > 1) "s" else ""} salvos",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            sortOption.label,
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }

                items(filteredProducts, key = { it.id }) { product ->
                    FavoriteProductCard(
                        product = product,
                        onProductClick = { onProductClick(product.id) },
                        onRemoveFavorite = { favoritesViewModel.toggleFavorite(product.id) },
                        onAddToCart = { viewModel.addToCart(it) }
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (!isAddingAll) {
                                isAddingAll = true
                                filteredProducts.forEach { viewModel.addToCart(it) }
                            }
                        },
                        enabled = !isAddingAll,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SatelliteBlue),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart, null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Adicionar tudo ao carrinho",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            if (suggestions.isNotEmpty()) {
                item {
                    FavoritesSuggestionsSection(
                        products = suggestions,
                        onProductClick = onProductClick,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun FavoritesTopBar(count: Int, onBackClick: () -> Unit) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Meus Favoritos",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    if (count > 0) "$count produto${if (count > 1) "s" else ""} salvos"
                    else "Nenhum produto salvo",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
            Icon(
                Icons.Default.Favorite,
                null,
                tint = AccentPink.copy(alpha = 0.7f),
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(20.dp)
            )
        }
    }
}

// ── Hero Header ──────────────────────────────────────────────────────────────

@Composable
private fun FavoritesHeroHeader(totalSaved: Int, maxDiscount: Int, onSaleCount: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(AccentPink.copy(alpha = 0.10f), Color.Transparent)
                )
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FavHeroCard(
                modifier = Modifier.weight(1f),
                emoji = "❤️",
                value = "$totalSaved",
                label = "Salvos",
                color = AccentPink
            )
            FavHeroCard(
                modifier = Modifier.weight(1f),
                emoji = "🏷️",
                value = if (maxDiscount > 0) "até $maxDiscount%" else "—",
                label = "Desconto",
                color = SignalOrange
            )
            FavHeroCard(
                modifier = Modifier.weight(1f),
                emoji = "🔥",
                value = "$onSaleCount",
                label = "Em oferta",
                color = ErrorRed
            )
        }
    }
}

@Composable
private fun FavHeroCard(
    modifier: Modifier = Modifier,
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.10f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.22f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(label, color = TextSecondary, fontSize = 11.sp)
        }
    }
}

// ── Filter Row ───────────────────────────────────────────────────────────────

@Composable
private fun FavoritesFilterRow(
    categories: List<String>,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    sortOption: FavSortOption,
    showSortMenu: Boolean,
    onSortClick: () -> Unit,
    onSortSelect: (FavSortOption) -> Unit,
    onDismissSort: () -> Unit
) {
    val filters = listOf("Todos", "Promoção", "Novidades") + categories

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Sort button
        item {
            Box {
                Surface(
                    onClick = onSortClick,
                    color = if (sortOption != FavSortOption.RELEVANCE)
                        SignalOrange.copy(alpha = 0.15f)
                    else
                        CardGradientStart,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(
                        1.dp,
                        if (sortOption != FavSortOption.RELEVANCE)
                            SignalOrange.copy(alpha = 0.5f)
                        else
                            CardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Sort, null,
                            tint = if (sortOption != FavSortOption.RELEVANCE)
                                SignalOrange
                            else
                                TextSecondary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (sortOption == FavSortOption.RELEVANCE) "Ordenar"
                            else sortOption.label,
                            color = if (sortOption != FavSortOption.RELEVANCE)
                                SignalOrange
                            else
                                TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = onDismissSort,
                    containerColor = CardGradientStart
                ) {
                    FavSortOption.entries.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    option.label,
                                    color = if (sortOption == option) SignalOrange else TextPrimary,
                                    fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            onClick = { onSortSelect(option) },
                            leadingIcon = if (sortOption == option) ({
                                Icon(
                                    Icons.Default.Check, null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(15.dp)
                                )
                            }) else null
                        )
                    }
                }
            }
        }

        // Category filter chips
        items(filters) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = {
                    Text(
                        filter,
                        fontSize = 12.sp,
                        fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = Color.White,
                    containerColor = CardGradientStart,
                    labelColor = TextSecondary
                )
            )
        }
    }
}

// ── Favorite Product Card (Horizontal) ───────────────────────────────────────

@Composable
private fun FavoriteProductCard(
    product: Product,
    onProductClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    onAddToCart: (Product) -> Unit
) {
    var isAdded by remember { mutableStateOf(false) }

    if (isAdded) {
        LaunchedEffect(isAdded) {
            delay(1800)
            isAdded = false
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
        color = CardGradientStart,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(95.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
                    .clickable { onProductClick() }
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if ((product.discount ?: 0) > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(ErrorRed)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "-${product.discount}%",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                if (product.isNew == true) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(SignalOrange)
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            "NOVO",
                            color = MidnightBlueStart,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Category
                product.category?.let { cat ->
                    Text(cat, color = TextTertiary, fontSize = 11.sp)
                    Spacer(Modifier.height(2.dp))
                }

                // Product name
                Text(
                    product.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    modifier = Modifier.clickable { onProductClick() }
                )

                Spacer(Modifier.height(6.dp))

                // Prices
                if ((product.discount ?: 0) > 0) {
                    Text(
                        product.price.toCurrency(),
                        color = TextTertiary,
                        fontSize = 11.sp,
                        textDecoration = TextDecoration.LineThrough
                    )
                }
                Text(
                    product.getDiscountedPrice().toCurrency(),
                    color = SignalOrange,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )

                Spacer(Modifier.height(10.dp))

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (!isAdded) {
                                onAddToCart(product)
                                isAdded = true
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAdded)
                                SuccessGreen.copy(alpha = 0.2f)
                            else
                                SignalOrange
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp)
                    ) {
                        Crossfade(targetState = isAdded, label = "cartAnim") { added ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    if (added) Icons.Default.Check else Icons.Default.ShoppingCart,
                                    null,
                                    tint = if (added) SuccessGreen else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (added) "Adicionado!" else "Carrinho",
                                    color = if (added) SuccessGreen else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = onRemoveFavorite,
                        modifier = Modifier.size(36.dp),
                        border = BorderStroke(1.dp, AccentPink.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPink)
                    ) {
                        Icon(
                            Icons.Default.FavoriteBorder, null,
                            tint = AccentPink,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Suggestions Section ───────────────────────────────────────────────────────

@Composable
private fun FavoritesSuggestionsSection(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel
) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
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
                    .background(SatelliteBlue)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                "Você também pode gostar",
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(products) { product ->
                Box(modifier = Modifier.width(168.dp)) {
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onAddToCart = { viewModel.addToCart(it) }
                    )
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun FavoritesEmptyState(selectedFilter: String, onShopClick: () -> Unit) {
    val (title, subtitle) = when (selectedFilter) {
        "Promoção" -> "Nenhum favorito em promoção" to
                "Você não tem produtos salvos com desconto no momento."
        "Novidades" -> "Nenhum favorito novo" to
                "Você não tem novidades salvas."
        "Todos" -> "Nenhum favorito ainda" to
                "Explore a loja e adicione produtos aos\nseus favoritos tocando no ♡."
        else -> "Nenhum favorito em \"$selectedFilter\"" to
                "Você não tem produtos salvos nesta categoria."
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(AccentPink.copy(alpha = 0.18f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Favorite, null,
                tint = AccentPink.copy(alpha = 0.65f),
                modifier = Modifier.size(50.dp)
            )
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
        if (selectedFilter == "Todos") {
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onShopClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    Icons.Default.ShoppingBag, null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Explorar Produtos",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
