package com.jefferson.antenas.data.repository

import android.util.Log
import com.jefferson.antenas.data.local.AppDatabase
import com.jefferson.antenas.data.model.Banner
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.remote.JeffersonApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: JeffersonApi,
    private val database: AppDatabase
) : ProductRepository {

    private val productDao = database.productDao()

    // ✅ RETORNA PRODUTOS COM CACHE INTELIGENTE
    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val startTime = System.currentTimeMillis()
            Log.d("ProductRepository", "🌐 Buscando produtos da API...")

            // 1. Tenta buscar da API
            val productsFromApi = api.getProducts()
            val apiTime = System.currentTimeMillis() - startTime
            Log.d("ProductRepository", "✅ API respondeu em ${apiTime}ms com ${productsFromApi.size} produtos")

            // 2. Salva no banco local (cache)
            Log.d("ProductRepository", "💾 Salvando ${productsFromApi.size} produtos no banco local...")
            productDao.insertProducts(productsFromApi)
            Log.d("ProductRepository", "✅ Produtos salvos no banco")

            Result.success(productsFromApi)
        } catch (e: Exception) {
            // ❌ API falhou, tenta buscar do cache local
            Log.e("ProductRepository", "❌ Erro na API: ${e.message}")
            Log.d("ProductRepository", "📦 Tentando carregar do cache local...")

            return try {
                val localProducts = productDao.getAllProducts()
                // Converte Flow em List (pega o valor primeiro)
                var cachedList = emptyList<Product>()
                localProducts.collect { products ->
                    cachedList = products
                }

                if (cachedList.isNotEmpty()) {
                    Log.d("ProductRepository", "✅ ${cachedList.size} produtos carregados do cache")
                    Result.success(cachedList)
                } else {
                    Log.e("ProductRepository", "❌ Sem internet e sem cache")
                    Result.failure(Exception("Sem conexão e sem dados em cache"))
                }
            } catch (e: Exception) {
                Log.e("ProductRepository", "❌ Erro ao acessar cache: ${e.message}")
                Result.failure(e)
            }
        }
    }

    // ✅ BUSCA UM PRODUTO ESPECIFICO
    override suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            Log.d("ProductRepository", "🔍 Buscando produto $productId...")

            // 1. Tenta da API
            val product = api.getProductById(productId)

            if (product != null) {
                // 2. Salva no cache
                productDao.insertProduct(product)
                Log.d("ProductRepository", "✅ Produto $productId carregado e cacheado")
                Result.success(product)
            } else {
                // 3. Se não encontrou na API, busca no cache
                val cachedProduct = productDao.getProductById(productId)
                Log.d("ProductRepository", "✅ Produto $productId carregado do cache")
                Result.success(cachedProduct)
            }
        } catch (e: Exception) {
            // ❌ API falhou, tenta cache
            Log.e("ProductRepository", "❌ Erro na API para produto $productId: ${e.message}")
            return try {
                val cachedProduct = productDao.getProductById(productId)
                if (cachedProduct != null) {
                    Log.d("ProductRepository", "✅ Produto $productId carregado do cache")
                    Result.success(cachedProduct)
                } else {
                    Result.failure(Exception("Produto não encontrado"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    // ✅ BUSCA BANNERS (não faz cache por enquanto)
    override suspend fun getBanners(): Result<List<Banner>> {
        return try {
            val response = api.getBanners()
            Result.success(response)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    // ✅ NOVO: Retorna produtos como Flow (para observar mudanças)
    fun getProductsAsFlow(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    // ✅ NOVO: Retorna produtos com desconto
    fun getProductsWithDiscount(): Flow<List<Product>> {
        return productDao.getProductsWithDiscount()
    }

    // ✅ NOVO: Retorna produtos novos
    fun getNewProducts(): Flow<List<Product>> {
        return productDao.getNewProducts()
    }

    // ✅ NOVO: Limpar cache manualmente
    suspend fun clearCache() {
        Log.d("ProductRepository", "🗑️ Limpando cache...")
        productDao.clearAllProducts()
        Log.d("ProductRepository", "✅ Cache limpo")
    }
}