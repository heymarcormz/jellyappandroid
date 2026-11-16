package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.marcormz.jellyapp.data.CartMath
import com.marcormz.jellyapp.data.Money
import com.marcormz.jellyapp.data.PaymentOption
import com.marcormz.jellyapp.data.ShippingOption
import com.marcormz.jellyapp.data.paymentOptions
import com.marcormz.jellyapp.data.shippingOptions
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.LocalCurrentScreen
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellySurfaceRaised

@Composable
fun CheckoutScreen(
    state: JellyUiState,
    onTrackBeginCheckout: () -> Unit,
    onBack: () -> Unit,
    onProducts: () -> Unit,
    onPlaceOrder: (ShippingOption, PaymentOption) -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    var shipping by rememberSaveable { mutableStateOf(shippingOptions.first().id) }
    var payment by rememberSaveable { mutableStateOf(paymentOptions.first().id) }
    val selectedShipping = shippingOptions.first { it.id == shipping }
    val selectedPayment = paymentOptions.first { it.id == payment }

    LaunchedEffect(Unit) { onTrackBeginCheckout() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(
            title = "Checkout",
            backButtonId = "btn_checkout_back",
            onBack = onBack,
        )
        Spacer(Modifier.height(20.dp))

        if (state.cart.isEmpty()) {
            SectionEyebrow(index = "01", text = "Sin pedido")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "No hay nada\nque confirmar.",
                style = MaterialTheme.typography.headlineLarge,
                color = JellyCream,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Vuelve al catálogo y añade un producto antes de iniciar el checkout.",
                style = MaterialTheme.typography.bodyMedium,
                color = JellyMuted,
            )
            Spacer(Modifier.height(22.dp))
            JellyPrimaryButton(
                text = "Explorar productos",
                buttonId = "btn_checkout_empty_products",
                onClick = onProducts,
            )
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            SectionEyebrow(index = "02", text = "Confirmación simulada")
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Casi es\ntuya.",
                style = MaterialTheme.typography.headlineLarge,
                color = JellyCream,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No se procesará ningún pago ni se enviarán datos personales.",
                style = MaterialTheme.typography.bodyMedium,
                color = JellyMuted,
            )

            Spacer(Modifier.height(22.dp))
            JellyCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "RESUMEN · ${state.itemCount} ARTÍCULOS",
                        style = MaterialTheme.typography.labelSmall,
                        color = JellyMuted,
                    )
                    state.cart.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${line.quantity}× ${line.product.name}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodyMedium,
                                color = JellyCream,
                            )
                            Text(
                                text = Money.format(line.subtotalCents),
                                style = MaterialTheme.typography.bodyMedium,
                                color = JellyCream,
                            )
                        }
                    }
                    HorizontalDivider(color = JellyBorder)
                    SummaryRow("Subtotal", CartMath.subtotalCents(state.cart))
                    SummaryRow("Envío ${selectedShipping.label}", selectedShipping.priceCents)
                    HorizontalDivider(color = JellyBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = JellyCream,
                        )
                        Text(
                            text = Money.format(state.subtotalCents + selectedShipping.priceCents),
                            style = MaterialTheme.typography.titleLarge,
                            color = JellyAmber,
                        )
                    }
                }
            }

            Spacer(Modifier.height(26.dp))
            Text(
                text = "01 / Envío",
                style = MaterialTheme.typography.titleMedium,
                color = JellyCream,
            )
            Spacer(Modifier.height(10.dp))
            shippingOptions.forEach { option ->
                OptionRow(
                    title = "${option.label}  ·  ${Money.format(option.priceCents)}",
                    detail = option.detail,
                    selected = option.id == shipping,
                    testTag = "btn_checkout_shipping_${option.id}",
                    onSelect = {
                        analytics?.trackButton("btn_checkout_shipping_${option.id}", screen)
                        shipping = option.id
                    },
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "02 / Pago",
                style = MaterialTheme.typography.titleMedium,
                color = JellyCream,
            )
            Spacer(Modifier.height(10.dp))
            paymentOptions.forEach { option ->
                OptionRow(
                    title = option.label,
                    detail = option.detail,
                    selected = option.id == payment,
                    testTag = "btn_checkout_payment_${option.id}",
                    onSelect = {
                        analytics?.trackButton("btn_checkout_payment_${option.id}", screen)
                        payment = option.id
                    },
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(12.dp))
        JellyPrimaryButton(
            text = "Confirmar pedido  ·  ${Money.format(state.subtotalCents + selectedShipping.priceCents)}",
            buttonId = "btn_checkout_purchase",
            modifier = Modifier.fillMaxWidth(),
            onClick = { onPlaceOrder(selectedShipping, selectedPayment) },
        )
        Spacer(Modifier.height(10.dp))
        JellySecondaryButton(
            text = "Volver al carrito",
            buttonId = "btn_checkout_cart",
            modifier = Modifier.fillMaxWidth(),
            onClick = onBack,
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SummaryRow(label: String, cents: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = JellyMuted)
        Text(Money.format(cents), style = MaterialTheme.typography.bodySmall, color = JellyCream)
    }
}

@Composable
private fun OptionRow(
    title: String,
    detail: String,
    selected: Boolean,
    testTag: String,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) JellyAmber.copy(alpha = 0.1f) else JellySurfaceRaised)
            .selectable(
                selected = selected,
                role = Role.RadioButton,
                onClick = onSelect,
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(50))
                .background(if (selected) JellyAmber else JellyBorder),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (selected) {
                Spacer(
                    Modifier
                        .size(6.dp)
                        .background(JellySurfaceRaised, RoundedCornerShape(50)),
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = JellyCream)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = JellyMuted)
        }
        Text(
            text = if (selected) "●" else "○",
            color = if (selected) JellyAmber else JellyMuted,
        )
    }
}
