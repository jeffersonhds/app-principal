package com.jefferson.antenas.ui.screens.home

import com.jefferson.antenas.data.model.Banner
import com.jefferson.antenas.data.model.Product

// Sealed Interface é como um Enum super poderoso
sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val banners: List<Banner>,
        val featuredProducts: List<Product>, // Produtos em destaque
        val newArrivals: List<Product>,      // Novidades
        val searchResults: List<Product> = emptyList(),
        val isSearching: Boolean = false,
        val categories: List<String> = emptyList(),
        val selectedCategory: String? = null
    ) : HomeUiState
    data class Error(val message: String) : HomeUiState
}