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
import com.google.firebase.auth.FirebaseAuth
import com.jefferson.antenas.ui.screens.auth.AuthViewModel
import com.jefferson.antenas.ui.screens.auth.LoginScreen
import com.jefferson.antenas.ui.screens.auth.SignUpScreen
import com.jefferson.antenas.ui.screens.cart.CartScreen
import com.jefferson.antenas.ui.screens.checkout.CheckoutScreen
import com.jefferson.antenas.ui.screens.downloads.DownloadsScreen
import com.jefferson.antenas.ui.screens.favorites.FavoritesScreen
import com.jefferson.antenas.ui.screens.home.HomeScreen
import com.jefferson.antenas.ui.screens.orders.OrdersScreen
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

    val hideBottomBarRoutes = listOf("splash", "login", "signup", "checkout", "product/{productId}", "cart", "search", "orders", "favorites")

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
                        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
                        val destination = if (isLoggedIn) "home" else "login"
                        navController.navigate(destination) {
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
                    onProfileClick = { navController.navigate("profile") },
                    onStoreClick = { navController.navigate("store") }
                )
            }
            composable("store") {
                StoreScreen(
                    onProductClick = { id -> navController.navigate("product/$id") },
                    onCartClick = { navController.navigate("cart") },
                    onServicesClick = { navController.navigate("services") },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable("services") { ServicesScreen(onBackClick = { navController.popBackStack() }) }
            composable("downloads") { DownloadsScreen(onBackClick = { navController.popBackStack() }) }
            composable("support") { SupportScreen(onBackClick = { navController.popBackStack() }) }
            composable("orders") {
                OrdersScreen(
                    onBackClick = { navController.popBackStack() },
                    onShopClick = { navController.navigate("store") }
                )
            }
            composable("favorites") {
                FavoritesScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { id -> navController.navigate("product/$id") },
                    onShopClick = { navController.navigate("store") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    onLogout = {
                        navController.navigate("login") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        }
                    },
                    onOrdersClick = { navController.navigate("orders") },
                    onFavoritesClick = { navController.navigate("favorites") },
                    onDownloadsClick = { navController.navigate("downloads") },
                    onSupportClick = { navController.navigate("support") },
                    onFaqClick = { navController.navigate("support") }
                )
            }
            composable("search") {
                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onProductClick = { id -> navController.navigate("product/$id") }
                )
            }
            composable(
                route = "product/{productId}",
                arguments = listOf(navArgument("productId") { type = NavType.StringType })
            ) {
                ProductDetailScreen(onBackClick = { navController.popBackStack() })
            }
            composable("cart") {
                CartScreen(
                    onBackClick = { navController.popBackStack() },
                    onCheckoutClick = { navController.navigate("checkout") },
                    onGoToStore = { navController.navigate("store") }
                )
            }
            composable("checkout") {
                CheckoutScreen(
                    onBackClick = { navController.popBackStack() },
                    onOrderSuccess = {
                        navController.navigate("home") { popUpTo("home") { inclusive = true } }
                    }
                )
            }
        }
    }
}