package com.marcormz.jellyapp.data

import java.text.NumberFormat
import java.util.Locale

data class Product(
    val sku: String,
    val name: String,
    val category: String,
    val tagline: String,
    val description: String,
    val priceCents: Int,
    val artworkCode: String,
    val stockUnits: Int = 20,
)

data class CartLine(
    val product: Product,
    val quantity: Int,
) {
    val subtotalCents: Int
        get() = product.priceCents * quantity
}

data class ShippingOption(
    val id: String,
    val label: String,
    val detail: String,
    val priceCents: Int,
)

data class PaymentOption(
    val id: String,
    val label: String,
    val detail: String,
)

data class JellyOrder(
    val id: String,
    val lines: List<CartLine>,
    val subtotalCents: Int,
    val shippingCents: Int,
    val shippingTier: String,
    val paymentType: String,
) {
    val totalCents: Int
        get() = subtotalCents + shippingCents

    val itemCount: Int
        get() = lines.sumOf(CartLine::quantity)
}

object Catalog {
    val products = listOf(
        Product(
            sku = "JELLY-001",
            name = "Auriculares Nébula",
            category = "Audio",
            tagline = "Silencio que se siente.",
            description = "Auriculares inalámbricos ficticios con cancelación de ruido y hasta 30 horas de batería.",
            priceCents = 7_999,
            artworkCode = "AN",
        ),
        Product(
            sku = "JELLY-002",
            name = "Smartwatch Órbita",
            category = "Wearables",
            tagline = "Tu ritmo, en órbita.",
            description = "Reloj inteligente de prueba con métricas de actividad, pantalla AMOLED y GPS.",
            priceCents = 12_999,
            artworkCode = "SO",
        ),
        Product(
            sku = "JELLY-003",
            name = "Mochila Atlas",
            category = "Accesorios",
            tagline = "Todo cabe. Nada pesa.",
            description = "Mochila urbana ficticia de 22 litros, tejido reciclado y compartimento para portátil.",
            priceCents = 5_990,
            artworkCode = "MA",
        ),
        Product(
            sku = "JELLY-004",
            name = "Lámpara Lumen",
            category = "Hogar",
            tagline = "Luz cálida, mente clara.",
            description = "Lámpara de sobremesa ficticia con luz regulable y carga USB-C.",
            priceCents = 3_995,
            artworkCode = "LL",
        ),
        Product(
            sku = "JELLY-005",
            name = "Zapatillas Vértice",
            category = "Calzado",
            tagline = "Hechas para avanzar.",
            description = "Zapatillas ficticias de edición limitada con suela ligera y tejido transpirable.",
            priceCents = 8_999,
            artworkCode = "ZV",
        ),
        Product(
            sku = "JELLY-006",
            name = "Teclado Quantum",
            category = "Edición limitada",
            tagline = "La señal que nunca llega.",
            description = "Producto ficticio sin existencias. Cada intento de compra consulta al backend y devuelve un error 409.",
            priceCents = 14_999,
            artworkCode = "TQ",
            stockUnits = 0,
        ),
    )

    fun find(sku: String): Product? = products.firstOrNull { it.sku == sku }
}

object CartMath {
    fun subtotalCents(lines: List<CartLine>): Int = lines.sumOf(CartLine::subtotalCents)

    fun itemCount(lines: List<CartLine>): Int = lines.sumOf(CartLine::quantity)

    fun totalCents(lines: List<CartLine>, shippingCents: Int): Int =
        subtotalCents(lines) + shippingCents
}

object Money {
    private val MEXICO = Locale.forLanguageTag("es-MX")
    private val MEXICAN_PESO = java.util.Currency.getInstance("MXN")

    fun format(cents: Int, locale: Locale = MEXICO): String {
        val formatter = NumberFormat.getCurrencyInstance(locale).apply {
            currency = MEXICAN_PESO
        }
        return "${formatter.format(cents / 100.0)} MXN"
    }
}

val shippingOptions = listOf(
    ShippingOption("standard", "Estándar", "3–5 días laborables", 495),
    ShippingOption("express", "Express", "24–48 horas", 995),
)

val paymentOptions = listOf(
    PaymentOption("card", "Tarjeta demo", "Simulación, no se realiza ningún cargo"),
    PaymentOption("cash", "Contra reembolso", "Pago ficticio al recibir"),
)
