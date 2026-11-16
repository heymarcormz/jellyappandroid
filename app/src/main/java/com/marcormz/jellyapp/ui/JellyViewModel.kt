package com.marcormz.jellyapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.marcormz.jellyapp.analytics.JellyAnalytics
import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.CartMath
import com.marcormz.jellyapp.data.JellyOrder
import com.marcormz.jellyapp.data.PaymentOption
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.data.ShippingOption
import com.marcormz.jellyapp.data.StockBackend
import com.marcormz.jellyapp.data.StockCheckResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StockError(
    val attemptId: Long,
    val sku: String,
    val httpStatus: Int?,
    val errorCode: String,
    val message: String,
)

data class JellyUiState(
    val cart: List<CartLine> = emptyList(),
    val loggedIn: Boolean = false,
    val displayName: String = "Invitado Jelly",
    val activeUserId: String? = null,
    val pendingStockSku: String? = null,
    val stockError: StockError? = null,
    val lastOrder: JellyOrder? = null,
) {
    val itemCount: Int
        get() = CartMath.itemCount(cart)

    val subtotalCents: Int
        get() = CartMath.subtotalCents(cart)
}

class JellyViewModel(
    private val analytics: JellyAnalytics,
    private val stockBackend: StockBackend,
) : ViewModel() {
    private val mutableState = MutableStateFlow(JellyUiState())
    val state: StateFlow<JellyUiState> = mutableState.asStateFlow()

    private var stockAttemptId = 0L

    fun addToCart(product: Product, quantity: Int = 1) {
        if (product.stockUnits <= 0) {
            requestOutOfStock(product)
            return
        }
        if (quantity <= 0) return
        mutableState.update { current ->
            val existing = current.cart.firstOrNull { it.product.sku == product.sku }
            val cart = if (existing == null) {
                current.cart + CartLine(product, quantity)
            } else {
                current.cart.map {
                    if (it.product.sku == product.sku) {
                        it.copy(quantity = it.quantity + quantity)
                    } else {
                        it
                    }
                }
            }
            current.copy(cart = cart)
        }
        analytics.trackAddToCart(product, quantity)
    }

    fun increment(sku: String) {
        val line = mutableState.value.cart.firstOrNull { it.product.sku == sku } ?: return
        addToCart(line.product)
    }

    fun decrement(sku: String) {
        val line = mutableState.value.cart.firstOrNull { it.product.sku == sku } ?: return
        if (line.quantity <= 1) {
            remove(sku)
        } else {
            mutableState.update { current ->
                current.copy(cart = current.cart.map {
                    if (it.product.sku == sku) it.copy(quantity = it.quantity - 1) else it
                })
            }
            analytics.trackRemoveFromCart(line.product, 1)
        }
    }

    fun remove(sku: String) {
        val line = mutableState.value.cart.firstOrNull { it.product.sku == sku } ?: return
        mutableState.update { current ->
            current.copy(cart = current.cart.filterNot { it.product.sku == sku })
        }
        analytics.trackRemoveFromCart(line.product, line.quantity)
    }

    fun markLoggedIn(userName: String) {
        val displayName = userName.trim().ifBlank { "Invitado Jelly" }
        val userId = userName.takeIf(String::isNotBlank)

        analytics.identifyUser(userId)
        mutableState.update {
            it.copy(
                loggedIn = true,
                displayName = displayName,
                activeUserId = userId,
            )
        }
        analytics.trackLogin()
    }

    fun logout() {
        if (!mutableState.value.loggedIn) return

        // Send logout while the current identity is still active.
        analytics.trackLogout()
        analytics.identifyUser(null)
        mutableState.update {
            it.copy(
                loggedIn = false,
                displayName = "Invitado Jelly",
                activeUserId = null,
            )
        }
    }

    fun completeCheckout(
        shipping: ShippingOption,
        payment: PaymentOption,
    ): JellyOrder? {
        val lines = mutableState.value.cart
        if (lines.isEmpty()) return null

        analytics.trackAddShippingInfo(lines, shipping.label)
        analytics.trackAddPaymentInfo(lines, payment.label)

        val order = JellyOrder(
            id = "JELLY-${System.currentTimeMillis()}",
            lines = lines,
            subtotalCents = CartMath.subtotalCents(lines),
            shippingCents = shipping.priceCents,
            shippingTier = shipping.label,
            paymentType = payment.label,
        )
        analytics.trackPurchase(order)
        mutableState.update { it.copy(cart = emptyList(), lastOrder = order) }
        return order
    }

    fun trackViewCart() {
        analytics.trackViewCart(mutableState.value.cart)
    }

    fun trackBeginCheckout() {
        analytics.trackBeginCheckout(mutableState.value.cart)
    }

    private fun requestOutOfStock(product: Product) {
        if (mutableState.value.pendingStockSku != null) return

        mutableState.update {
            it.copy(
                pendingStockSku = product.sku,
                stockError = null,
            )
        }

        viewModelScope.launch {
            val result = stockBackend.attemptAddToCart(product)
            val attemptId = ++stockAttemptId
            val error = when (result) {
                is StockCheckResult.Rejected -> StockError(
                    attemptId = attemptId,
                    sku = product.sku,
                    httpStatus = result.httpStatus,
                    errorCode = result.errorCode,
                    message = if (result.httpStatus == 409) {
                        "Error 409 · OUT_OF_STOCK. No quedan existencias de ${product.name}."
                    } else {
                        "Error HTTP ${result.httpStatus} · ${result.errorCode}. No se añadió ${product.name}."
                    },
                )

                is StockCheckResult.NetworkFailure -> StockError(
                    attemptId = attemptId,
                    sku = product.sku,
                    httpStatus = null,
                    errorCode = "NETWORK_ERROR",
                    message = "No se pudo contactar con el backend. No se añadió ${product.name}.",
                )
            }

            mutableState.update {
                it.copy(
                    pendingStockSku = null,
                    stockError = error,
                )
            }
        }
    }

    companion object {
        fun factory(
            analytics: JellyAnalytics,
            stockBackend: StockBackend,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return JellyViewModel(analytics, stockBackend) as T
                }
            }
    }
}
