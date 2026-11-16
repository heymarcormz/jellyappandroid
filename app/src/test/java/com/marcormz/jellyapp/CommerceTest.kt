package com.marcormz.jellyapp

import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.CartMath
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.data.Money
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CommerceTest {
    @Test
    fun catalogHasSixUniqueProductsAndOneWithoutStock() {
        assertEquals(6, Catalog.products.size)
        assertEquals(6, Catalog.products.map { it.sku }.distinct().size)
        assertTrue(Catalog.products.all { it.priceCents > 0 })
        assertEquals(0, Catalog.find("JELLY-006")?.stockUnits)
        assertTrue(Catalog.products.filterNot { it.sku == "JELLY-006" }.all { it.stockUnits > 0 })
    }

    @Test
    fun cartMathCalculatesItemsSubtotalAndTotal() {
        val lines = listOf(
            CartLine(Catalog.products[0], quantity = 2),
            CartLine(Catalog.products[2], quantity = 1),
        )

        assertEquals(3, CartMath.itemCount(lines))
        assertEquals(21_988, CartMath.subtotalCents(lines))
        assertEquals(22_483, CartMath.totalCents(lines, shippingCents = 495))
    }

    @Test
    fun moneyFormatterUsesMexicanPesos() {
        val formatted = Money.format(7_999)

        assertEquals("\$79.99 MXN", formatted)
    }
}
