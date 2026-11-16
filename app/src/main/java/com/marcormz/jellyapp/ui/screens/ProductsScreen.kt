package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.LocalCurrentScreen
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.components.PriceText
import com.marcormz.jellyapp.ui.components.ProductArtwork
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.components.StatusBanner
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyError
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellySurfaceRaised

@Composable
fun ProductsScreen(
    state: com.marcormz.jellyapp.ui.JellyUiState,
    onProductClick: (Product) -> Unit,
    onAddToCart: (Product) -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current

    LaunchedEffect(Unit) {
        analytics?.trackViewItemList(Catalog.products)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(title = "Catálogo")
        Spacer(Modifier.height(20.dp))
        SectionEyebrow(index = "01", text = "Colección ficticia")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Elige tu\nsiguiente prueba.",
                style = MaterialTheme.typography.headlineLarge,
                color = JellyCream,
            )
            Text(
                text = "${Catalog.products.size.toString().padStart(2, '0')}\nSKU",
                style = MaterialTheme.typography.labelMedium,
                color = JellyMuted,
            )
        }
        Spacer(Modifier.height(18.dp))
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("products_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Catalog.products, key = Product::sku) { product ->
                ProductCard(
                    product = product,
                    isPending = state.pendingStockSku == product.sku,
                    stockError = state.stockError?.takeIf { it.sku == product.sku },
                    onOpen = { onProductClick(product) },
                    onSelect = {
                        analytics?.trackSelectItem(product)
                    },
                    onAdd = { onAddToCart(product) },
                )
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    isPending: Boolean,
    stockError: com.marcormz.jellyapp.ui.StockError?,
    onOpen: () -> Unit,
    onSelect: () -> Unit,
    onAdd: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    val skuTag = product.sku.tagSuffix()
    JellyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .testTag("btn_product_open_$skuTag")
                    .clickable(role = Role.Button) {
                        analytics?.trackButton("btn_product_open_$skuTag", screen)
                        onSelect()
                        onOpen()
                    },
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductArtwork(
                    product = product,
                    modifier = Modifier.size(82.dp),
                    compact = true,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (product.stockUnits <= 0) {
                            "SIN STOCK · ${product.category.uppercase()}"
                        } else {
                            product.category.uppercase()
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (product.stockUnits <= 0) JellyError else JellyMuted,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = JellyCream,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(6.dp))
                    PriceText(product.priceCents)
                }
                Text(
                    text = "↗",
                    color = JellyMuted,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            if (isPending) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = JellyAmber,
                    trackColor = JellyBorder,
                )
            }
            stockError?.let { error ->
                StatusBanner(
                    message = error.message,
                    success = false,
                    modifier = Modifier.testTag("stock_error_$skuTag"),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = product.sku,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    color = JellyMuted,
                )
                JellySecondaryButton(
                    text = "Ver",
                    buttonId = "btn_product_detail_$skuTag",
                    onClick = {
                        onSelect()
                        onOpen()
                    },
                )
                JellyPrimaryButton(
                    text = when {
                        isPending -> "Comprobando…"
                        product.stockUnits <= 0 -> "Probar sin stock"
                        else -> "Añadir"
                    },
                    buttonId = "btn_product_add_$skuTag",
                    enabled = !isPending,
                    onClick = onAdd,
                )
            }
        }
    }
}

private fun String.tagSuffix(): String = replace("-", "_")
