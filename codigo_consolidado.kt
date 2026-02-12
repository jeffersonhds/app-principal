// ============================================
// CÓDIGO CONSOLIDADO DO APP
// Gerado em: qui 12 fev 2026 19:34:25 -04
// ============================================

// ============================================
// Arquivo: ./app/src/androidTest/java/com/jefferson/antenas/ExampleInstrumentedTest.kt
// ============================================
package com.jefferson.antenas

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.jefferson.antenas", appContext.packageName)
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/local/AppDatabase.kt
// ============================================
package com.jefferson.antenas.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jefferson.antenas.data.local.dao.ProductDao
import com.jefferson.antenas.data.model.Product

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    // ✅ AGORA TEM O DAO!
    abstract fun productDao(): ProductDao
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/local/dao/ProductDao.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/Address.kt
// ============================================
package com.jefferson.antenas.data.model

/**
 * Representa um endereço físico, usado para informações de entrega e cobrança.
 */
data class Address(
    val city: String?,
    val country: String? = "BR", // Padrão para Brasil
    val line1: String?,
    val line2: String? = null,
    val postalCode: String?,
    val state: String?
)


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/Banner.kt
// ============================================
package com.jefferson.antenas.data.model

import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import com.jefferson.antenas.data.model.FlexibleStringAdapter

