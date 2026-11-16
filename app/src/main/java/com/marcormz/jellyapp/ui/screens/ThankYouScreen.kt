package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marcormz.jellyapp.data.Money
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellyPrimaryButton
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBlack
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellySurface

@Composable
fun ThankYouScreen(
    state: JellyUiState,
    onHome: () -> Unit,
    onProducts: () -> Unit,
) {
    val order = state.lastOrder
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(title = "Thank you")

        Spacer(Modifier.height(38.dp))
        Box(
            modifier = Modifier
                .size(92.dp)
                .background(JellyAmber, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "✓",
                color = JellyBlack,
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(28.dp))
        SectionEyebrow(index = "04", text = "Compra completada")
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Pedido\nregistrado.",
            style = MaterialTheme.typography.displayMedium,
            color = JellyCream,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "La conversión simulada ya forma parte del recorrido de analítica.",
            style = MaterialTheme.typography.bodyLarge,
            color = JellyMuted,
        )

        Spacer(Modifier.height(26.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "RECIBO DE PRUEBA",
                    style = MaterialTheme.typography.labelSmall,
                    color = JellyMuted,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Pedido", style = MaterialTheme.typography.bodyMedium, color = JellyMuted)
                    Text(
                        text = order?.id ?: "Sin pedido en memoria",
                        style = MaterialTheme.typography.bodyMedium,
                        color = JellyCream,
                    )
                }
                if (order != null) {
                    HorizontalDivider(color = JellyBorder)
                    order.lines.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = "${line.quantity}× ${line.product.name}",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall,
                                color = JellyCream,
                            )
                            Text(
                                text = Money.format(line.subtotalCents),
                                style = MaterialTheme.typography.bodySmall,
                                color = JellyCream,
                            )
                        }
                    }
                    SummaryLine("Envío · ${order.shippingTier}", Money.format(order.shippingCents))
                    SummaryLine("Pago", order.paymentType)
                    HorizontalDivider(color = JellyBorder)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.titleMedium,
                            color = JellyCream,
                        )
                        Text(
                            Money.format(order.totalCents),
                            style = MaterialTheme.typography.titleLarge,
                            color = JellyAmber,
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(18.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JellySurface)
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(JellyAmber, CircleShape),
            )
            Text(
                text = "Evento purchase enviado con transaction_id, items, moneda e ingresos.",
                style = MaterialTheme.typography.bodySmall,
                color = JellyMuted,
            )
        }

        Spacer(Modifier.height(26.dp))
        JellyPrimaryButton(
            text = "Volver al inicio",
            buttonId = "btn_thankyou_home",
            modifier = Modifier.fillMaxWidth(),
            onClick = onHome,
        )
        Spacer(Modifier.height(10.dp))
        JellySecondaryButton(
            text = "Repetir la compra",
            buttonId = "btn_thankyou_products",
            modifier = Modifier.fillMaxWidth(),
            onClick = onProducts,
        )
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = JellyMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = JellyCream)
    }
}
