package com.jefferson.antenas.data.repository

import android.util.Log
import com.jefferson.antenas.data.local.AppDatabase
import com.jefferson.antenas.data.model.Banner
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.remote.JeffersonApi
import kotlinx.coroutines.flow.firstOrNull
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val api: JeffersonApi,
    private val database: AppDatabase
) : ProductRepository {

    private val productDao = database.productDao()

    override suspend fun getProducts(): Result<List<Product>> {
        return try {
            val productsFromApi = api.getProducts()
            productDao.insertProducts(productsFromApi)
            Log.d("ProductRepository", "${productsFromApi.size} produtos carregados da API e cacheados")
            Result.success(productsFromApi)
        } catch (e: Exception) {
            val errorMsg = apiErrorMessage(e)
            Log.e("ProductRepository", "API falhou: $errorMsg — tentando cache local")
            return try {
                val cachedList = productDao.getAllProducts().firstOrNull() ?: emptyList()
                if (cachedList.isNotEmpty()) {
                    Log.d("ProductRepository", "${cachedList.size} produtos carregados do cache")
                    Result.success(cachedList)
                } else {
                    Log.e("ProductRepository", "Sem cache disponível")
                    Result.failure(Exception(errorMsg))
                }
            } catch (cacheException: Exception) {
                Log.e("ProductRepository", "Erro ao acessar cache: ${cacheException.message}")
                Result.failure(Exception(errorMsg))
            }
        }
    }

    private fun apiErrorMessage(e: Exception): String = when {
        e is HttpException && e.code() in 500..599 ->
            "Erro no servidor (${e.code()}). Tente novamente em instantes."
        e is HttpException ->
            "Erro na requisição (${e.code()}). Tente novamente."
        e is SocketTimeoutException ->
            "Servidor demorou muito para responder. Tente novamente."
        e is UnknownHostException ->
            "Sem conexão com a internet. Verifique sua rede."
        e is IOException ->
            "Sem conexão com a internet. Verifique sua rede."
        else ->
            "Erro inesperado: ${e.message}"
    }

    override suspend fun getProductById(productId: String): Result<Product?> {
        return try {
            val product = api.getProductById(productId)
            if (product != null) {
                productDao.insertProduct(product)
                Result.success(product)
            } else {
                Result.success(productDao.getProductById(productId))
            }
        } catch (e: Exception) {
            Log.e("ProductRepository", "API falhou para produto $productId: ${e.message} — tentando cache")
            return try {
                val cachedProduct = productDao.getProductById(productId)
                if (cachedProduct != null) Result.success(cachedProduct)
                else Result.failure(Exception("Produto não encontrado"))
            } catch (cacheException: Exception) {
                Result.failure(cacheException)
            }
        }
    }

    // ✅ BUSCA BANNERS (não faz cache por enquanto)
    override suspend fun getBanners(): Result<List<Banner>> {
        return try {
            val response = api.getBanners()
            Result.success(response)
        } catch (e: Exception) {
            Log.e("ProductRepositoryImpl", "Erro ao carregar banners", e)
            Result.failure(e)
        }
    }

    // ✅ Limpar cache manualmente
    override suspend fun clearCache() {
        productDao.clearAllProducts()
        Log.d("ProductRepository", "Cache limpo")
    }
}