data class Banner(
    @JsonAdapter(FlexibleStringAdapter::class)
    @SerializedName(value = "id", alternate = ["_id"]) val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName(value = "imageUrl", alternate = ["image_url"]) val imageUrl: String,
    @SerializedName(value = "actionText", alternate = ["action_text"]) val actionText: String
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/CartItem.kt
// ============================================
package com.jefferson.antenas.data.model

data class CartItem(
    val product: Product,
    val quantity: Int = 1
) {
    // Calcula o total deste item (Preço x Quantidade)
    val total: Double
        get() = product.getDiscountedPrice() * quantity
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/CheckoutModels.kt
// ============================================
package com.jefferson.antenas.data.model

import com.google.gson.annotations.SerializedName

// O que enviamos para o servidor (Agora com customerInfo)
data class CheckoutRequest(
    @SerializedName("items") val items: List<CheckoutItemDto>,
    @SerializedName("customerInfo") val customerInfo: CustomerInfoDto // Campo Novo
)

// Dados do Cliente
data class CustomerInfoDto(
    @SerializedName("name") val name: String,
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("phoneNumber") val phoneNumber: String
)

data class CheckoutItemDto(
    @SerializedName("id") val id: String,
    @SerializedName("quantity") val quantity: Int
)

// O que recebemos (Igual)
data class CheckoutResponse(
    @SerializedName("paymentIntent") val paymentIntent: String,
    @SerializedName("ephemeralKey") val ephemeralKey: String,
    @SerializedName("customer") val customer: String
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/FlexibleStringAdapter.kt
// ============================================
package com.jefferson.antenas.data.model

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class FlexibleStringAdapter : TypeAdapter<String>() {
    override fun write(out: JsonWriter, value: String?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(`in`: JsonReader): String {
        return when (`in`.peek()) {
            JsonToken.STRING -> `in`.nextString()
            JsonToken.NUMBER -> `in`.nextString()
            JsonToken.BOOLEAN -> `in`.nextBoolean().toString()
            JsonToken.NULL -> {
                `in`.nextNull()
                ""
            }
            else -> {
                `in`.skipValue()
                ""
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/Product.kt
// ============================================
package com.jefferson.antenas.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity
data class Product(
    // O servidor manda ID numérico, mas o GSON converte para String automaticamente para facilitar a navegação
    @PrimaryKey
    @SerializedName("id") val id: String,

    @SerializedName("name") val name: String,

    @SerializedName("description") val description: String,

    // O servidor manda decimal/string, aqui garantimos String para não dar erro de arredondamento
    @SerializedName("price") val price: String,

    @SerializedName("imageUrl") val imageUrl: String,

    // Campos novos que adicionamos no servidor
    @SerializedName("category") val category: String? = null,

    @SerializedName("discount") val discount: Int? = 0,

    @SerializedName("isNew") val isNew: Boolean? = false
) {
    fun getDiscountedPrice(): Double {
        val basePrice = price
            .trim()
            .replace("R$", "")
            .replace(" ", "")
            .replace(",", ".")
            .toDoubleOrNull()
            ?: 0.0

        val discountPercent = (discount ?: 0).coerceAtLeast(0)
        val multiplier = 1.0 - (discountPercent / 100.0)

        return (basePrice * multiplier).coerceAtLeast(0.0)
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/model/User.kt
// ============================================
package com.jefferson.antenas.data.model

/**
 * Representa a identidade permanente de um cliente no aplicativo.
 *
 * @property uid O ID único fornecido pelo Firebase Authentication. É a chave primária.
 * @property name O nome de exibição do cliente.
 * @property email O email usado para login e comunicação.
 * @property points O saldo de pontos de fidelidade do cliente.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val points: Int = 0
)


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/remote/JeffersonApi.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/repository/CartRepository.kt
// ============================================
package com.jefferson.antenas.data.repository

import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor() {

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())
    val items: StateFlow<List<CartItem>> = _items.asStateFlow()

    // AGORA ACEITA QUANTIDADE (Padrão = 1 para funcionar na Home)
    fun addToCart(product: Product, quantityToAdd: Int = 1) {
        val currentList = _items.value.toMutableList()
        val existingItem = currentList.find { it.product.id == product.id }

        if (existingItem != null) {
            // Se já existe, soma a nova quantidade
            updateQuantity(product.id, existingItem.quantity + quantityToAdd)
        } else {
            // Se não, adiciona com a quantidade escolhida
            currentList.add(CartItem(product, quantityToAdd))
            _items.value = currentList
        }
    }

    fun updateQuantity(productId: String, quantity: Int) {
        if (quantity <= 0) {
            removeItem(productId)
            return
        }

        val currentList = _items.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }

        if (index != -1) {
            currentList[index] = currentList[index].copy(quantity = quantity)
            _items.value = currentList
        }
    }

    fun removeItem(productId: String) {
        val currentList = _items.value.toMutableList()
        currentList.removeIf { it.product.id == productId }
        _items.value = currentList
    }

    fun getCartTotal(): Double {
        return _items.value.sumOf { it.total }
    }

    fun clearCart() {
        _items.value = emptyList()
    }

    fun getCartCount(): Int {
        return _items.value.sumOf { it.quantity }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/repository/ProductRepositoryImpl.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/data/repository/ProductRepository.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/di/AuthModule.kt
// ============================================
package com.jefferson.antenas.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/di/DatabaseModule.kt
// ============================================
package com.jefferson.antenas.di

import android.content.Context
import androidx.room.Room
import com.jefferson.antenas.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "jefferson_antenas.db"
        ).fallbackToDestructiveMigration() // Limpa o banco se mudarmos a estrutura (bom para dev)
            .build()
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/di/NetworkModule.kt
// ============================================
package com.jefferson.antenas.di

import com.jefferson.antenas.data.remote.JeffersonApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://jefferson-antenas-server.onrender.com"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            // ✅ OTIMIZADO: Timeouts reduzidos para detecção rápida
            .connectTimeout(10, TimeUnit.SECONDS)  // 10s em vez de 30s
            .readTimeout(10, TimeUnit.SECONDS)     // 10s em vez de 30s
            .writeTimeout(10, TimeUnit.SECONDS)    // Adicionado para upload
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideJeffersonApi(retrofit: Retrofit): JeffersonApi {
        return retrofit.create(JeffersonApi::class.java)
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/di/RepositoryModule.kt
// ============================================
package com.jefferson.antenas.di

import com.jefferson.antenas.data.repository.ProductRepository
import com.jefferson.antenas.data.repository.ProductRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/JeffersonApp.kt
// ============================================
package com.jefferson.antenas

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class JeffersonApp : Application()

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/MainActivity.kt
// ============================================
package com.jefferson.antenas

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jefferson.antenas.ui.componets.BottomNavBar
import com.jefferson.antenas.ui.screens.auth.AuthViewModel
import com.jefferson.antenas.ui.screens.auth.LoginScreen
import com.jefferson.antenas.ui.screens.auth.SignUpScreen
import com.jefferson.antenas.ui.screens.cart.CartScreen
import com.jefferson.antenas.ui.screens.checkout.CheckoutScreen
import com.jefferson.antenas.ui.screens.downloads.DownloadsScreen
import com.jefferson.antenas.ui.screens.home.HomeScreen
import com.jefferson.antenas.ui.screens.product.ProductDetailScreen
import com.jefferson.antenas.ui.screens.profile.ProfileScreen
import com.jefferson.antenas.ui.screens.search.SearchScreen
import com.jefferson.antenas.ui.screens.services.ServicesScreen
import com.jefferson.antenas.ui.screens.splash.SplashScreen
import com.jefferson.antenas.ui.screens.store.StoreScreen
import com.jefferson.antenas.ui.screens.support.SupportScreen
import com.jefferson.antenas.ui.theme.JeffersonAntenasAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JeffersonAntenasAppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    // Adicionei "splash" na lista para esconder a barra de baixo nela
    val hideBottomBarRoutes = listOf("splash", "login", "signup", "checkout", "product/{productId}", "cart", "search")

    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute !in hideBottomBarRoutes) {
                BottomNavBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash", // <--- MUDANÇA AQUI: Começa pela Splash
            modifier = Modifier.padding(innerPadding)
        ) {
            // --- NOVA ROTA SPLASH ---
            composable("splash") {
                SplashScreen(
                    onSplashFinished = {
                        // Quando terminar, vai para o Login e apaga a Splash da memória (backstack)
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                )
            }

            // ... Suas outras rotas (login, signup, home, etc.) continuam iguais aqui para baixo ...
            composable("login") {
                LoginScreen(
                    onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onSignUpClick = { navController.navigate("signup") }
                )
            }
            // (Mantenha o resto do código igual estava no seu arquivo)
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onLoginClick = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
                )
            }

            // ... (Restante das rotas Home, Store, etc. que você já tem) ...

            composable("home") {
                HomeScreen(
                    onProductClick = { id -> navController.navigate("product/$id") },
                    onCartClick = { navController.navigate("cart") },
                    onServicesClick = { navController.navigate("services") },
                    onSearchClick = { navController.navigate("search") },
                    onProfileClick = { navController.navigate("profile") }
                )
            }
            composable("store") { StoreScreen(onProductClick = { id -> navController.navigate("product/$id") }, onCartClick = { navController.navigate("cart") }, onServicesClick = { navController.navigate("services") }, onBackClick = { navController.popBackStack() }) }
            composable("services") { ServicesScreen(onBackClick = { navController.popBackStack() }) }
            composable("downloads") { DownloadsScreen(onBackClick = { navController.popBackStack() }) }
            composable("support") { SupportScreen(onBackClick = { navController.popBackStack() }) }
            composable("profile") {
                ProfileScreen(onLogout = {
                    navController.navigate("login") {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                })
            }
            composable("search") { SearchScreen(onBackClick = { navController.popBackStack() }, onProductClick = { id -> navController.navigate("product/$id") }) }
            composable(route = "product/{productId}", arguments = listOf(navArgument("productId") { type = NavType.StringType })) { ProductDetailScreen(onBackClick = { navController.popBackStack() }) }
            composable("cart") { CartScreen(onBackClick = { navController.popBackStack() }, onCheckoutClick = { navController.navigate("checkout") }) }
            composable("checkout") {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderSuccess = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                )
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/AdvancedUI.kt
// ============================================
package com.jefferson.antenas.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.theme.* // Certifique-se que suas cores estão importadas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Modelos visuais simples
data class BannerItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val imageUrl: String,
    val actionText: String
)

data class ReviewItem(
    val id: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val date: String
)

// --- 1. Carrossel Principal (Hero) ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    banners: List<BannerItem>,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(220.dp).padding(16.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
        ) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente para leitura
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                )))

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(banner.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(banner.subtitle, fontSize = 13.sp, color = Color.LightGray)

                    Surface(
                        color = Color(0xFFF59E0B), // Laranja
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            banner.actionText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = Color(0xFF0F172A), // Azul Escuro
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Indicadores (Bolinhas)
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(if (pagerState.currentPage == index) Color(0xFFF59E0B) else Color.Gray, CircleShape)
                )
            }
        }
    }
}

// --- 2. Selos de Confiança ---
@Composable
fun TrustBadges(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BadgeItem(Icons.Default.VerifiedUser, "Garantia", "1 Ano")
        BadgeItem(Icons.Default.LocalShipping, "Entrega", "Rápida")
        BadgeItem(Icons.Default.Lock, "Seguro", "100%")
        BadgeItem(Icons.Default.HeadsetMic, "Suporte", "24h")
    }
}

@Composable
fun BadgeItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, sub: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(top = 4.dp))
        Text(sub, fontSize = 10.sp, color = Color.Gray)
    }
}

// --- 3. Carrossel de Avaliações ---
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReviewsCarousel(
    reviews: List<ReviewItem>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { reviews.size })

    Column(modifier = modifier.padding(vertical = 16.dp)) {
        Text(
            "O que dizem nossos clientes",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(140.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            val review = reviews[page]
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)) // Azul Médio
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(review.customerName, fontWeight = FontWeight.Bold, color = Color.White)
                        Row {
                            repeat(5) { i ->
                                Icon(
                                    if (i < review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i < review.rating) Color(0xFFF59E0B) else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    Text(
                        review.comment,
                        fontSize = 13.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(top = 8.dp),
                        maxLines = 3
                    )
                }
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/CartAppBarAction.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import com.jefferson.antenas.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartAppBarAction(
    cartCount: Int,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- LÓGICA DA ANIMAÇÃO ---
    var previousCartCount by remember { mutableStateOf(cartCount) }
    var iconScale by remember { mutableStateOf(1f) }

    // Anima a escala do ícone suavemente
    val animatedScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = tween(durationMillis = 300),
        label = "CartIconScale"
    )

    // Efeito que dispara a animação quando um item é adicionado
    LaunchedEffect(cartCount) {
        if (cartCount > previousCartCount) {
            // Dispara a animação de "pulo"
            iconScale = 1.3f
            // Agenda a volta ao estado normal
            kotlinx.coroutines.delay(150) // Metade da duração da animação
            iconScale = 1f
        }
        // Atualiza a contagem anterior
        previousCartCount = cartCount
    }

    BadgedBox(
        modifier = modifier,
        badge = {
            if (cartCount > 0) {
                Badge { Text(text = cartCount.toString()) }
            }
        }
    ) {
        IconButton(onClick = onCartClick) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Carrinho de Compras",
                tint = TextPrimary,
                modifier = Modifier.scale(animatedScale) // Aplica a escala animada ao ícone
            )
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/CategoryFilter.kt
// ============================================
package com.jefferson.antenas.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = { Text(category) },
                leadingIcon = if (isSelected) {
                    { Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MidnightBlueCard,
                    labelColor = TextSecondary,
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = TextPrimary,
                    selectedLeadingIconColor = TextPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = if(isSelected) SignalOrange else TextSecondary
                )
            )
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/CustomToast.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.SuccessGreen
import com.jefferson.antenas.ui.theme.TextPrimary

/**
 * Toast customizado profissional com animação suave
 *
 * Tipos disponíveis:
 * - ToastType.SUCCESS (verde)
 * - ToastType.ERROR (vermelho)
 * - ToastType.WARNING (laranja)
 * - ToastType.INFO (azul)
 */
enum class ToastType {
    SUCCESS, ERROR, WARNING, INFO
}

data class ToastConfig(
    val message: String,
    val type: ToastType = ToastType.INFO,
    val duration: Long = 3000L
)

@Composable
fun CustomToast(
    visible: Boolean,
    message: String,
    type: ToastType = ToastType.ERROR,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 400)
        ) + fadeIn(animationSpec = tween(durationMillis = 400)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        // ✅ CORRIGIDO: Sem destructuring, declaração clara
        val backgroundColor: Color
        val iconColor: Color
        val icon: ImageVector
        val textColor: Color

        when (type) {
            ToastType.SUCCESS -> {
                backgroundColor = SuccessGreen.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.CheckCircle
                textColor = Color.White
            }
            ToastType.ERROR -> {
                backgroundColor = ErrorRed.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.Error
                textColor = Color.White
            }
            ToastType.WARNING -> {
                backgroundColor = SignalOrange.copy(alpha = 0.95f)
                iconColor = Color.White
                icon = Icons.Default.Warning
                textColor = Color.White
            }
            ToastType.INFO -> {
                backgroundColor = MidnightBlueCard.copy(alpha = 0.95f)
                iconColor = SignalOrange
                icon = Icons.Default.Info
                textColor = TextPrimary
            }
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            color = backgroundColor,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.width(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = message,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/HeroCarousel.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(
    banners: List<BannerItem>,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })

    Box(modifier = modifier.fillMaxWidth().height(200.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .clickable { /* Ação de clique do banner */ },
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AsyncImage(
                        model = banner.imageUrl,
                        contentDescription = banner.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Gradiente sobre a imagem para legibilidade do texto
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                                    startY = 300f
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = banner.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = banner.subtitle,
                            color = TextPrimary.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Indicadores de página
        Row(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) SignalOrange else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/NavigationComponents.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jefferson.antenas.ui.theme.*

@Composable
fun BottomNavBar(navController: NavHostController) {
    NavigationBar(
        modifier = Modifier.height(72.dp),
        containerColor = CardGradientStart,
        contentColor = TextSecondary
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // O "SALÃO DO TESOURO" FOI ADICIONADO AQUI
        val items = listOf(
            Triple("home", "Início", Icons.Default.Home),
            Triple("store", "Loja", Icons.Default.ShoppingBag),
            Triple("downloads", "Baixar", Icons.Default.Download),
            Triple("support", "Suporte", Icons.Default.HeadsetMic)
        )

        items.forEach { (route, label, icon) ->
            val isSelected = currentRoute == route

            val iconScale by animateFloatAsState(
                targetValue = if (isSelected) 1.2f else 1.0f,
                label = "IconScaleAnimation"
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        icon,
                        contentDescription = label,
                        modifier = Modifier
                            .size(24.dp)
                            .scale(iconScale),
                        tint = if (isSelected) SignalOrange else TextSecondary
                    )
                },
                label = {
                    Text(
                        label,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) SignalOrange else TextSecondary
                    )
                },
                selected = isSelected,
                onClick = {
                    if (currentRoute != route) {
                        navController.navigate(route) {
                            // Mantém o comportamento de navegação padrão
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MidnightBlueEnd
                )
            )
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/ProductCard.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onAddToCart: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdded by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }

    // Efeito para reverter o estado do botão após um tempo
    if (isAdded) {
        LaunchedEffect(isAdded) {
            delay(2000)
            isAdded = false
        }
    }

    val elevation by animateDpAsState(
        targetValue = if (isHovered) 16.dp else 8.dp,
        label = "CardElevation"
    )

    val imageAlpha by animateColorAsState(
        targetValue = if (isHovered) Color.Black.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0f),
        label = "ImageOverlay"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isHovered = true
                onClick()
            }
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ✅ IMAGEM COM OVERLAY E BADGES
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Overlay ao passar o mouse
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(imageAlpha)
                )

                // ✅ BADGE DE DESCONTO (canto superior esquerdo)
                if (product.discount != null && product.discount > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp),
                        color = ErrorRed,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "-${product.discount}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ✅ BADGE "NOVO" (canto superior direito)
                if (product.isNew == true) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        color = SignalOrange,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "NOVO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MidnightBlueStart,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // ✅ BOTÃO DE FAVORITO (canto inferior direito)
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { isFavorite = !isFavorite }
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.White.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (isFavorite) ErrorRed else TextSecondary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
            }

            // ✅ CONTEÚDO
            Column(modifier = Modifier.padding(12.dp)) {
                // Nome do Produto
                Text(
                    text = product.name,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                // ✅ RATING (estrelas fictícias - adapte conforme necessário)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { i ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i < 4) SignalOrange else Color.Gray.copy(alpha = 0.3f),
                            modifier = Modifier.size(14.sp.value.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "(4.0)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ✅ PREÇOS (com desconto tachado)
                if (product.discount != null && product.discount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = product.price.toCurrency(),
                            color = TextTertiary,
                            fontSize = 11.sp,
                            textDecoration = TextDecoration.LineThrough
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = product.getDiscountedPrice().toCurrency(),
                            color = SignalOrange,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    Text(
                        text = product.getDiscountedPrice().toCurrency(),
                        color = SignalOrange,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // ✅ BOTÃO ANIMADO COM MAIS ESTILO
                Button(
                    onClick = {
                        if (!isAdded) {
                            onAddToCart(product)
                            isAdded = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAdded) SuccessGreen.copy(alpha = 0.2f) else SignalOrange.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Crossfade(targetState = isAdded, label = "AddToCartAnimation") { added ->
                        if (added) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(18.sp.value.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Adicionado!",
                                    color = SuccessGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.AddShoppingCart,
                                    contentDescription = null,
                                    tint = SignalOrange,
                                    modifier = Modifier.size(16.sp.value.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Adicionar",
                                    color = SignalOrange,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/ReviewsCarousel.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary

// ✅ CONSOLIDADO: Usa ReviewItem de UiModels.kt (única fonte de verdade)
@Composable
fun ReviewsCarousel(
    reviews: List<ReviewItem>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "O que nossos clientes dizem",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews) { review ->
                ReviewCard(review)
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItem) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // ✅ Stars com base no rating
            Row {
                repeat(review.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = SignalOrange)
                }
                repeat(5 - review.rating) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.text,
                color = TextPrimary,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "- ${review.author}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/SearchAppBar.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class) // AVISO CORRIGIDO
@Composable
fun SearchAppBar(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Pede foco para o campo de texto assim que o componente aparece
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    TopAppBar(
        title = {
            TextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                placeholder = { Text("Buscar produtos...", color = TextSecondary) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = SignalOrange
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
            )
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
            }
        },
        actions = {
            // Mostra o botão de limpar apenas se houver texto
            if (searchQuery.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = TextPrimary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlueStart)
    )
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/SectionTitle.kt
// ============================================
package com.jefferson.antenas.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.TextPrimary

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        color = TextPrimary,
        modifier = Modifier.padding(start = 16.dp, bottom = 12.dp, top = 8.dp)
    )
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/SharedComponents.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary

@Composable
fun ModernSuccessToast(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Surface(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MidnightBlueCard,
            shape = MaterialTheme.shapes.medium,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = SignalOrange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/ShimmerProductCard.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.jefferson.antenas.ui.theme.MidnightBlueCard
import com.valentinilk.shimmer.shimmer

@Composable
fun ShimmerProductCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard)
    ) {
        Column(modifier = Modifier.shimmer()) { // Aplica o efeito shimmer a tudo dentro desta coluna
            // Placeholder para a imagem
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(Color.Gray)
            )

            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Placeholder para o nome do produto
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(24.dp)
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder para o preço
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(20.dp)
                        .background(Color.Gray)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Placeholder para o botão
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Gray)
                )
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/StoreEnhancements.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*

// ✅ BANNER DE PROMOÇÃO NO TOPO
@Composable
fun PromotionBanner(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SignalOrange,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🎉 $text",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MidnightBlueStart
            )
        }
    }
}

// ✅ CARD COM INFO DE FRETE/PROMOÇÃO
@Composable
fun ProductBenefitBadge(
    icon: String,
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .background(Color.Transparent)
            .padding(4.dp),
        color = SignalOrange.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 10.sp)
            Text(
                text = text,
                fontSize = 9.sp,
                color = SignalOrange,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ✅ CHIP DE FILTRO COM ANIMAÇÃO
@Composable
fun AnimatedFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        },
        modifier = modifier,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = SignalOrange,
            selectedLabelColor = MidnightBlueStart,
            containerColor = CardGradientStart,
            labelColor = TextSecondary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// ✅ STATS DA LOJA (Produtos totais, categorias, etc)
@Composable
fun StoreStats(
    totalProducts: Int,
    totalCategories: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            number = totalProducts.toString(),
            label = "Produtos",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = totalCategories.toString(),
            label = "Categorias",
            modifier = Modifier.weight(1f)
        )
        StatCard(
            number = "100%",
            label = "Originais",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    number: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = number,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = SignalOrange
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ✅ AVISO DE FRETE GRÁTIS
@Composable
fun FreeShippingBanner(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SuccessGreen.copy(alpha = 0.15f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocalShipping,
                contentDescription = null,
                tint = SuccessGreen,
                modifier = Modifier.size(20.dp)
            )
            Column {
                Text(
                    text = "Frete Grátis",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SuccessGreen
                )
                Text(
                    text = "Acima de R$ 100",
                    fontSize = 11.sp,
                    color = SuccessGreen.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// ✅ BOTÃO "VOLTAR AO TOPO" (flutuante)
@Composable
fun ScrollToTopButton(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300)
        ),
        modifier = modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier
                .padding(16.dp)
                .size(48.dp),
            containerColor = SignalOrange,
            contentColor = MidnightBlueStart,
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Voltar ao topo",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ✅ FILTRO ATIVO INDICATOR
@Composable
fun ActiveFiltersIndicator(
    hasActiveFilters: Boolean,
    filterCount: Int,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!hasActiveFilters) return

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        color = SignalOrange.copy(alpha = 0.1f),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = SignalOrange,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "$filterCount filtro${if (filterCount > 1) "s" else ""} ativo${if (filterCount > 1) "s" else ""}",
                    fontSize = 12.sp,
                    color = SignalOrange,
                    fontWeight = FontWeight.SemiBold
                )
            }

            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.height(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Limpar",
                    tint = SignalOrange,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Limpar",
                    fontSize = 10.sp,
                    color = SignalOrange,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/StoreFilterComponents.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*

// ✅ HEADER DA LOJA COM BUSCA E FILTROS
@Composable
fun StoreHeader(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSearchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MidnightBlueStart)
            .padding(16.dp)
    ) {
        // Título
        Text(
            text = "Loja Completa",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Barra de Busca
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Buscar produtos...", color = TextSecondary) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SignalOrange) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardGradientStart,
                unfocusedContainerColor = CardGradientStart,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = SignalOrange,
                unfocusedBorderColor = TextSecondary
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

// ✅ FILTROS HORIZONTAIS
data class FilterOption(
    val id: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null
)

@Composable
fun HorizontalFilters(
    filters: List<FilterOption>,
    selectedFilter: String?,
    onFilterSelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Botão "Limpar"
        item {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { onFilterSelected(null) },
                label = { Text("Todos", fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = MidnightBlueStart,
                    containerColor = CardGradientStart,
                    labelColor = TextSecondary
                )
            )
        }

        // Filtros
        items(filters) { filter ->
            val isSelected = selectedFilter == filter.id
            FilterChip(
                selected = isSelected,
                onClick = { onFilterSelected(if (isSelected) null else filter.id) },
                label = { Text(filter.label, fontSize = 12.sp) },
                leadingIcon = filter.icon?.let { icon ->
                    { Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp)) }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SignalOrange,
                    selectedLabelColor = MidnightBlueStart,
                    containerColor = CardGradientStart,
                    labelColor = TextSecondary
                )
            )
        }
    }
}

// ✅ OPÇÕES DE ORDENAÇÃO
data class SortOption(
    val id: String,
    val label: String
)

@Composable
fun SortDropdown(
    sortOptions: List<SortOption>,
    selectedSort: String,
    onSortSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier.padding(horizontal = 16.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = SignalOrange
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, SignalOrange),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Ordenar por: ${sortOptions.find { it.id == selectedSort }?.label ?: "Padrão"}",
                fontSize = 12.sp,
                maxLines = 1
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(CardGradientStart)
        ) {
            sortOptions.forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            option.label,
                            color = if (option.id == selectedSort) SignalOrange else TextPrimary,
                            fontWeight = if (option.id == selectedSort) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onSortSelected(option.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ✅ INDICADOR DE RESULTADOS
@Composable
fun ResultsInfo(
    totalProducts: Int,
    filteredProducts: Int,
    modifier: Modifier = Modifier
) {
    if (totalProducts == 0) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Mostrando $filteredProducts de $totalProducts produtos",
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

// ✅ EMPTY STATE (quando não tem produtos)
@Composable
fun EmptyStoreState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingBag,
            contentDescription = null,
            tint = SignalOrange,
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nenhum Produto Encontrado",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tente ajustar seus filtros ou faça uma nova busca",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/TopAppBarCustom.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarCustom(
    title: String,
    modifier: Modifier = Modifier,
    showBack: Boolean = true,
    onBackClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {} // Parâmetro para ações
) {
    TopAppBar(
        title = { Text(text = title, color = TextPrimary) },
        modifier = modifier,
        navigationIcon = {
            if (showBack) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = SignalOrange
                    )
                }
            }
        },
        // Ações (como o carrinho) são passadas aqui
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/TrustBadges.kt
// ============================================
package com.jefferson.antenas.ui.componets

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextSecondary

@Composable
fun TrustBadges(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        TrustBadgeItem(icon = Icons.Default.VerifiedUser, text = "Compra Segura")
        TrustBadgeItem(icon = Icons.Default.LocalShipping, text = "Entrega Rápida")
    }
}

@Composable
private fun TrustBadgeItem(icon: ImageVector, text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = SignalOrange,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/componets/UiModels.kt
// ============================================
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
    val buttonText: String
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/auth/AuthViewModel.kt
// ============================================
package com.jefferson.antenas.ui.screens.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoginSuccessful: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(error = "Email e senha não podem estar em branco.") }
            return
        }

        _authState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            loginWithRetry(email, password, tentativa = 1)
        }
    }

    private suspend fun loginWithRetry(email: String, password: String, tentativa: Int = 1) {
        try {
            val startTime = System.currentTimeMillis()
            Log.d("AuthViewModel", "🔐 TENTATIVA $tentativa - LOGIN com email: $email")

            // ✅ TIMEOUT DE 30 SEGUNDOS (para internet lenta)
            Log.d("AuthViewModel", "📱 Chamando Firebase Auth (timeout: 30s, tentativa $tentativa/3)...")

            val success = withTimeoutOrNull(30000L) {
                auth.signInWithEmailAndPassword(email, password).await()
                true
            }

            val authTime = System.currentTimeMillis() - startTime
            Log.d("AuthViewModel", "⏱️ Firebase respondeu em ${authTime}ms")

            if (success == true) {
                Log.d("AuthViewModel", "✅ LOGIN SUCESSO na tentativa $tentativa! Tempo: ${authTime}ms")
                _authState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
            } else {
                // ✅ SE TIMEOUT, TENTA NOVAMENTE ATÉ 3 VEZES
                if (tentativa < 3) {
                    Log.w("AuthViewModel", "⏱️ TIMEOUT na tentativa $tentativa, tentando novamente em 2s...")
                    _authState.update { it.copy(error = "Reconectando... (tentativa $tentativa/3)") }
                    delay(2000)
                    loginWithRetry(email, password, tentativa + 1)
                } else {
                    Log.e("AuthViewModel", "❌ FALHA após 3 tentativas")
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Servidor indisponível. Tente em alguns minutos."
                        )
                    }
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("AuthViewModel", "⏱️ TIMEOUT na tentativa $tentativa")
            if (tentativa < 3) {
                Log.w("AuthViewModel", "🔄 Tentando novamente...")
                _authState.update { it.copy(error = "Reconectando... (tentativa $tentativa/3)") }
                delay(2000)
                loginWithRetry(email, password, tentativa + 1)
            } else {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        error = "Sem conexão com servidor. Verifique sua internet."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ ERRO: ${e.message}", e)
            _authState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao fazer login."
                )
            }
        }
    }

    fun signUp(name: String, email: String, password: String) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            _authState.update { it.copy(error = "Todos os campos são obrigatórios.") }
            return
        }

        _authState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            signUpWithRetry(name, email, password, tentativa = 1)
        }
    }

    private suspend fun signUpWithRetry(name: String, email: String, password: String, tentativa: Int = 1) {
        try {
            val startTime = System.currentTimeMillis()
            Log.d("AuthViewModel", "📝 TENTATIVA $tentativa - SIGNUP com email: $email")

            // ✅ CRIAR USUÁRIO COM TIMEOUT DE 30 SEGUNDOS
            Log.d("AuthViewModel", "📱 Criando usuário (timeout: 30s, tentativa $tentativa/3)...")

            val authResult = withTimeoutOrNull(30000L) {
                auth.createUserWithEmailAndPassword(email, password).await()
            }

            if (authResult == null) {
                if (tentativa < 3) {
                    Log.w("AuthViewModel", "⏱️ TIMEOUT na tentativa $tentativa, tentando novamente...")
                    _authState.update { it.copy(error = "Reconectando... (tentativa $tentativa/3)") }
                    delay(2000)
                    signUpWithRetry(name, email, password, tentativa + 1)
                } else {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Servidor indisponível. Tente em alguns minutos."
                        )
                    }
                }
                return
            }

            val authTime = System.currentTimeMillis() - startTime
            Log.d("AuthViewModel", "✅ Firebase Auth concluído em ${authTime}ms")

            val firebaseUser = authResult.user

            if (firebaseUser != null) {
                val newUser = User(
                    uid = firebaseUser.uid,
                    name = name,
                    email = email,
                    points = 0
                )

                Log.d("AuthViewModel", "☁️ Salvando no Firestore (timeout: 20s)...")
                val firestoreStart = System.currentTimeMillis()

                val savedSuccessfully = withTimeoutOrNull(20000L) {
                    firestore.collection("users")
                        .document(firebaseUser.uid)
                        .set(newUser)
                        .await()
                    true
                }

                val firestoreTime = System.currentTimeMillis() - firestoreStart
                Log.d("AuthViewModel", "⏱️ Firestore respondeu em ${firestoreTime}ms")

                if (savedSuccessfully == true) {
                    _authState.update { it.copy(isLoading = false, isLoginSuccessful = true) }
                    Log.d("AuthViewModel", "🎉 SIGNUP SUCESSO! Tempo total: ${System.currentTimeMillis() - startTime}ms")
                } else {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            error = "Erro ao salvar perfil. Tente fazer login."
                        )
                    }
                    Log.e("AuthViewModel", "❌ TIMEOUT ao salvar no Firestore")
                }
            } else {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erro ao criar usuário."
                    )
                }
                Log.e("AuthViewModel", "❌ Firebase User é null")
            }
        } catch (e: TimeoutCancellationException) {
            Log.e("AuthViewModel", "⏱️ TIMEOUT SIGNUP na tentativa $tentativa")
            if (tentativa < 3) {
                _authState.update { it.copy(error = "Reconectando... (tentativa $tentativa/3)") }
                delay(2000)
                signUpWithRetry(name, email, password, tentativa + 1)
            } else {
                _authState.update {
                    it.copy(
                        isLoading = false,
                        error = "Servidor indisponível. Tente em alguns minutos."
                    )
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "❌ ERRO SIGNUP: ${e.message}", e)
            _authState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao cadastrar."
                )
            }
        }
    }

    fun clearError() {
        _authState.update { it.copy(error = null) }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/auth/LoginScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.CustomToast
import com.jefferson.antenas.ui.componets.ToastType
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.utils.ErrorMessageHandler
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // ✅ CORRIGIDO: rememberSaveable salva o estado mesmo quando rotaciona
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showErrorToast by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()

    // Observa o estado de sucesso do login para navegar
    LaunchedEffect(authState.isLoginSuccessful) {
        if (authState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Observa o estado de erro para mostrar um Toast customizado
    LaunchedEffect(authState.error) {
        authState.error?.let {
            errorMessage = ErrorMessageHandler.tratarErro(Exception(it))
            showErrorToast = true
            viewModel.clearError()

            delay(3000)
            showErrorToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ TÍTULOS HARMONIZADOS
            Text(
                "Bem-vindo de Volta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = TextPrimary  // Branco puro
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Acesse sua conta para continuar",
                style = MaterialTheme.typography.bodyLarge,
                color = SignalOrange  // Laranja harmonioso
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Campo de Email
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✅ Campo de Senha com Ícone de Olho
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                // ✅ NOVO: Ícone de olho para mostrar/esconder senha
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (showPassword) "Esconder senha" else "Mostrar senha",
                            tint = SignalOrange
                        )
                    }
                },
                visualTransformation = if (showPassword) {
                    VisualTransformation.None  // Mostra a senha em texto plano
                } else {
                    PasswordVisualTransformation()  // Esconde com pontos
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !authState.isLoading
            )

            Spacer(modifier = Modifier.height(32.dp))

            // ✅ Botão de Entrar
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                enabled = !authState.isLoading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CircularProgressIndicator(
                            color = MidnightBlueStart,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Entrando...", fontWeight = FontWeight.Bold, color = MidnightBlueStart)
                    }
                } else {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ✅ LINK PARA CADASTRO - CORES HARMONIZADAS
            val annotatedString = buildAnnotatedString {
                append("Não tem uma conta? ")  // TextSecondary (cinza suave)
                withStyle(style = SpanStyle(color = SignalOrange, fontWeight = FontWeight.Bold)) {
                    pushStringAnnotation(tag = "SIGNUP", annotation = "signup")
                    append("Cadastre-se")  // Laranja
                    pop()
                }
            }

            ClickableText(
                text = annotatedString,
                onClick = { offset ->
                    if (!authState.isLoading) {
                        annotatedString.getStringAnnotations(tag = "SIGNUP", start = offset, end = offset)
                            .firstOrNull()?.let { onSignUpClick() }
                    }
                },
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        // ✅ Toast customizado profissional
        CustomToast(
            visible = showErrorToast,
            message = errorMessage,
            type = ToastType.ERROR,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/auth/SignUpScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import com.jefferson.antenas.ui.theme.TextSecondary
import com.jefferson.antenas.ui.theme.ErrorRed
import com.jefferson.antenas.utils.ValidationUtils

@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    // ✅ CORRIGIDO: rememberSaveable salva o estado mesmo quando rotaciona
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var showConfirmPassword by rememberSaveable { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current

    // Observa o estado de sucesso do cadastro para navegar
    LaunchedEffect(authState.isLoginSuccessful) {
        if (authState.isLoginSuccessful) {
            onSignUpSuccess()
        }
    }

    // Observa o estado de erro para mostrar um Toast
    LaunchedEffect(authState.error) {
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Crie Sua Conta",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = TextPrimary
        )
        Text(
            "Comece a juntar pontos hoje mesmo",
            style = MaterialTheme.typography.bodyLarge,
            color = SignalOrange
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Nome
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nome Completo") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = name.isNotEmpty() && !ValidationUtils.isValidName(name),
            supportingText = {
                ValidationUtils.getNameError(name)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = email.isNotEmpty() && !ValidationUtils.isValidEmail(email),
            supportingText = {
                ValidationUtils.getEmailError(email)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Campo de Senha com Ícone de Olho
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showPassword) "Esconder senha" else "Mostrar senha",
                        tint = SignalOrange
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            enabled = !authState.isLoading,
            isError = password.isNotEmpty() && !ValidationUtils.isValidPassword(password),
            supportingText = {
                ValidationUtils.getPasswordError(password)?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp)
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Campo de Confirmar Senha com Ícone de Olho
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirme a Senha") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                    Icon(
                        imageVector = if (showConfirmPassword) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (showConfirmPassword) "Esconder senha" else "Mostrar senha",
                        tint = SignalOrange
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (showConfirmPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            isError = password != confirmPassword && confirmPassword.isNotEmpty(),
            enabled = !authState.isLoading,
            supportingText = {
                if (password != confirmPassword && confirmPassword.isNotEmpty()) {
                    Text("As senhas não coincidem", color = ErrorRed, fontSize = 12.sp)
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // ✅ Botão de Cadastrar
        Button(
            onClick = { viewModel.signUp(name, email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
            enabled = !authState.isLoading &&
                    ValidationUtils.isValidName(name) &&
                    ValidationUtils.isValidEmail(email) &&
                    ValidationUtils.isValidPassword(password) &&
                    password == confirmPassword
        ) {
            if (authState.isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CircularProgressIndicator(
                        color = MidnightBlueStart,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Criando Conta...", fontWeight = FontWeight.Bold, color = MidnightBlueStart)
                }
            } else {
                Text("Criar Conta", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Link para Login
        val annotatedString = buildAnnotatedString {
            append("Já tem uma conta? ")
            withStyle(style = SpanStyle(color = SignalOrange, fontWeight = FontWeight.Bold)) {
                pushStringAnnotation(tag = "LOGIN", annotation = "login")
                append("Faça Login")
                pop()
            }
        }

        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                if (!authState.isLoading) {
                    annotatedString.getStringAnnotations(tag = "LOGIN", start = offset, end = offset)
                        .firstOrNull()?.let { onLoginClick() }
                }
            },
            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
        )
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/cart/CartScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {
    val cartItems = cartRepository.items

    fun increaseQuantity(item: CartItem) {
        cartRepository.updateQuantity(item.product.id, item.quantity + 1)
    }

    fun decreaseQuantity(item: CartItem) {
        cartRepository.updateQuantity(item.product.id, item.quantity - 1)
    }

    fun removeItem(item: CartItem) {
        cartRepository.removeItem(item.product.id)
    }

    fun getTotal(): Double = cartRepository.getCartTotal()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBackClick: () -> Unit,
    onCheckoutClick: () -> Unit,
    viewModel: CartViewModel = hiltViewModel()
) {
    val items by viewModel.cartItems.collectAsState()
    val total = items.sumOf { it.total }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Seu Carrinho", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MidnightBlueStart
                )
            )
        },
        bottomBar = {
            if (items.isNotEmpty()) {
                Surface(
                    color = MidnightBlueCard,
                    shadowElevation = 16.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Text(total.toCurrency(), style = MaterialTheme.typography.titleLarge, color = SignalOrange, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onCheckoutClick,
                            colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = Shapes.medium
                        ) {
                            Text("Finalizar Compra", color = MidnightBlueStart, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Seu carrinho está vazio",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextTertiary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items) { item ->
                    CartItemCard(
                        item = item,
                        onIncrease = { viewModel.increaseQuantity(item) },
                        onDecrease = { viewModel.decreaseQuantity(item) },
                        onRemove = { viewModel.removeItem(item) }
                    )
                }
            }
        }
    }
}

@Composable
fun CartItemCard(
    item: CartItem,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        shape = Shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.product.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(Shapes.small).background(Color.White)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(item.product.name, style = MaterialTheme.typography.titleMedium, color = TextPrimary, maxLines = 1)
                Text(item.product.getDiscountedPrice().toCurrency(), style = MaterialTheme.typography.bodyMedium, color = SignalOrange)

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDecrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, null, tint = TextSecondary)
                    }
                    Text(
                        item.quantity.toString(),
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    IconButton(onClick = onIncrease, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, null, tint = TextSecondary)
                    }
                }
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, null, tint = ErrorRed)
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/checkout/CheckoutScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.BuildConfig
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    onBackClick: () -> Unit,
    onOrderSuccess: () -> Unit,
    viewModel: CheckoutViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // ERRO CORRIGIDO: Um Flow regular (não StateFlow) precisa de um valor inicial.
    val cartTotal by viewModel.cartTotal.collectAsState(initial = 0.0)
    val context = LocalContext.current

    // --- CONFIGURAÇÃO DO STRIPE (Usando BuildConfig) ---
    LaunchedEffect(Unit) {
        // ✅ SEGURO: Chave vem do BuildConfig, não do código-fonte
        PaymentConfiguration.init(
            context,
            BuildConfig.STRIPE_PUBLIC_KEY
        )
    }

    val paymentSheet = rememberPaymentSheet { result ->
        when (result) {
            is PaymentSheetResult.Completed -> {
                viewModel.onPaymentSuccess()
                onOrderSuccess()
            }
            is PaymentSheetResult.Canceled -> {
                viewModel.onPaymentResultHandled()
            }
            is PaymentSheetResult.Failed -> {
                viewModel.onPaymentResultHandled()
            }
        }
    }

    LaunchedEffect(uiState.paymentInfo) {
        uiState.paymentInfo?.let { info ->
            paymentSheet.presentWithPaymentIntent(
                info.paymentIntent,
                PaymentSheet.Configuration(
                    merchantDisplayName = "Jefferson Antenas",
                    customer = PaymentSheet.CustomerConfiguration(
                        id = info.customer,
                        ephemeralKeySecret = info.ephemeralKey
                    )
                )
            )
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finalizar Pedido", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MidnightBlueStart)
            )
        },
        containerColor = MidnightBlueStart
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Seus Dados de Entrega", style = MaterialTheme.typography.titleMedium, color = SignalOrange)

                CheckoutTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = "Nome Completo",
                    icon = Icons.Default.Person
                )

                CheckoutTextField(
                    value = uiState.phoneNumber,
                    onValueChange = viewModel::onPhoneChange,
                    label = "Telefone / WhatsApp",
                    icon = Icons.Default.Phone,
                    keyboardType = KeyboardType.Phone
                )

                CheckoutTextField(
                    value = uiState.address,
                    onValueChange = viewModel::onAddressChange,
                    label = "Endereço (Rua e Número)",
                    icon = Icons.Default.Place
                )

                CheckoutTextField(
                    value = uiState.city,
                    onValueChange = viewModel::onCityChange,
                    label = "Cidade e Estado",
                    icon = Icons.Default.Place
                )

                HorizontalDivider(color = CardBorder, modifier = Modifier.padding(vertical = 8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total a Pagar:", style = MaterialTheme.typography.titleLarge, color = TextPrimary)
                    Text(
                        cartTotal.toCurrency(),
                        style = MaterialTheme.typography.titleLarge,
                        color = SignalOrange,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { viewModel.preparePayment() },
                    enabled = !uiState.isLoading && uiState.name.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = Shapes.medium
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(color = MidnightBlueStart, modifier = Modifier.size(24.dp))
                    } else {
                        Text("Pagar com Cartão", color = MidnightBlueStart, fontWeight = FontWeight.Bold)
                    }
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = ErrorRed,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CheckoutTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = SignalOrange) },
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MidnightBlueCard,
            unfocusedContainerColor = MidnightBlueCard,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = SignalOrange,
            unfocusedBorderColor = TextSecondary,
            focusedLabelColor = SignalOrange,
            unfocusedLabelColor = TextSecondary
        ),
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/checkout/CheckoutViewModel.kt
// ============================================
package com.jefferson.antenas.ui.screens.checkout

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.CartItem
import com.jefferson.antenas.data.model.CheckoutItemDto
import com.jefferson.antenas.data.model.CheckoutRequest
import com.jefferson.antenas.data.model.CheckoutResponse
import com.jefferson.antenas.data.model.CustomerInfoDto
import com.jefferson.antenas.data.remote.JeffersonApi
import com.jefferson.antenas.data.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val name: String = "",
    val address: String = "",
    val city: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val paymentInfo: CheckoutResponse? = null,
    val error: String? = null,
    val isPaymentSuccessful: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val api: JeffersonApi,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    val cartTotal: StateFlow<Double> = cartRepository.items
        .map { items -> items.sumOf { it.total } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun onNameChange(newValue: String) = _uiState.update { it.copy(name = newValue) }
    fun onAddressChange(newValue: String) = _uiState.update { it.copy(address = newValue) }
    fun onCityChange(newValue: String) = _uiState.update { it.copy(city = newValue) }
    fun onPhoneChange(newValue: String) = _uiState.update { it.copy(phoneNumber = newValue) }

    fun preparePayment() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val customerInfo = CustomerInfoDto(
                    name = uiState.value.name,
                    address = uiState.value.address,
                    city = uiState.value.city,
                    phoneNumber = uiState.value.phoneNumber
                )

                val cartItems: List<CartItem> = cartRepository.items.first()
                val itemsDto: List<CheckoutItemDto> = cartItems.map { item ->
                    CheckoutItemDto(id = item.product.id, quantity = item.quantity)
                }

                val response = api.createPaymentSheet(CheckoutRequest(items = itemsDto, customerInfo = customerInfo))
                _uiState.update { it.copy(isLoading = false, paymentInfo = response) }

            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { it.copy(isLoading = false, error = "Erro ao iniciar pagamento: ${e.message}") }
            }
        }
    }

    fun onPaymentSuccess() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("CheckoutViewModel", "Usuário não logado, não é possível dar pontos.")
                cartRepository.clearCart()
                _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                return@launch
            }

            val totalAmount = cartTotal.value
            val pointsToAward = (totalAmount / 10).toLong()

            if (pointsToAward <= 0) {
                Log.d("CheckoutViewModel", "Compra de valor $totalAmount não gera pontos.")
                cartRepository.clearCart()
                _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                return@launch
            }

            val userDocRef = firestore.collection("users").document(currentUser.uid)

            userDocRef.update("points", FieldValue.increment(pointsToAward))
                .addOnSuccessListener {
                    Log.d("CheckoutViewModel", "$pointsToAward pontos adicionados para o usuário ${currentUser.uid}")
                    cartRepository.clearCart()
                    _uiState.update { it.copy(isPaymentSuccessful = true, paymentInfo = null) }
                }
                .addOnFailureListener { e ->
                    Log.e("CheckoutViewModel", "Erro ao adicionar pontos para ${currentUser.uid}", e)
                    cartRepository.clearCart()
                    _uiState.update { s -> s.copy(error = "Pagamento aprovado, mas houve um erro ao creditar seus pontos. Contate o suporte.", isPaymentSuccessful = true, paymentInfo = null) }
                }
        }
    }

    fun onPaymentResultHandled() {
        _uiState.update { it.copy(paymentInfo = null) }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/downloads/DownloadsScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.downloads

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.theme.*
// CORREÇÃO: O nome do pacote foi corrigido de 'components' para 'componets'
import com.jefferson.antenas.ui.componets.TopAppBarCustom

@Composable
fun DownloadsScreen(onBackClick: () -> Unit) {
    var expandedBrand by remember { mutableStateOf<Int?>(null) }
    val brands = listOf(
        Triple(0, "Duosat", listOf("Joy S v2.8", "Blade HD v3.1")),
        Triple(1, "AzAmerica", listOf("S1009 HD v1.9", "Champions v1.6")),
        Triple(2, "HTV", listOf("HTV 7 v4.2", "HTV 8 v1.0")),
    )

    Column(modifier = Modifier.fillMaxSize().background(MidnightBlueStart)) {
        TopAppBarCustom(title = "Downloads", onBackClick = onBackClick, showBack = false)

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(brands.size) { index ->
                val (id, name, models) = brands[index]
                Card(
                    onClick = { expandedBrand = if (expandedBrand == id) null else id },
                    colors = CardDefaults.cardColors(containerColor = CardGradientStart),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(name, color = TextPrimary, fontWeight = FontWeight.Bold)
                            Icon(if (expandedBrand == id) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, tint = SignalOrange)
                        }
                        if (expandedBrand == id) {
                            Spacer(Modifier.height(8.dp))
                            models.forEach { model ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(model, color = TextSecondary)
                                    Icon(Icons.Default.Download, null, tint = SuccessGreen)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/home/HomeScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.componets.*
import com.jefferson.antenas.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCartClick: () -> Unit,
    onServicesClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val products by viewModel.products.collectAsState()
    val cartCount by viewModel.cartItemCount.collectAsState()

    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    // ✅ BANNERS MODERNIZADOS - Relevantes para Antenas
    val banners: List<BannerItem> = listOf(
        BannerItem("1", "Antenas 4K Ultra HD", "Qualidade de imagem cristalina", "https://images.unsplash.com/photo-1518611505868-48510c2e1fb4?w=800&h=400&fit=crop", "Ver Modelos"),
        BannerItem("2", "Instalação Profissional", "Técnicos qualificados e experientes", "https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=800&h=400&fit=crop", "Agendar"),
        BannerItem("3", "Promoção 2026", "Desconto especial de até 25%", "https://images.unsplash.com/photo-1607082348824-0a96f2a4b9da?w=800&h=400&fit=crop", "Aproveitar"),
        BannerItem("4", "Receptores Inteligentes", "Controle total da sua TV", "https://images.unsplash.com/photo-1611532736579-6b16e2b50449?w=800&h=400&fit=crop", "Conhecer")
    )

    val reviews: List<ReviewItem> = listOf(
        ReviewItem("1", "Carlos Silva", 5, "Entrega super rápida!", "20/12"),
        ReviewItem("2", "Ana Souza", 5, "O técnico foi muito atencioso.", "18/12"),
        ReviewItem("3", "Roberto Lima", 4, "Produto original.", "15/12")
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MidnightBlueStart,
            topBar = {
                TopAppBar(
                    title = { Text("Jefferson Antenas", color = TextPrimary, fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MidnightBlueStart),
                    actions = {
                        IconButton(onClick = onSearchClick) {
                            Icon(Icons.Default.Search, contentDescription = "Buscar Produtos", tint = TextPrimary)
                        }
                        CartAppBarAction(cartCount = cartCount, onCartClick = onCartClick)
                        IconButton(onClick = onProfileClick) {
                            Icon(Icons.Default.Person, contentDescription = "Perfil", tint = TextPrimary)
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // ✅ BANNER COM AUTO-SCROLL
                HeroCarouselModernized(banners = banners)

                // ✅ BADGES MODERNIZADAS
                TrustBadgesModernized()

                // ✅ CARD DE SERVIÇOS COM ÍCONE ESCURO
                ServiceCallToActionCard_Interactive(onClick = onServicesClick)

                Row(modifier = Modifier.padding(16.dp)) {
                    Text("Destaques", style = MaterialTheme.typography.titleLarge, color = TextPrimary, fontWeight = FontWeight.Bold)
                }

                if (products.isEmpty()) {
                    repeat(4) {
                        ShimmerProductCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                    }
                } else {
                    products.take(4).forEach { product ->
                        ProductCard(
                            product = product,
                            onAddToCart = {
                                viewModel.addToCart(it)
                                showToast = true
                            },
                            onClick = { onProductClick(product.id) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    OutlinedButton(
                        onClick = { onCartClick() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SignalOrange),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SignalOrange)
                    ) { Text("Ver Loja Completa") }
                }
                ReviewsCarousel(reviews = reviews)
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        ModernSuccessToast(
            visible = showToast,
            message = "Item adicionado!",
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

// ✅ BANNER COM AUTO-SCROLL AUTOMÁTICO
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HeroCarouselModernized(banners: List<BannerItem>, modifier: Modifier = Modifier) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()

    // ✅ Auto-scroll automático a cada 5 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(5000)
            val nextPage = (pagerState.currentPage + 1) % banners.size
            coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(220.dp).padding(16.dp)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp))
        ) { page ->
            val banner = banners[page]
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = banner.imageUrl,
                    contentDescription = banner.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Gradiente para leitura
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )

                Column(modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(banner.title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(banner.subtitle, fontSize = 13.sp, color = Color.LightGray)

                    Surface(
                        color = SignalOrange,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            banner.buttonText,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MidnightBlueStart,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // ✅ Indicadores (Bolinhas) com estilo melhorado
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 8.dp else 6.dp)
                        .background(if (pagerState.currentPage == index) SignalOrange else Color.Gray, CircleShape)
                )
            }
        }
    }
}

// ✅ BADGES MODERNIZADAS - 4 ITENS EM UMA LINHA
@Composable
fun TrustBadgesModernized(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BadgeItemModernized(Icons.Default.VerifiedUser, "Garantia", "3 Meses", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.LocalShipping, "Entrega", "Rápida", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.Lock, "Seguro", "100%", modifier = Modifier.weight(1f))
        BadgeItemModernized(Icons.Default.HeadsetMic, "Suporte", "24h", modifier = Modifier.weight(1f))
    }
}

@Composable
fun BadgeItemModernized(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    sub: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = SignalOrange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(sub, fontSize = 10.sp, color = SignalOrange, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ✅ CARD DE SERVIÇOS COM ÍCONE ESCURO
@Composable
fun ServiceCallToActionCard_Interactive(onClick: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val cardElevation by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 6.dp,
        label = "CardElevation"
    )

    val iconSize by animateDpAsState(
        targetValue = if (isPressed) 38.dp else 36.dp,
        label = "IconSize"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                isPressed = true
                onClick()
            }
            .shadow(
                elevation = cardElevation,
                shape = RoundedCornerShape(16.dp),
                clip = true
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SignalOrange.copy(alpha = 0.85f),
                            SignalOrangeDark.copy(alpha = 0.85f)
                        )
                    )
                )
                .padding(vertical = 18.dp, horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ✅ ÍCONE COM COR DO FUNDO DO APP
                Surface(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = MidnightBlueStart  // ✅ Cor do fundo do app
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = SignalOrange,  // Ícone em laranja para contraste
                            modifier = Modifier.size(iconSize)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Serviços de Instalação",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Peça um orçamento gratuito",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }

                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/home/HomeUiState.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/home/HomeViewModel.kt
// ============================================
package com.jefferson.antenas.ui.screens.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.remote.JeffersonApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val api: JeffersonApi,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    private val _cartItemCount = MutableStateFlow(0)
    val cartItemCount: StateFlow<Int> = _cartItemCount.asStateFlow()

    init {
        Log.d("HomeViewModel", "📦 HomeViewModel inicializado")
        val startTime = System.currentTimeMillis()

        fetchProducts()
        updateCartCount()

        Log.d("HomeViewModel", "✅ Init concluído em ${System.currentTimeMillis() - startTime}ms")
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            try {
                val startTime = System.currentTimeMillis()
                Log.d("HomeViewModel", "🌐 Iniciando fetch de produtos...")

                val result = api.getProducts()

                val fetchTime = System.currentTimeMillis() - startTime
                Log.d("HomeViewModel", "✅ Produtos carregados em ${fetchTime}ms - Total: ${result.size} itens")

                _products.value = result
            } catch (e: Exception) {
                Log.e("HomeViewModel", "❌ ERRO ao buscar produtos: ${e.message}", e)
                _products.value = emptyList()
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            Log.d("HomeViewModel", "🛒 Adicionando ao carrinho: ${product.name}")
            cartRepository.addToCart(product)
        }
    }

    private fun updateCartCount() {
        Log.d("HomeViewModel", "👁️ Observando carrinho...")
        cartRepository.items.onEach { items ->
            _cartItemCount.value = items.sumOf { it.quantity }
            Log.d("HomeViewModel", "🔄 Carrinho atualizado - Count: ${_cartItemCount.value}")
        }.launchIn(viewModelScope)
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/product/ProductDetailScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.jefferson.antenas.ui.componets.ModernSuccessToast
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.toCurrency
import kotlinx.coroutines.delay

@Composable
fun ProductDetailScreen(
    onBackClick: () -> Unit,
    viewModel: ProductDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var quantity by remember { mutableStateOf(1) }

    var showToast by remember { mutableStateOf(false) }

    if (showToast) {
        LaunchedEffect(showToast) {
            delay(2000)
            showToast = false
        }
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            if (uiState is ProductUiState.Success) {
                val product = (uiState as ProductUiState.Success).product
                TopAppBarCustom(title = product.name, onBackClick = onBackClick)
            }
        },
        bottomBar = {
            if (uiState is ProductUiState.Success) {
                val product = (uiState as ProductUiState.Success).product
                BottomPurchaseBar(
                    price = product.getDiscountedPrice() * quantity,
                    quantity = quantity,
                    onQuantityChange = { newQuantity ->
                        if (newQuantity > 0) {
                            quantity = newQuantity
                        }
                    },
                    onAddToCart = {
                        viewModel.addToCart(product, quantity)
                        showToast = true
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(brush = BackgroundGradient)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SignalOrange
                    )
                }
                is ProductUiState.Error -> {
                    Text(
                        text = state.message,
                        color = ErrorRed,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProductUiState.Success -> {
                    val product = state.product

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .background(MidnightBlueCard)
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = product.category?.uppercase() ?: "GERAL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SatelliteBlue
                                )
                                if (product.isNew == true) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        color = SignalOrange,
                                        shape = Shapes.small
                                    ) {
                                        Text(
                                            text = "NOVO",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MidnightBlueStart
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.name,
                                style = MaterialTheme.typography.headlineLarge,
                                color = TextPrimary,
                                fontSize = 26.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            if (product.discount != null && product.discount > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "-${product.discount}%",
                                        color = ErrorRed,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(ErrorRed.copy(alpha = 0.1f), Shapes.small)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = product.price.toCurrency(),
                                        style = MaterialTheme.typography.titleMedium,
                                        textDecoration = TextDecoration.LineThrough,
                                        color = TextTertiary
                                    )
                                }
                            }
                            Text(
                                text = product.getDiscountedPrice().toCurrency(),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = SignalOrange
                                ),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 24.dp),
                                color = CardBorder
                            )
                            Text(
                                text = "Sobre o Produto",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary,
                                lineHeight = 24.sp
                            )
                            Spacer(modifier = Modifier.height(100.dp))
                        }
                    }
                }
            }

            // CORREÇÃO: Toast movido para dentro do Box para ter o escopo correto
            ModernSuccessToast(
                visible = showToast,
                message = "Item adicionado!",
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun BottomPurchaseBar(
    price: Double,
    quantity: Int,
    onQuantityChange: (Int) -> Unit,
    onAddToCart: () -> Unit
) {
    Surface(
        color = MidnightBlueCard,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onQuantityChange(quantity - 1) }) {
                    Icon(Icons.Default.Remove, contentDescription = "Remover um", tint = TextPrimary)
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { onQuantityChange(quantity + 1) }) {
                    Icon(Icons.Default.Add, contentDescription = "Adicionar um", tint = TextPrimary)
                }
            }
            Button(
                onClick = onAddToCart,
                colors = ButtonDefaults.buttonColors(containerColor = SignalOrange),
                shape = Shapes.medium,
                modifier = Modifier.height(50.dp)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = MidnightBlueStart)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Comprar Agora",
                    color = MidnightBlueStart,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/product/ProductDetailViewModel.kt
// ============================================
package com.jefferson.antenas.ui.screens.product

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jefferson.antenas.data.model.Product
import com.jefferson.antenas.data.repository.CartRepository
import com.jefferson.antenas.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ProductUiState {
    data object Loading : ProductUiState
    data class Success(val product: Product) : ProductUiState
    data class Error(val message: String) : ProductUiState
}

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val repository: ProductRepository,
    private val cartRepository: CartRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val productId: String = checkNotNull(savedStateHandle["productId"])

    init {
        loadProduct()
    }

    fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { ProductUiState.Loading }

            val result = repository.getProductById(productId)

            result.onSuccess { product ->
                if (product != null) {
                    _uiState.update { ProductUiState.Success(product) }
                } else {
                    _uiState.update { ProductUiState.Error("Produto não encontrado") }
                }
            }.onFailure { error ->
                _uiState.update { ProductUiState.Error(error.localizedMessage ?: "Erro de conexão") }
            }
        }
    }

    fun addToCart(product: Product, quantity: Int) {
        cartRepository.addToCart(product, quantity)
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/profile/ProfileScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.theme.MidnightBlueEnd
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueEnd),
        contentAlignment = Alignment.Center
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(color = SignalOrange)
            }
            uiState.error != null -> {
                Text(text = uiState.error!!, color = Color.Red)
            }
            uiState.user != null -> {
                val user = uiState.user!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(32.dp))

                    // Ícone do Usuário
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(SignalOrange),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.name.firstOrNull()?.uppercase() ?: "U",
                            fontSize = 60.sp,
                            fontWeight = FontWeight.Bold,
                            color = MidnightBlueEnd
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nome do Usuário
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimary
                    )

                    // Email
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Email, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Cartão de Pontos de Fidelidade
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SignalOrange)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "SEU SALDO DE PONTOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MidnightBlueEnd
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = MidnightBlueEnd, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.size(8.dp))
                                Text(
                                    text = user.points.toString(),
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MidnightBlueEnd
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f)) // Empurra o botão para baixo

                    // Botão de Logout
                    Button(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = SignalOrange),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sair")
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Sair da Conta")
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/profile/ProfileViewModel.kt
// ============================================
package com.jefferson.antenas.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jefferson.antenas.data.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            _uiState.update { it.copy(isLoading = false, error = "Nenhum usuário logado.") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }
        firestore.collection("users").document(firebaseUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val user = document.toObject(User::class.java)
                    _uiState.update { it.copy(isLoading = false, user = user) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Perfil de usuário não encontrado.") }
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false, error = "Erro ao buscar perfil: ${e.message}") }
            }
    }

    fun logout() {
        auth.signOut()
        _uiState.update { it.copy(isLoggedOut = true) }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/search/SearchScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jefferson.antenas.ui.componets.ProductCard
import com.jefferson.antenas.ui.componets.SearchAppBar
import com.jefferson.antenas.ui.screens.home.HomeViewModel
import com.jefferson.antenas.ui.theme.MidnightBlueStart

@Composable
fun SearchScreen(
    onBackClick: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val allProducts by viewModel.products.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    // Filtra os produtos em tempo real baseado na busca
    val filteredProducts = if (searchQuery.isBlank()) {
        emptyList() // Não mostra nada se a busca estiver vazia
    } else {
        allProducts.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.description.contains(searchQuery, ignoreCase = true) ||
                    it.category?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            SearchAppBar(
                searchQuery = searchQuery,
                onQueryChange = { searchQuery = it },
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier.fillMaxSize().padding(padding),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(filteredProducts) { product ->
                ProductCard(
                    product = product,
                    onAddToCart = { productToAdd -> viewModel.addToCart(productToAdd) },
                    onClick = { onProductClick(product.id) }
                )
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/services/ServicesScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.services

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import java.net.URLEncoder

// Modelo de dados simples para os serviços
data class ServiceItem(val title: String, val description: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServicesScreen(onBackClick: () -> Unit) { // Adicionado onBackClick para consistência
    val context = LocalContext.current

    // Lista dos seus serviços
    val services = listOf(
        ServiceItem("Instalação Completa", "Instalação de antenas e cabeamento residencial.", Icons.Default.Settings),
        ServiceItem("Apontamento", "Reajuste de sinal para satélites (StarOne, Sky, etc).", Icons.Default.Router),
        ServiceItem("Manutenção", "Troca de conectores, cabos e reparos.", Icons.Default.Build),
        ServiceItem("Atualização", "Update de lista de canais e sistema do receptor.", Icons.Default.Tv)
    )

    Scaffold(
        containerColor = MidnightBlueStart,
        topBar = {
            // CORREÇÃO: Usando o componente padrão e passando a função onBackClick
            TopAppBarCustom(
                title = "Nossos Serviços",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            Text(
                "Escolha um serviço para orçar no WhatsApp:",
                color = TextSecondary, // CORREÇÃO: Usando cor do tema
                fontSize = 14.sp,
                modifier = Modifier.padding(16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(1),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(services.size) { index ->
                    ServiceCard(services[index]) { service ->
                        val phone = "5565992895296"
                        val message = "Olá Jefferson! Gostaria de um orçamento para: *${service.title}*"

                        try {
                            val url = "https://api.whatsapp.com/send?phone=$phone&text=${URLEncoder.encode(message, "UTF-8")}"
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data = Uri.parse(url)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Lidar com erro se o WhatsApp não estiver instalado
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceCard(service: ServiceItem, onClick: (ServiceItem) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick(service) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MidnightBlueCard), // CORREÇÃO: Usando cor do tema
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = service.icon,
                contentDescription = null,
                tint = SignalOrange, // CORREÇÃO: Usando cor do tema
                modifier = Modifier.size(40.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(service.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary) // CORREÇÃO
                Spacer(modifier = Modifier.height(4.dp))
                Text(service.description, fontSize = 13.sp, color = TextSecondary, lineHeight = 18.sp) // CORREÇÃO
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/splash/SplashScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.jefferson.antenas.R
import com.jefferson.antenas.ui.theme.MidnightBlueStart
import com.jefferson.antenas.ui.theme.SignalOrange
import com.jefferson.antenas.ui.theme.TextPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    // 1. Carrega o arquivo JSON (Certifique-se que o nome é splash_anim.json na pasta raw)
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_anim))

    // 2. Estados de Animação APENAS para o Texto
    val textAlpha = remember { Animatable(0f) } // Começa invisível
    val textScale = remember { Animatable(0.8f) } // Começa um pouco menor

    LaunchedEffect(key1 = true) {
        // Inicia a animação dos textos
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        launch {
            textScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }

        // Tempo total que a Splash fica na tela (3.5 segundos)
        delay(4000)
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart), // Fundo Azul Escuro Oficial
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            // 📡 AQUI ENTRA O SEU JSON (Já animado)
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever, // Fica repetindo enquanto carrega
                modifier = Modifier
                    .size(350.dp) // Tamanho da animação (Ajuste se ficar grande/pequeno)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ✍️ TÍTULO (Animado via Código: Fade In + Zoom)
            Text(
                text = "Jefferson Antenas",
                color = TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ✍️ SUBTÍTULO (Animado igual)
            Text(
                text = "Conectando você ao mundo",
                color = SignalOrange,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .scale(textScale.value)
            )
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/store/StoreScreen.kt
// ============================================
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

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/screens/support/SupportScreen.kt
// ============================================
package com.jefferson.antenas.ui.screens.support

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jefferson.antenas.ui.componets.TopAppBarCustom
import com.jefferson.antenas.ui.theme.*
import com.jefferson.antenas.utils.WhatsAppHelper

@Composable
fun SupportScreen(
    onBackClick: () -> Unit
) {
    var activeTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightBlueStart)
    ) {
        TopAppBarCustom(title = "Suporte", onBackClick = onBackClick, showBack = true)

        TabRow(
            selectedTabIndex = activeTab,
            containerColor = CardGradientStart,
            contentColor = SignalOrange,
            divider = {}
        ) {
            listOf("Ajuda", "Meus Chamados", "FAQ").forEachIndexed { index, title ->
                Tab(
                    selected = activeTab == index,
                    onClick = { activeTab = index },
                    text = { Text(title, fontSize = 14.sp, fontWeight = if(activeTab == index) FontWeight.Bold else FontWeight.Normal) },
                    selectedContentColor = SignalOrange,
                    unselectedContentColor = TextSecondary
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when (activeTab) {
                0 -> HelpTab()
                1 -> TicketsTab()
                2 -> FaqTab()
            }
        }
    }
}

@Composable
fun HelpTab() {
    // --- LÓGICA DO WHATSAPP ---
    val context = LocalContext.current
    // IMPORTANTE: Substitua pelo seu número de telefone com código do país (ex: 55119XXXXXXXX)
    val supportPhoneNumber = "55SEUNUMEROAQUI"
    val defaultMessage = "Olá! Vim pelo app Jefferson Antenas e preciso de ajuda."

    Text("Solucionador de Problemas", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    val items = listOf(
        "Sem Sinal?" to "Verifique os cabos e o apontamento da antena.",
        "Canal Codificado?" to "Verifique sua conexão com a internet e assinatura.",
        "Controle não funciona?" to "Troque as pilhas ou verifique o sensor.",
        "Imagem travando?" to "Reinicie o roteador e o receptor."
    )

    items.forEach { (title, desc) ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardGradientStart)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(desc, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }

    Button(
        onClick = { 
            WhatsAppHelper.openWhatsApp(context, supportPhoneNumber, defaultMessage)
        },
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Phone, contentDescription = null, tint = TextPrimary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Falar no WhatsApp", color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun TicketsTab() {
    Text("Meus Chamados", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardGradientStart)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text("#TK001", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = SignalOrange)
                Surface(color = SuccessGreen, shape = RoundedCornerShape(4.dp)) {
                    Text("Resolvido", fontSize = 10.sp, color = TextPrimary, modifier = Modifier.padding(4.dp))
                }
            }
            Text("Receptor não atualiza", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun FaqTab() {
    Text("Perguntas Frequentes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

    val faqs = listOf(
        "Qual a garantia?" to "6 meses contra defeitos de fabricação.",
        "Fazem instalação?" to "Sim, agende pelo app ou WhatsApp.",
        "Aceitam cartão?" to "Sim, parcelamos em até 12x."
    )

    faqs.forEach { (q, a) ->
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = CardGradientStart)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(q, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text(a, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}


// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/theme/Color.kt
// ============================================
package com.jefferson.antenas.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// --- CORES SÓLIDAS ---

// Tons Escuros (Fundo)
val MidnightBlueStart = Color(0xFF0F172A)
val MidnightBlueEnd = Color(0xFF1E293B)
val MidnightBlueCard = Color(0xFF1E293B)

// Laranja (Destaque Principal)
val SignalOrange = Color(0xFFF59E0B)
val SignalOrangeDark = Color(0xFFD97706)

// Azul (Secundário/Tech)
val SatelliteBlue = Color(0xFF3B82F6)
val SatelliteBlueDark = Color(0xFF2563EB)

// Status
val SuccessGreen = Color(0xFF10B981)
val ErrorRed = Color(0xFFEF4444)
val WarningYellow = Color(0xFFFBBF24)
val AccentPink = Color(0xFFEC4899)

// Textos
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFCBD5E1)
val TextTertiary = Color(0xFF94A3B8)

// Bordas e Cartões
val CardBorder = Color(0xFF3D4A5C)
val CardGradientStart = Color(0xFF2A3544)
val CardGradientEnd = Color(0xFF1E2836)

// --- DEGRADÊS (GRADIENTS) ---

// Fundo dos Cartões (Efeito Premium)
val CardPremiumGradient = Brush.verticalGradient(
    colors = listOf(CardGradientStart, CardGradientEnd)
)

// Botões Principais (Laranja Brilhante)
val PrimaryButtonGradient = Brush.horizontalGradient(
    colors = listOf(SignalOrange, SignalOrangeDark)
)

// Fundo Geral das Telas (CORREÇÃO AQUI)
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(MidnightBlueStart, MidnightBlueEnd)
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/theme/Shape.kt
// ============================================
package com.jefferson.antenas.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(8.dp),   // Botões pequenos, inputs
    medium = RoundedCornerShape(12.dp), // Botões principais, cartões simples
    large = RoundedCornerShape(16.dp),  // Cards de produto, Modais
    extraLarge = RoundedCornerShape(24.dp) // Bottom Sheet, Paineis grandes
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/theme/Theme.kt
// ============================================
package com.jefferson.antenas.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Configuração das Cores do Tema
private val DarkColorScheme = darkColorScheme(
    primary = SignalOrange,
    secondary = SatelliteBlue,
    tertiary = AccentPink,

    // CORRIGIDO AQUI:
    background = MidnightBlueStart, // Cor do fundo da tela
    surface = MidnightBlueEnd,      // Cor do fundo dos cartões (antes estava MidnightBlueSurface)

    onPrimary = MidnightBlueStart,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    error = ErrorRed
)

// Por enquanto, vamos usar o mesmo esquema para o modo claro (App sempre escuro)
private val LightColorScheme = DarkColorScheme

@Composable
fun JeffersonAntenasAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color (Android 12+) desligado para manter a identidade da sua marca
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Pinta a barra de status com a cor do fundo
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/ui/theme/Type.kt
// ============================================
package com.jefferson.antenas.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Definindo a tipografia padrão do Material 3 mas com os teus ajustes
val Typography = Typography(
    // Títulos Grandes (Ex: Hero Banner)
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    // Títulos de Seção (Ex: "Vistos Recentemente")
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
        color = TextPrimary
    ),
    // Nomes de Produtos nos Cards
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
        color = TextPrimary
    ),
    // Texto Normal
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = TextSecondary
    ),
    // Texto Pequeno (Legendas, datas)
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = TextSecondary
    ),
    // Texto Muito Pequeno (Badges, notas de rodapé)
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = TextTertiary
    )
)

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/utils/ErrorMessageHandler.kt
// ============================================
package com.jefferson.antenas.utils

/**
 * Converte mensagens de erro do Firebase (inglês) para português profissional
 *
 * Uso:
 * catch (e: Exception) {
 *     val mensagem = ErrorMessageHandler.tratarErro(e)
 *     Toast.makeText(context, mensagem, Toast.LENGTH_LONG).show()
 * }
 */
object ErrorMessageHandler {

    /**
     * Traduz exceções do Firebase para mensagens em português
     *
     * @param exception A exceção capturada
     * @return Mensagem amigável em português
     */
    fun tratarErro(exception: Exception?): String {
        if (exception == null) {
            return "Erro desconhecido. Tente novamente."
        }

        val mensagem = exception.message ?: ""

        // Ordena por likelihood (mais comuns primeiro)
        return when {
            // ❌ LOGIN / AUTENTICAÇÃO
            mensagem.contains("incorrect", ignoreCase = true) ||
                    mensagem.contains("malformed", ignoreCase = true) ||
                    mensagem.contains("auth credential", ignoreCase = true) -> {
                "Email ou senha incorretos. Verifique e tente novamente."
            }

            mensagem.contains("no user", ignoreCase = true) ||
                    mensagem.contains("There is no user record", ignoreCase = true) -> {
                "Usuário não encontrado. Verifique o email ou cadastre-se."
            }

            mensagem.contains("password is invalid", ignoreCase = true) ||
                    mensagem.contains("wrong password", ignoreCase = true) -> {
                "Senha incorreta. Tente novamente."
            }

            // ❌ CADASTRO
            mensagem.contains("already in use", ignoreCase = true) ||
                    mensagem.contains("email already exists", ignoreCase = true) -> {
                "Este email já está cadastrado. Faça login ou use outro email."
            }

            mensagem.contains("invalid email", ignoreCase = true) ||
                    mensagem.contains("badly formatted", ignoreCase = true) -> {
                "Email inválido. Verifique o formato."
            }

            mensagem.contains("password too short", ignoreCase = true) ||
                    mensagem.contains("weak password", ignoreCase = true) -> {
                "Senha muito fraca. Use pelo menos 6 caracteres."
            }

            // ❌ CONECTIVIDADE
            mensagem.contains("network", ignoreCase = true) ||
                    mensagem.contains("connection", ignoreCase = true) ||
                    mensagem.contains("timeout", ignoreCase = true) ||
                    mensagem.contains("unreachable", ignoreCase = true) -> {
                "Sem conexão com a internet. Verifique sua conexão."
            }

            mensagem.contains("User not found", ignoreCase = true) -> {
                "Usuário não encontrado. Cadastre-se primeiro."
            }

            // ❌ THROTTLING / RATE LIMIT
            mensagem.contains("too many requests", ignoreCase = true) ||
                    mensagem.contains("too many failed login", ignoreCase = true) -> {
                "Muitas tentativas. Aguarde alguns minutos e tente novamente."
            }

            // ❌ SERVIDOR
            mensagem.contains("internal error", ignoreCase = true) ||
                    mensagem.contains("server error", ignoreCase = true) -> {
                "Erro no servidor. Tente novamente mais tarde."
            }

            // ❌ GENÉRICO
            else -> {
                "Erro ao processar sua solicitação. Tente novamente."
            }
        }
    }

    /**
     * Versão simplificada que retorna uma descrição curta
     */
    fun obterMensagemCurta(exception: Exception?): String {
        val mensagem = tratarErro(exception)
        // Se for muito longa, trunca
        return if (mensagem.length > 60) {
            mensagem.take(57) + "..."
        } else {
            mensagem
        }
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/utils/Extensions.kt
// ============================================
package com.jefferson.antenas.utils

import com.jefferson.antenas.data.model.Product
import java.text.NumberFormat
import java.util.Locale

// --- Calculadora de Desconto (CORRIGIDA) ---
fun Product.getDiscountedPrice(): Double {
    // 1. Limpa o texto e transforma em número (Blinda contra erros)
    val priceString = this.price.toString()
        .replace("R$", "")
        .trim()
        .replace(",", ".") // Troca vírgula por ponto para o sistema entender

    val priceDouble = priceString.toDoubleOrNull() ?: 0.0

    // 2. Agora sim fazemos a conta matemática
    return if (this.discount != null && this.discount > 0) {
        priceDouble * (1 - this.discount / 100.0)
    } else {
        priceDouble
    }
}

// --- Formatadores de Moeda ---
fun Double.toCurrency(): String {
    val ptBr = Locale("pt", "BR")
    return NumberFormat.getCurrencyInstance(ptBr).format(this)
}

fun String.toCurrency(): String {
    val value = this
        .trim()
        .replace("R$", "")
        .replace(" ", "")
        .replace(".", "") // Remove pontos de milhar (ex: 1.000 -> 1000)
        .replace(",", ".") // Troca vírgula decimal por ponto
        .toDoubleOrNull()
        ?: 0.0

    return value.toCurrency()
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/utils/ValidationUtils.kt
// ============================================
package com.jefferson.antenas.utils

/**
 * Utilitário centralizado para validações de entrada do usuário
 *
 * Usado em: AuthViewModel, SignUpScreen, LoginScreen
 */
object ValidationUtils {

    // Regex para validação de email
    // Aceita: user@example.com, user+tag@example.co.uk, etc
    private val EMAIL_REGEX = Regex(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    )

    /**
     * Valida se um email está no formato correto
     *
     * @param email String a validar
     * @return true se email é válido, false caso contrário
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && email.matches(EMAIL_REGEX)
    }

    /**
     * Valida se uma senha atende aos requisitos mínimos
     * Requisito: mínimo 6 caracteres
     *
     * @param password String a validar
     * @return true se senha é válida, false caso contrário
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Valida se o nome não está vazio
     *
     * @param name String a validar
     * @return true se nome é válido, false caso contrário
     */
    fun isValidName(name: String): Boolean {
        return name.isNotBlank()
    }

    /**
     * Retorna mensagem de erro para senha
     * Útil para exibir ao usuário
     *
     * @param password String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getPasswordError(password: String): String? {
        return when {
            password.isEmpty() -> null // Campo vazio, não mostra erro ainda
            password.length < 6 -> "Senha deve ter pelo menos 6 caracteres"
            else -> null
        }
    }

    /**
     * Retorna mensagem de erro para email
     * Útil para exibir ao usuário
     *
     * @param email String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getEmailError(email: String): String? {
        return when {
            email.isEmpty() -> null // Campo vazio, não mostra erro ainda
            !isValidEmail(email) -> "Email inválido"
            else -> null
        }
    }

    /**
     * Retorna mensagem de erro para nome
     *
     * @param name String a validar
     * @return null se válido, mensagem de erro caso contrário
     */
    fun getNameError(name: String): String? {
        return when {
            name.isEmpty() -> null // Campo vazio, não mostra erro ainda
            name.length < 3 -> "Nome deve ter pelo menos 3 caracteres"
            else -> null
        }
    }

    /**
     * Valida todos os campos de SignUp de uma vez
     * Retorna lista de erros (vazia se tudo ok)
     *
     * @param name Nome do usuário
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Lista com mensagens de erro (vazia se válido)
     */
    fun validateSignUp(name: String, email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        // Validar nome
        if (!isValidName(name)) {
            errors.add("Nome é obrigatório")
        } else if (name.length < 3) {
            errors.add("Nome deve ter pelo menos 3 caracteres")
        }

        // Validar email
        if (!isValidEmail(email)) {
            errors.add("Email inválido")
        }

        // Validar senha
        if (!isValidPassword(password)) {
            errors.add("Senha deve ter pelo menos 6 caracteres")
        }

        return errors
    }

    /**
     * Valida todos os campos de Login de uma vez
     *
     * @param email Email do usuário
     * @param password Senha do usuário
     * @return Lista com mensagens de erro (vazia se válido)
     */
    fun validateLogin(email: String, password: String): List<String> {
        val errors = mutableListOf<String>()

        if (!isValidEmail(email)) {
            errors.add("Email inválido")
        }

        if (password.isEmpty()) {
            errors.add("Senha é obrigatória")
        }

        return errors
    }
}

// ============================================
// Arquivo: ./app/src/main/java/com/jefferson/antenas/utils/WhatsAppHelper.kt
// ============================================
package com.jefferson.antenas.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object WhatsAppHelper {

    fun openWhatsApp(context: Context, phoneNumber: String, message: String) {
        try {
            // Garante que o número de telefone está no formato correto (com código do país, sem +)
            val formattedPhoneNumber = phoneNumber.replace("+", "").replace(" ", "")
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhoneNumber&text=${Uri.encode(message)}")
                // O pacote "com.whatsapp" garante que abrirá o WhatsApp diretamente
                setPackage("com.whatsapp")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Se o WhatsApp não estiver instalado, informa ao usuário.
            Toast.makeText(context, "WhatsApp não encontrado.", Toast.LENGTH_SHORT).show()
        }
    }
}


// ============================================
// Arquivo: ./app/src/test/java/com/jefferson/antenas/ExampleUnitTest.kt
// ============================================
package com.jefferson.antenas

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}

// ============================================
// Arquivo: ./codigo_consolidado.kt
// ============================================


