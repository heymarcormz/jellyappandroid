package com.marcormz.jellyapp.ui.screens

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marcormz.jellyapp.data.CartLine
import com.marcormz.jellyapp.data.Money
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellyQuantityButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.PriceText
import com.marcormz.jellyapp.ui.components.ProductArtwork
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted

@Composable
fun CartScreen(
    state: JellyUiState,
    onTrackViewCart: () -> Unit,
    onIncrement: (String) -> Unit,
    onDecrement: (String) -> Unit,
    onRemove: (String) -> Unit,
    onCheckout: () -> Unit,
    onProducts: () -> Unit,
) {
    LaunchedEffect(Unit) { onTrackViewCart() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(title = "Carrito")
        Spacer(Modifier.height(20.dp))
        SectionEyebrow(index = "01", text = "Tu selección")
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = if (state.cart.isEmpty()) "Carrito\nvacío." else "Todo lo que\nquieres probar.",
                style = MaterialTheme.typography.headlineLarge,
                color = JellyCream,
            )
            Text(
                text = "${state.itemCount.toString().padStart(2, '0')}\nuds.",
                style = MaterialTheme.typography.labelMedium,
                color = JellyMuted,
            )
        }
        Spacer(Modifier.height(16.dp))

        if (state.cart.isEmpty()) {
            EmptyCart(onProducts = onProducts)
            return@Column
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.cart, key = { it.product.sku }) { line ->
                CartLineCard(
                    line = line,
                    onIncrement = { onIncrement(line.product.sku) },
                    onDecrement = { onDecrement(line.product.sku) },
                    onRemove = { onRemove(line.product.sku) },
                )
            }
            item {
                Column {
                    Spacer(Modifier.height(8.dp))
                    PriceSummary(state.subtotalCents)
                    Spacer(Modifier.height(18.dp))
                }
            }
        }

        JellyPrimaryButton(
            text = "Ir al checkout  →",
            buttonId = "btn_cart_checkout",
            modifier = Modifier.fillMaxWidth(),
            onClick = onCheckout,
        )
        Spacer(Modifier.height(10.dp))
        JellySecondaryButton(
            text = "Seguir comprando",
            buttonId = "btn_cart_continue_shopping",
            modifier = Modifier.fillMaxWidth(),
            onClick = onProducts,
        )
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
private fun CartLineCard(
    line: CartLine,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onRemove: () -> Unit,
) {
    val skuTag = line.product.sku.replace("-", "_")
    JellyCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProductArtwork(
                    product = line.product,
                    modifier = Modifier.size(68.dp),
                    compact = true,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = line.product.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = JellyMuted,
                    )
                    Text(
                        text = line.product.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = JellyCream,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(3.dp))
                    PriceText(line.subtotalCents)
                }
            }
            HorizontalDivider(color = JellyBorder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                JellyQuantityButton(
                    text = "−",
                    buttonId = "btn_cart_quantity_minus_$skuTag",
                    contentDescription = "Reducir cantidad de ${line.product.name}",
                    onClick = onDecrement,
                )
                Text(
                    text = line.quantity.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.titleMedium,
                    color = JellyCream,
                )
                JellyQuantityButton(
                    text = "+",
                    buttonId = "btn_cart_quantity_plus_$skuTag",
                    contentDescription = "Aumentar cantidad de ${line.product.name}",
                    onClick = onIncrement,
                )
                Spacer(Modifier.weight(1f))
                JellySecondaryButton(
                    text = "Quitar",
                    buttonId = "btn_cart_remove_$skuTag",
                    onClick = onRemove,
                )
            }
        }
    }
}

@Composable
private fun PriceSummary(subtotalCents: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Subtotal", style = MaterialTheme.typography.bodyMedium, color = JellyMuted)
            Text(Money.format(subtotalCents), style = MaterialTheme.typography.titleMedium, color = JellyCream)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Envío", style = MaterialTheme.typography.bodyMedium, color = JellyMuted)
            Text("Se elige en checkout", style = MaterialTheme.typography.bodySmall, color = JellyMuted)
        }
    }
}

@Composable
private fun EmptyCart(onProducts: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 44.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "0",
            color = JellyAmber,
            fontSize = 76.sp,
            style = MaterialTheme.typography.displayLarge,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Aún no hay nada que medir.",
            style = MaterialTheme.typography.titleLarge,
            color = JellyCream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Añade un producto para activar el flujo de ecommerce.",
            style = MaterialTheme.typography.bodyMedium,
            color = JellyMuted,
        )
        Spacer(Modifier.height(22.dp))
        JellyPrimaryButton(
            text = "Explorar productos",
            buttonId = "btn_cart_empty_products",
            onClick = onProducts,
        )
    }
}
