package com.marcormz.jellyapp.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marcormz.jellyapp.data.Catalog
import com.marcormz.jellyapp.ui.JellyUiState
import com.marcormz.jellyapp.ui.components.JellyCard
import com.marcormz.jellyapp.ui.components.JellySecondaryButton
import com.marcormz.jellyapp.ui.components.JellyTopBar
import com.marcormz.jellyapp.ui.components.LocalJellyAnalytics
import com.marcormz.jellyapp.ui.components.LocalCurrentScreen
import com.marcormz.jellyapp.ui.components.MetricChip
import com.marcormz.jellyapp.ui.components.SectionEyebrow
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyBlack
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellySurface
import com.marcormz.jellyapp.ui.theme.Sora

@Composable
fun HomeScreen(
    state: JellyUiState,
    onProducts: () -> Unit,
    onWebView: () -> Unit,
    onAccount: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
    ) {
        JellyTopBar(title = "Inicio")

        Spacer(Modifier.height(22.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(JellyAmber, RoundedCornerShape(28.dp)),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    color = JellyBlack.copy(alpha = 0.08f),
                    radius = size.width * 0.44f,
                    center = Offset(size.width * 0.93f, size.height * 0.1f),
                )
                drawCircle(
                    color = JellyBlack.copy(alpha = 0.12f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.84f, size.height * 0.9f),
                )
                drawLine(
                    color = JellyBlack.copy(alpha = 0.2f),
                    start = Offset(size.width * 0.5f, 0f),
                    end = Offset(size.width, size.height * 0.55f),
                    strokeWidth = 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "ENTORNO DE PRUEBA / 01",
                    color = JellyBlack.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(30.dp))
                Text(
                    text = "Compra.\nObserva.\nAprende.",
                    color = JellyBlack,
                    style = MaterialTheme.typography.displayLarge,
                )
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Un ecommerce ficticio para validar cada señal de analítica.",
                    color = JellyBlack.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(end = 44.dp),
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        analytics?.trackButton("btn_home_start_shopping", screen)
                        onProducts()
                    },
                    modifier = Modifier.testTag("btn_home_start_shopping"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JellyBlack,
                        contentColor = JellyCream,
                    ),
                ) {
                    Text(
                        text = "Explorar los ${Catalog.products.size} productos  →",
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricChip(
                label = "User ID",
                value = state.activeUserId ?: "No activo",
                modifier = Modifier.weight(1f),
            )
            MetricChip(
                label = "Carrito",
                value = "${state.itemCount} artículos",
                modifier = Modifier.weight(0.55f),
            )
        }

        Spacer(Modifier.height(32.dp))
        SectionEyebrow(index = "02", text = "Recorrido")
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tres plataformas.\nUn mismo flujo.",
            style = MaterialTheme.typography.headlineLarge,
            color = JellyCream,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Cada producto, gesto y pantalla está preparado para QA.",
            style = MaterialTheme.typography.bodyMedium,
            color = JellyMuted,
        )
        Spacer(Modifier.height(18.dp))
        JellyCard(modifier = Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FlowStep(
                    number = "01",
                    title = "Navega",
                    detail = "Home, catálogo y detalle registran screen_view y eventos ecommerce.",
                )
                HorizontalDivider(color = JellyBorder)
                FlowStep(
                    number = "02",
                    title = "Añade",
                    detail = "El carrito envía add_to_cart, remove_from_cart y view_cart.",
                )
                HorizontalDivider(color = JellyBorder)
                FlowStep(
                    number = "03",
                    title = "Convierte",
                    detail = "Checkout y purchase llegan a GA4, AppsFlyer y Quantum Metric.",
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        SectionEyebrow(index = "03", text = "Herramientas")
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            JellyCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "WEB\nVIEW",
                        style = MaterialTheme.typography.titleLarge,
                        color = JellyAmber,
                        lineHeight = 23.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "marcormz.com",
                        style = MaterialTheme.typography.bodySmall,
                        color = JellyMuted,
                    )
                    Spacer(Modifier.height(14.dp))
                    JellySecondaryButton(
                        text = "Abrir",
                        buttonId = "btn_home_webview",
                        onClick = onWebView,
                    )
                }
            }
            JellyCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text(
                        text = "TEST\nLAB",
                        style = MaterialTheme.typography.titleLarge,
                        color = JellyAmber,
                        lineHeight = 23.sp,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Login y eventos",
                        style = MaterialTheme.typography.bodySmall,
                        color = JellyMuted,
                    )
                    Spacer(Modifier.height(14.dp))
                    JellySecondaryButton(
                        text = "Abrir",
                        buttonId = "btn_home_account",
                        onClick = onAccount,
                    )
                }
            }
        }

        Spacer(Modifier.height(26.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(JellySurface, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Catálogo local",
                style = MaterialTheme.typography.labelMedium,
                color = JellyMuted,
            )
            Text(
                text = "${Catalog.products.size} productos · MXN",
                style = MaterialTheme.typography.labelMedium,
                color = JellyCream,
            )
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun FlowStep(
    number: String,
    title: String,
    detail: String,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(JellyAmber, RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = number,
                color = JellyBlack,
                fontFamily = Sora,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = JellyCream,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = JellyMuted,
            )
        }
    }
}
