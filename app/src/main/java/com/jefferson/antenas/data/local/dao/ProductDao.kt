package com.jefferson.antenas.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ✅ INSERIR PRODUTOS (ou atualizar se já existem)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    // ✅ INSERIR UM PRODUTO
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    // ✅ PEGAR TODOS OS PRODUTOS
    @Query("SELECT * FROM Product")
    fun getAllProducts(): Flow<List<Product>>

    // ✅ PEGAR UM PRODUTO POR ID
    @Query("SELECT * FROM Product WHERE id = :productId")
    suspend fun getProductById(productId: String): Product?

    // ✅ PEGAR PRODUTOS POR CATEGORIA
    @Query("SELECT * FROM Product WHERE category = :category")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    // ✅ PEGAR PRODUTOS NOVOS
    @Query("SELECT * FROM Product WHERE isNew = 1")
    fun getNewProducts(): Flow<List<Product>>

    // ✅ PEGAR PRODUTOS COM DESCONTO
    @Query("SELECT * FROM Product WHERE discount > 0 ORDER BY discount DESC")
    fun getProductsWithDiscount(): Flow<List<Product>>

    // ✅ LIMPAR TODOS OS PRODUTOS
    @Query("DELETE FROM Product")
    suspend fun clearAllProducts()

    // ✅ CONTAR QUANTOS PRODUTOS TEM
    @Query("SELECT COUNT(*) FROM Product")
    suspend fun getProductCount(): Int
}