package com.marcormz.jellyapp

import com.marcormz.jellyapp.analytics.JellyAnalytics
import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.data.JellyOrder
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.data.StockBackend
import com.marcormz.jellyapp.data.StockCheckResult
import com.marcormz.jellyapp.data.paymentOptions
import com.marcormz.jellyapp.data.shippingOptions
import com.marcormz.jellyapp.ui.JellyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class JellyViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analytics = RecordingAnalytics()
    private val stockBackend = FakeStockBackend()
    private val viewModel = JellyViewModel(analytics, stockBackend)

    @Test
    fun addingSameProductMergesLinesAndTracksEachAddition() {
        val product = Catalog.products.first()

        viewModel.addToCart(product)
        viewModel.addToCart(product, 2)

        assertEquals(1, viewModel.state.value.cart.size)
        assertEquals(3, viewModel.state.value.itemCount)
        assertEquals(23_997, viewModel.state.value.subtotalCents)
        assertEquals(listOf("add_to_cart", "add_to_cart"), analytics.events)
    }

    @Test
    fun decrementRemovesLineAtZeroWithoutGoingNegative() {
        val product = Catalog.products[1]
        viewModel.addToCart(product)
        analytics.events.clear()

        viewModel.decrement(product.sku)

        assertTrue(viewModel.state.value.cart.isEmpty())
        assertEquals(listOf("remove_from_cart"), analytics.events)
    }

    @Test
    fun checkoutTracksFullConversionAndClearsCart() {
        val product = Catalog.products.first()
        viewModel.addToCart(product, 2)
        analytics.events.clear()

        val order = viewModel.completeCheckout(
            shippingOptions.first(),
            paymentOptions.first(),
        )

        assertNotNull(order)
        assertEquals(0, viewModel.state.value.itemCount)
        assertEquals("Estándar", order?.shippingTier)
        assertEquals("Tarjeta demo", order?.paymentType)
        assertEquals(16_493, order?.totalCents)
        assertEquals(
            listOf("add_shipping_info", "add_payment_info", "purchase"),
            analytics.events,
        )

        analytics.events.clear()
        assertNull(
            viewModel.completeCheckout(shippingOptions.first(), paymentOptions.first()),
        )
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun startsWithoutAnActiveIdentity() {
        assertNull(viewModel.state.value.activeUserId)
        assertFalse(viewModel.state.value.loggedIn)
        assertTrue(analytics.events.isEmpty())
    }

    @Test
    fun loginUsesExactUserNameAsIdentity() {
        viewModel.markLoggedIn("persona@example.com")

        assertTrue(viewModel.state.value.loggedIn)
        assertEquals("persona@example.com", viewModel.state.value.activeUserId)
        assertEquals(
            listOf("identify_user:persona@example.com", "login"),
            analytics.events,
        )
    }

    @Test
    fun blankUserIsAcceptedWithoutAnActiveUserId() {
        viewModel.markLoggedIn("")

        assertTrue(viewModel.state.value.loggedIn)
        assertNull(viewModel.state.value.activeUserId)
        assertEquals(
            listOf("identify_user:null", "login"),
            analytics.events,
        )
    }

    @Test
    fun outOfStockAttemptsHitBackendEveryTimeAndNeverChangeCart() {
        val product = requireNotNull(Catalog.find("JELLY-006"))

        viewModel.addToCart(product)
        viewModel.addToCart(product)

        assertEquals(2, stockBackend.attempts)
        assertEquals(0, viewModel.state.value.itemCount)
        assertEquals("JELLY-006", viewModel.state.value.stockError?.sku)
        assertEquals(409, viewModel.state.value.stockError?.httpStatus)
        assertEquals("OUT_OF_STOCK", viewModel.state.value.stockError?.errorCode)
        assertNull(viewModel.state.value.pendingStockSku)
    }

    @Test
    fun logoutIsTrackedBeforeIdentityIsCleared() {
        val product = Catalog.products.first()
        viewModel.addToCart(product)
        viewModel.markLoggedIn("jelly_user")
        analytics.events.clear()

        viewModel.logout()

        assertFalse(viewModel.state.value.loggedIn)
        assertNull(viewModel.state.value.activeUserId)
        assertEquals(1, viewModel.state.value.itemCount)
        assertEquals(listOf("logout", "identify_user:null"), analytics.events)
    }
}

private class RecordingAnalytics : JellyAnalytics {
    val events = mutableListOf<String>()

    override fun identifyUser(userId: String?) {
        events += "identify_user:${userId ?: "null"}"
    }

    override fun trackScreen(screenName: String, screenClass: String) {
        events += "screen_view"
    }

    override fun trackButton(buttonId: String, screenName: String) {
        events += "select_content"
    }

    override fun trackLogin() {
        events += "login"
    }

    override fun trackLogout() {
        events += "logout"
    }

    override fun setUserProperty(name: String, value: String) {
        events += "user_property"
    }

    override fun trackCustomEvent(name: String, parameters: Map<String, String>) {
        events += name
    }

    override fun trackViewItemList(products: List<Product>) {
        events += "view_item_list"
    }

    override fun trackSelectItem(product: Product) {
        events += "select_item"
    }

    override fun trackViewItem(product: Product) {
        events += "view_item"
    }

    override fun trackAddToCart(product: Product, quantity: Int) {
        events += "add_to_cart"
    }

    override fun trackRemoveFromCart(product: Product, quantity: Int) {
        events += "remove_from_cart"
    }

    override fun trackViewCart(lines: List<CartLine>) {
        events += "view_cart"
    }

    override fun trackBeginCheckout(lines: List<CartLine>) {
        events += "begin_checkout"
    }

    override fun trackAddShippingInfo(lines: List<CartLine>, shippingTier: String) {
        events += "add_shipping_info"
    }

    override fun trackAddPaymentInfo(lines: List<CartLine>, paymentType: String) {
        events += "add_payment_info"
    }

    override fun trackPurchase(order: JellyOrder) {
        events += "purchase"
    }
}

private class FakeStockBackend : StockBackend {
    var attempts = 0
    var result: StockCheckResult = StockCheckResult.Rejected(
        httpStatus = 409,
        errorCode = "OUT_OF_STOCK",
    )

    override suspend fun attemptAddToCart(product: Product): StockCheckResult {
        attempts += 1
        return result
    }
}
