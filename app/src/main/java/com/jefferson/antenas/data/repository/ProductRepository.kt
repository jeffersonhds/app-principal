package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.Banner
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    // Usamos 'Result' para tratar erros de forma elegante (Sucesso ou Falha)
    suspend fun getProducts(): Result<List<Product>>
    suspend fun getProductById(productId: String): Result<Product?> // Adicionado
    suspend fun getBanners(): Result<List<Banner>>
}