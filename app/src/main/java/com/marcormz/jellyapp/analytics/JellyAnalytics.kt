package com.marcormz.jellyapp.analytics

import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.JellyOrder
import com.marcormz.jellyapp.data.Product

interface JellyAnalytics {
    fun identifyUser(userId: String?)
    fun trackScreen(screenName: String, screenClass: String = "MainActivity")
    fun trackButton(buttonId: String, screenName: String)
    fun trackLogin()
    fun trackLogout()
    fun setUserProperty(name: String, value: String)
    fun trackCustomEvent(name: String, parameters: Map<String, String>)
    fun trackViewItemList(products: List<Product>)
    fun trackSelectItem(product: Product)
    fun trackViewItem(product: Product)
    fun trackAddToCart(product: Product, quantity: Int)
    fun trackRemoveFromCart(product: Product, quantity: Int)
    fun trackViewCart(lines: List<CartLine>)
    fun trackBeginCheckout(lines: List<CartLine>)
    fun trackAddShippingInfo(lines: List<CartLine>, shippingTier: String)
    fun trackAddPaymentInfo(lines: List<CartLine>, paymentType: String)
    fun trackPurchase(order: JellyOrder)
}

object AnalyticsInput {
    private val namePattern = Regex("^[A-Za-z][A-Za-z0-9_]*$")

    fun validateUserPropertyName(value: String): String? = when {
        value.isBlank() -> "Escribe un nombre de propiedad."
        value.length > 24 -> "Máximo 24 caracteres."
        !namePattern.matches(value) -> "Usa letras, números o guion bajo; debe empezar por letra."
        else -> null
    }

    fun validateUserPropertyValue(value: String): String? = when {
        value.isBlank() -> "Escribe un valor."
        value.length > 36 -> "Máximo 36 caracteres."
        else -> null
    }

    fun validateEventName(value: String): String? = when {
        value.isBlank() -> "Escribe un nombre de evento."
        value.length > 40 -> "Máximo 40 caracteres."
        !namePattern.matches(value) -> "Usa letras, números o guion bajo; debe empezar por letra."
        else -> null
    }

    fun validateParameterName(value: String): String? = when {
        value.isBlank() -> "Escribe el nombre del parámetro."
        value.length > 40 -> "Máximo 40 caracteres."
        !namePattern.matches(value) -> "Usa letras, números o guion bajo; debe empezar por letra."
        else -> null
    }

    fun validateParameterValue(value: String): String? = when {
        value.isBlank() -> "Escribe el valor del parámetro."
        value.length > 100 -> "Máximo 100 caracteres."
        else -> null
    }
}
