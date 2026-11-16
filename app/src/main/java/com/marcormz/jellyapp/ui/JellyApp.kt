package com.marcormz.jellyapp.ui

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.marcormz.jellyapp.JellyApplication
import com.marcormz.jellyapp.analytics.JellyAnalytics
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.ui.components.LocalCurrentScreen
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.screens.AccountScreen
import com.marcormz.jellyapp.ui.screens.CartScreen
import com.marcormz.jellyapp.ui.screens.CheckoutScreen
import com.marcormz.jellyapp.ui.screens.HomeScreen
import com.marcormz.jellyapp.ui.screens.ProductDetailScreen
import com.marcormz.jellyapp.ui.screens.ProductsScreen
import com.marcormz.jellyapp.ui.screens.ThankYouScreen
import com.marcormz.jellyapp.ui.screens.WebViewScreen
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBlack
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted

object Routes {
    const val HOME = "home"
    const val PRODUCTS = "products"
    const val PRODUCT_DETAIL = "product/{productId}"
    const val CART = "cart"
    const val CHECKOUT = "checkout"
    const val THANK_YOU = "thank_you"
    const val WEBVIEW = "webview"
    const val ACCOUNT = "account"

    fun productDetail(sku: String): String = "product/${Uri.encode(sku)}"
}

private val TOP_LEVEL_ROUTES = setOf(
    Routes.HOME,
    Routes.PRODUCTS,
    Routes.CART,
    Routes.ACCOUNT,
)

private data class BottomDestination(
    val route: String,
    val index: String,
    val label: String,
    val buttonId: String,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.HOME, "01", "Inicio", "btn_nav_home"),
    BottomDestination(Routes.PRODUCTS, "02", "Productos", "btn_nav_products"),
    BottomDestination(Routes.CART, "03", "Carrito", "btn_nav_cart"),
    BottomDestination(Routes.ACCOUNT, "04", "Cuenta", "btn_nav_account"),
)

private fun String.toScreenName(): String = when (this) {
    Routes.PRODUCT_DETAIL -> "product_detail"
    else -> this
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun JellyApp() {
    val application = LocalContext.current.applicationContext as JellyApplication
    val analytics = application.analytics
    val viewModel: JellyViewModel = viewModel(
        factory = JellyViewModel.factory(analytics, application.stockBackend),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    val currentScreen = currentRoute.toScreenName()
    val showBottomBar = currentRoute in TOP_LEVEL_ROUTES

    LaunchedEffect(currentRoute) {
        analytics.trackScreen(currentScreen)
    }

    ComposableContent(
        currentScreen = currentScreen,
        analytics = analytics,
        modifier = Modifier.semantics { testTagsAsResourceId = true },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = JellyBlack,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (showBottomBar) {
                    JellyBottomBar(
                        currentRoute = currentRoute,
                        cartCount = state.itemCount,
                        onNavigate = { destination ->
                            navController.navigateTopLevel(destination)
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                composable(Routes.HOME) {
                    HomeScreen(
                        state = state,
                        onProducts = { navController.navigateTopLevel(Routes.PRODUCTS) },
                        onWebView = { navController.navigate(Routes.WEBVIEW) },
                        onAccount = { navController.navigateTopLevel(Routes.ACCOUNT) },
                    )
                }
                composable(Routes.PRODUCTS) {
                    ProductsScreen(
                        state = state,
                        onProductClick = { product ->
                            navController.navigate(Routes.productDetail(product.sku))
                        },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                    )
                }
                composable(
                    route = Routes.PRODUCT_DETAIL,
                    arguments = listOf(navArgument("productId") { type = NavType.StringType }),
                ) { entry ->
                    ProductDetailScreen(
                        productId = entry.arguments?.getString("productId").orEmpty(),
                        state = state,
                        onBack = { navController.popBackStack() },
                        onAddToCart = { product -> viewModel.addToCart(product) },
                        onViewCart = { navController.navigateTopLevel(Routes.CART) },
                    )
                }
                composable(Routes.CART) {
                    CartScreen(
                        state = state,
                        onTrackViewCart = viewModel::trackViewCart,
                        onIncrement = viewModel::increment,
                        onDecrement = viewModel::decrement,
                        onRemove = viewModel::remove,
                        onCheckout = { navController.navigate(Routes.CHECKOUT) },
                        onProducts = { navController.navigateTopLevel(Routes.PRODUCTS) },
                    )
                }
                composable(Routes.CHECKOUT) {
                    CheckoutScreen(
                        state = state,
                        onTrackBeginCheckout = viewModel::trackBeginCheckout,
                        onBack = { navController.popBackStack() },
                        onProducts = { navController.navigateTopLevel(Routes.PRODUCTS) },
                        onPlaceOrder = { shipping, payment ->
                            if (viewModel.completeCheckout(shipping, payment) != null) {
                                navController.navigate(Routes.THANK_YOU) {
                                    popUpTo(Routes.HOME)
                                    launchSingleTop = true
                                }
                            }
                        },
                    )
                }
                composable(Routes.THANK_YOU) {
                    ThankYouScreen(
                        state = state,
                        onHome = { navController.navigateHome() },
                        onProducts = { navController.navigateTopLevel(Routes.PRODUCTS) },
                    )
                }
                composable(Routes.WEBVIEW) {
                    WebViewScreen(onBack = { navController.popBackStack() })
                }
                composable(Routes.ACCOUNT) {
                    AccountScreen(
                        state = state,
                        onLogin = viewModel::markLoggedIn,
                        onLogout = viewModel::logout,
                    )
                }
            }
        }
    }
}

@Composable
private fun ComposableContent(
    currentScreen: String,
    analytics: JellyAnalytics,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalJellyAnalytics provides analytics,
        LocalCurrentScreen provides currentScreen,
        content = { Box(modifier = modifier) { content() } },
    )
}

@Composable
private fun JellyBottomBar(
    currentRoute: String,
    cartCount: Int,
    onNavigate: (String) -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(JellyBlack)
            .navigationBarsPadding(),
    ) {
        HorizontalDivider(color = JellyBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 68.dp),
        ) {
            bottomDestinations.forEach { destination ->
                val selected = destination.route == currentRoute
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 64.dp)
                        .testTag(destination.buttonId)
                        .selectable(selected = selected, role = Role.Tab) {
                            analytics?.trackButton(destination.buttonId, currentRoute.toScreenName())
                            onNavigate(destination.route)
                        }
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 18.dp, height = 3.dp)
                            .background(
                                if (selected) JellyAmber else JellyBlack,
                                RectangleShape,
                            ),
                    )
                    Text(
                        text = destination.index,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (selected) JellyAmber else JellyMuted,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = destination.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) JellyCream else JellyMuted,
                        )
                        if (destination.route == Routes.CART && cartCount > 0) {
                            Box(
                                modifier = Modifier
                                    .size(17.dp)
                                    .background(JellyAmber, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = cartCount.toString(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JellyBlack,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(Routes.HOME) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

private fun NavHostController.navigateHome() {
    navigate(Routes.HOME) {
        popUpTo(Routes.HOME) {
            inclusive = false
        }
        launchSingleTop = true
    }
}
