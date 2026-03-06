package com.jefferson.antenas.ui.componets

// ✅ Modelo único para ReviewItem - consolidado de todas as duplicatas
data class ReviewItem(
    val id: String,
    val author: String,
    val rating: Int,
    val text: String,
    val date: String
)

// Modelo para os itens do carrossel de banners
data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val buttonText: String,
    val icon: String = "📡"
)