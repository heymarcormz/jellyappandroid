package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.components.PriceText
import com.marcormz.jellyapp.ui.components.ProductArtwork
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.components.StatusBanner
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyError
import com.marcormz.jellyapp.ui.theme.JellyMuted

@Composable
fun ProductDetailScreen(
    productId: String,
    state: JellyUiState,
    onBack: () -> Unit,
    onAddToCart: (Product) -> Unit,
    onViewCart: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val product = Catalog.find(productId)
    var added by remember { mutableStateOf(false) }
    val stockError = state.stockError?.takeIf { it.sku == productId }
    val isPending = state.pendingStockSku == productId
    val outOfStock = product?.stockUnits?.let { it <= 0 } == true

    LaunchedEffect(productId) {
        product?.let { analytics?.trackViewItem(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(
            title = "Producto",
            backButtonId = "btn_detail_back",
            onBack = onBack,
        )

        if (product == null) {
            Spacer(Modifier.height(40.dp))
            Text(
                text = "Producto no encontrado.",
                style = MaterialTheme.typography.headlineMedium,
                color = JellyCream,
            )
            Spacer(Modifier.height(12.dp))
            JellySecondaryButton(
                text = "Volver al catálogo",
                buttonId = "btn_detail_not_found_products",
                onClick = onBack,
            )
            return@Column
        }

        Spacer(Modifier.height(18.dp))
        ProductArtwork(
            product = product,
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
        )
        Spacer(Modifier.height(24.dp))
        SectionEyebrow(
            index = "02",
            text = if (outOfStock) "Sin stock · ${product.category}" else product.category,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = product.name,
            style = MaterialTheme.typography.headlineLarge,
            color = JellyCream,
        )
        if (outOfStock) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "0 unidades disponibles. El botón consulta al backend para probar el error automático en Quantum Metric.",
                style = MaterialTheme.typography.bodySmall,
                color = JellyError,
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = product.tagline,
            style = MaterialTheme.typography.bodyLarge,
            color = JellyMuted,
        )
        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PriceText(cents = product.priceCents, large = true)
            Text(
                text = product.sku,
                style = MaterialTheme.typography.labelMedium,
                color = JellyMuted,
            )
        }

        Spacer(Modifier.height(20.dp))
        HorizontalDivider(color = JellyBorder)
        Spacer(Modifier.height(20.dp))
        Text(
            text = product.description,
            style = MaterialTheme.typography.bodyLarge,
            color = JellyCream,
        )
        Spacer(Modifier.height(22.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "TRAZABILIDAD DE LA PRUEBA",
                    style = MaterialTheme.typography.labelSmall,
                    color = JellyMuted,
                )
                Text(
                    text = "view_item + select_item",
                    style = MaterialTheme.typography.titleMedium,
                    color = JellyCream,
                )
                Text(
                    text = "Cada apertura del detalle se envía una sola vez a Firebase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = JellyMuted,
                )
            }
        }

        Spacer(Modifier.height(22.dp))
        if (added && !outOfStock) {
            StatusBanner(
                message = "${product.name} añadido al carrito (${state.itemCount} en total).",
                success = true,
            )
            Spacer(Modifier.height(12.dp))
        }
        if (isPending) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = JellyAmber,
                trackColor = JellyBorder,
            )
            Spacer(Modifier.height(12.dp))
        }
        stockError?.let { error ->
            StatusBanner(
                message = error.message,
                success = false,
                modifier = Modifier.testTag("stock_error_${product.sku.replace("-", "_")}"),
            )
            Spacer(Modifier.height(12.dp))
        }
        JellyPrimaryButton(
            text = when {
                isPending -> "Comprobando stock…"
                outOfStock -> "Probar sin stock"
                else -> "Añadir al carrito  ·  ${com.marcormz.jellyapp.data.Money.format(product.priceCents)}"
            },
            buttonId = "btn_detail_add_${product.sku.replace("-", "_")}",
            modifier = Modifier.fillMaxWidth(),
            enabled = !isPending,
            onClick = {
                onAddToCart(product)
                if (!outOfStock) added = true
            },
        )
        Spacer(Modifier.height(10.dp))
        JellySecondaryButton(
            text = "Ver carrito (${state.itemCount})",
            buttonId = "btn_detail_view_cart",
            modifier = Modifier.fillMaxWidth(),
            onClick = onViewCart,
        )
        Spacer(Modifier.height(30.dp))
    }
}
