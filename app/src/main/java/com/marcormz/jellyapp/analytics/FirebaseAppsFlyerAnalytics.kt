package com.marcormz.jellyapp.analytics

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.appsflyer.AppsFlyerProperties
import com.google.firebase.analytics.FirebaseAnalytics
import com.marcormz.jellyapp.BuildConfig
import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.CartMath
import com.marcormz.jellyapp.data.JellyOrder
import com.marcormz.jellyapp.data.Product

// Event reference: https://firebase.google.com/docs/analytics/events
// Ecommerce reference: https://firebase.google.com/docs/analytics/measure-ecommerce
class FirebaseAppsFlyerAnalytics(
    private val application: Application,
) : JellyAnalytics {
    private val firebase: FirebaseAnalytics? = if (BuildConfig.FIREBASE_CONFIGURED) {
        runCatching { FirebaseAnalytics.getInstance(application) }
            .onFailure { Log.w(TAG, "Firebase Analytics no está disponible.", it) }
            .getOrNull()
    } else {
        Log.i(TAG, "Firebase omitido: añade app/google-services.json.")
        null
    }

    private val quantumMetric = QuantumMetricBridge(application)

    @Volatile
    private var appsFlyerStarted: Boolean = false

    fun initialize() {
        firebase?.runCatching {
            setAnalyticsCollectionEnabled(true)
            // No identity is active until the login form is submitted.
            setUserId(null)
            setUserProperty("test_app", "jellyapp")
        }?.onFailure { Log.w(TAG, "No se pudo configurar la identidad de Firebase.", it) }

        initializeAppsFlyer()
        quantumMetric.initialize()
    }

    private fun initializeAppsFlyer() {
        if (BuildConfig.APPSFLYER_DEV_KEY.isBlank()) {
            Log.i(TAG, "AppsFlyer omitido: define APPSFLYER_DEV_KEY.")
            return
        }

        runCatching {
            AppsFlyerLib.getInstance().apply {
                setDebugLog(BuildConfig.DEBUG)
                init(BuildConfig.APPSFLYER_DEV_KEY, null, application)
                // SDK 6 may retain setter values. Start every launch without a CUID.
                AppsFlyerProperties.getInstance().remove(AppsFlyerProperties.APP_USER_ID)
                start(application)
            }
            appsFlyerStarted = true
            Log.i(TAG, "AppsFlyer inicializado sin Customer User ID.")
        }.onFailure {
            appsFlyerStarted = false
            Log.w(TAG, "No se pudo inicializar AppsFlyer.", it)
        }
    }

    override fun identifyUser(userId: String?) {
        firebase?.runCatching {
            setUserId(userId)
        }?.onFailure {
            Log.w(TAG, "Firebase no pudo actualizar el User ID.", it)
        }

        if (appsFlyerStarted) {
            runCatching {
                if (userId == null) {
                    AppsFlyerProperties.getInstance().remove(AppsFlyerProperties.APP_USER_ID)
                } else {
                    AppsFlyerLib.getInstance().setCustomerUserId(userId)
                }
            }.onFailure {
                Log.w(TAG, "AppsFlyer no pudo actualizar el Customer User ID.", it)
            }
        }
        Log.i(
            TAG,
            if (userId == null) {
                "Identidad analítica eliminada."
            } else {
                "Identidad analítica activada."
            },
        )
    }

    override fun trackScreen(screenName: String, screenClass: String) {
        // Log a screen view event
        // https://firebase.google.com/docs/analytics/screenviews#kotlin
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        logFirebase(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    override fun trackButton(buttonId: String, screenName: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, "button")
            putString(FirebaseAnalytics.Param.ITEM_ID, buttonId)
            putString("screen_name", screenName)
        }
        logFirebase(FirebaseAnalytics.Event.SELECT_CONTENT, params)
    }

    override fun trackLogin() {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "demo")
        }
        logFirebase(FirebaseAnalytics.Event.LOGIN, params)
        logAppsFlyer("af_login", emptyMap())
    }

    override fun trackLogout() {
        logFirebase("logout", Bundle.EMPTY)
        logAppsFlyer("af_logout", emptyMap())
    }

    override fun setUserProperty(name: String, value: String) {
        // https://firebase.google.com/docs/analytics/user-properties
        firebase?.setUserProperty(name, value)
        Log.d(TAG, "user_property $name=$value")
    }

    override fun trackCustomEvent(name: String, parameters: Map<String, String>) {
        // https://firebase.google.com/docs/analytics/events#log_events
        val params = Bundle().apply {
            parameters.forEach { (key, value) -> putString(key, value) }
        }
        logFirebase(name, params)
        logAppsFlyer(name, parameters)
    }

    override fun trackViewItemList(products: List<Product>) {
        val items = products.map { it.toItemBundle() }
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_LIST_ID, "catalog")
            putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, "Catálogo JellyApp")
            putParcelableArray(FirebaseAnalytics.Param.ITEMS, items.toItemArray())
        }
        logFirebase(FirebaseAnalytics.Event.VIEW_ITEM_LIST, params)

        val values = HashMap<String, Any>().apply {
            put("af_content_type", "product")
            put("af_content_list", products.map(Product::name).toTypedArray())
        }
        logAppsFlyer("af_list_view", values)
    }

    override fun trackSelectItem(product: Product) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_LIST_ID, "catalog")
            putString(FirebaseAnalytics.Param.ITEM_LIST_NAME, "Catálogo JellyApp")
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS,
                arrayOf(product.toItemBundle()).toItemArray(),
            )
        }
        logFirebase(FirebaseAnalytics.Event.SELECT_ITEM, params)
    }

    override fun trackViewItem(product: Product) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CURRENCY, CURRENCY)
            putDouble(FirebaseAnalytics.Param.VALUE, product.priceCents.toMoney())
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS,
                arrayOf(product.toItemBundle()).toItemArray(),
            )
        }
        logFirebase(FirebaseAnalytics.Event.VIEW_ITEM, params)

        logAppsFlyer(
            "af_content_view",
            hashMapOf(
                "af_content_id" to product.sku,
                "af_content_type" to product.category,
                "af_price" to product.priceCents.toMoney(),
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackAddToCart(product: Product, quantity: Int) {
        val item = product.toItemBundle(quantity)
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CURRENCY, CURRENCY)
            putDouble(FirebaseAnalytics.Param.VALUE, product.priceCents.toMoney() * quantity)
            putParcelableArray(FirebaseAnalytics.Param.ITEMS, arrayOf(item).toItemArray())
        }
        logFirebase(FirebaseAnalytics.Event.ADD_TO_CART, params)

        logAppsFlyer(
            "af_add_to_cart",
            hashMapOf(
                "af_content_id" to product.sku,
                "af_content_type" to product.category,
                "af_price" to product.priceCents.toMoney(),
                "af_quantity" to quantity,
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackRemoveFromCart(product: Product, quantity: Int) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CURRENCY, CURRENCY)
            putDouble(FirebaseAnalytics.Param.VALUE, product.priceCents.toMoney() * quantity)
            putParcelableArray(
                FirebaseAnalytics.Param.ITEMS,
                arrayOf(product.toItemBundle(quantity)).toItemArray(),
            )
        }
        logFirebase(FirebaseAnalytics.Event.REMOVE_FROM_CART, params)

        logAppsFlyer(
            "af_remove_from_cart",
            hashMapOf(
                "af_content_id" to product.sku,
                "af_content_type" to product.category,
                "af_price" to product.priceCents.toMoney(),
                "af_quantity" to quantity,
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackViewCart(lines: List<CartLine>) {
        logFirebase(FirebaseAnalytics.Event.VIEW_CART, cartBundle(lines))
    }

    override fun trackBeginCheckout(lines: List<CartLine>) {
        logFirebase(FirebaseAnalytics.Event.BEGIN_CHECKOUT, cartBundle(lines))

        logAppsFlyer(
            "af_initiated_checkout",
            hashMapOf(
                "af_content_id" to lines.firstOrNull()?.product?.sku.orEmpty(),
                "af_content_type" to "product",
                "af_revenue" to CartMath.subtotalCents(lines).toMoney(),
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackAddShippingInfo(lines: List<CartLine>, shippingTier: String) {
        val params = cartBundle(lines).apply {
            putString("shipping_tier", shippingTier)
        }
        logFirebase(FirebaseAnalytics.Event.ADD_SHIPPING_INFO, params)
        logAppsFlyer(
            "af_add_shipping_info",
            hashMapOf(
                "af_content_type" to "product",
                "af_shipping_tier" to shippingTier,
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackAddPaymentInfo(lines: List<CartLine>, paymentType: String) {
        val params = cartBundle(lines).apply {
            putString("payment_type", paymentType)
        }
        logFirebase(FirebaseAnalytics.Event.ADD_PAYMENT_INFO, params)
        logAppsFlyer(
            "af_add_payment_info",
            hashMapOf(
                "af_content_type" to "product",
                "af_payment_type" to paymentType,
                "af_currency" to CURRENCY,
            ),
        )
    }

    override fun trackPurchase(order: JellyOrder) {
        val items = order.lines.map { it.toItemBundle() }
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.TRANSACTION_ID, order.id)
            putString(FirebaseAnalytics.Param.AFFILIATION, "JellyApp Lab")
            putString(FirebaseAnalytics.Param.CURRENCY, CURRENCY)
            putDouble(FirebaseAnalytics.Param.VALUE, order.totalCents.toMoney())
            putDouble(FirebaseAnalytics.Param.SHIPPING, order.shippingCents.toMoney())
            putParcelableArray(FirebaseAnalytics.Param.ITEMS, items.toItemArray())
        }
        logFirebase(FirebaseAnalytics.Event.PURCHASE, params)

        val values = HashMap<String, Any>().apply {
            put("af_content_id", order.lines.firstOrNull()?.product?.sku.orEmpty())
            put("af_content_type", "product")
            put("af_content_list", order.lines.map { it.product.name }.toTypedArray())
            put("af_quantity", order.itemCount)
            put("af_revenue", order.totalCents.toMoney())
            put("af_currency", CURRENCY)
            put("af_order_id", order.id)
        }
        logAppsFlyer("af_purchase", values)
    }

    private fun cartBundle(lines: List<CartLine>): Bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.CURRENCY, CURRENCY)
        putDouble(
            FirebaseAnalytics.Param.VALUE,
            CartMath.subtotalCents(lines).toMoney(),
        )
        putParcelableArray(
            FirebaseAnalytics.Param.ITEMS,
            lines.map { it.toItemBundle() }.toItemArray(),
        )
    }

    private fun logFirebase(name: String, params: Bundle) {
        Log.d(TAG, "GA4 $name ${params.toDebugString()}")
        firebase?.logEvent(name, params)
    }

    private fun logAppsFlyer(name: String, params: Map<String, Any>) {
        if (!appsFlyerStarted) return
        Log.d(TAG, "AppsFlyer $name $params")
        runCatching {
            AppsFlyerLib.getInstance().logEvent(application, name, HashMap(params))
        }.onFailure { Log.w(TAG, "AppsFlyer rechazó el evento $name.", it) }
    }

    private fun Product.toItemBundle(quantity: Int = 1): Bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.ITEM_ID, sku)
        putString(FirebaseAnalytics.Param.ITEM_NAME, name)
        putString(FirebaseAnalytics.Param.ITEM_CATEGORY, category)
        putString(FirebaseAnalytics.Param.ITEM_BRAND, "JellyApp Lab")
        putDouble(FirebaseAnalytics.Param.PRICE, priceCents.toMoney())
        putLong(FirebaseAnalytics.Param.QUANTITY, quantity.toLong())
    }

    private fun CartLine.toItemBundle(): Bundle = product.toItemBundle(quantity)

    private fun List<Bundle>.toItemArray(): Array<Parcelable> = Array(size) { this[it] }

    private fun Array<Bundle>.toItemArray(): Array<Parcelable> = Array(size) { this[it] }

    private fun Int.toMoney(): Double = this / 100.0

    private fun Bundle.toDebugString(): String =
        keySet().joinToString(prefix = "{", postfix = "}") { key ->
            val value = when (val raw = get(key)) {
                is Array<*> -> raw.contentToString()
                else -> raw.toString()
            }
            "$key=$value"
        }

    companion object {
        private const val TAG = "JellyAnalytics"
        private const val CURRENCY = "MXN"
    }
}
