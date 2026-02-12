package com.jefferson.antenas.data.remote

import com.jefferson.antenas.data.model.Banner
import com.jefferson.antenas.data.model.CheckoutRequest
import com.jefferson.antenas.data.model.CheckoutResponse
import com.jefferson.antenas.data.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface JeffersonApi {

    @GET("api/products")
    suspend fun getProducts(): List<Product>

    @GET("api/products/{productId}")
    suspend fun getProductById(@Path("productId") productId: String): Product?

    @GET("api/banners")
    suspend fun getBanners(): List<Banner>

    // --- ROTA ATUALIZADA (NATIVO) ---
    // Agora chama \'payment-sheet\' em vez de \'create-checkout-session\'
    @POST("api/payment-sheet")
    suspend fun createPaymentSheet(@Body request: CheckoutRequest): CheckoutResponse
}