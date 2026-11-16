package com.marcormz.jellyapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marcormz.jellyapp.analytics.JellyAnalytics
import com.marcormz.jellyapp.data.Product
import com.marcormz.jellyapp.ui.theme.JellyAmber
import com.marcormz.jellyapp.ui.theme.JellyAmberDim
import com.marcormz.jellyapp.ui.theme.JellyBlack
import com.marcormz.jellyapp.ui.theme.JellyBorder
import com.marcormz.jellyapp.ui.theme.JellyCream
import com.marcormz.jellyapp.ui.theme.JellyError
import com.marcormz.jellyapp.ui.theme.JellyMuted
import com.marcormz.jellyapp.ui.theme.JellySuccess
import com.marcormz.jellyapp.ui.theme.JellySurface
import com.marcormz.jellyapp.ui.theme.JellySurfaceRaised
import com.marcormz.jellyapp.ui.theme.Sora

val LocalJellyAnalytics = staticCompositionLocalOf<JellyAnalytics?> { null }
val LocalCurrentScreen = staticCompositionLocalOf { "unknown" }

@Composable
fun JellyPrimaryButton(
    text: String,
    buttonId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    compact: Boolean = false,
    onClick: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    Button(
        onClick = {
            analytics?.trackButton(buttonId, screen)
            onClick()
        },
        modifier = modifier
            .testTag(buttonId)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 14.dp),
        contentPadding = ButtonDefaults.ContentPadding,
        colors = ButtonDefaults.buttonColors(
            containerColor = JellyAmber,
            contentColor = JellyBlack,
            disabledContainerColor = JellyBorder,
            disabledContentColor = JellyMuted,
        ),
    ) {
        Text(
            text = text,
            style = if (compact) {
                MaterialTheme.typography.labelMedium
            } else {
                MaterialTheme.typography.labelLarge
            },
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
        )
    }
}

@Composable
fun JellySecondaryButton(
    text: String,
    buttonId: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    OutlinedButton(
        onClick = {
            analytics?.trackButton(buttonId, screen)
            onClick()
        },
        modifier = modifier
            .testTag(buttonId)
            .defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (enabled) JellyAmber else JellyBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (enabled) JellyCream else JellyMuted,
            disabledContentColor = JellyMuted,
        ),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun JellyQuantityButton(
    text: String,
    buttonId: String,
    contentDescription: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    Box(
        modifier = Modifier
            .size(40.dp)
            .testTag(buttonId)
            .semantics { this.contentDescription = contentDescription }
            .clip(RoundedCornerShape(10.dp))
            .background(JellySurfaceRaised)
            .clickable(enabled = enabled, role = Role.Button) {
                analytics?.trackButton(buttonId, screen)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (enabled) JellyCream else JellyMuted,
            fontSize = 20.sp,
            fontFamily = Sora,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun JellyBackButton(
    buttonId: String,
    onClick: () -> Unit,
) {
    val analytics = LocalJellyAnalytics.current
    val screen = LocalCurrentScreen.current
    Row(
        modifier = Modifier
            .testTag(buttonId)
            .defaultMinSize(minHeight = 48.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.Button) {
                analytics?.trackButton(buttonId, screen)
                onClick()
            }
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = "←", color = JellyAmber, fontSize = 22.sp, fontFamily = Sora)
        Text(
            text = "Volver",
            style = MaterialTheme.typography.labelMedium,
            color = JellyCream,
        )
    }
}

@Composable
fun JellyTopBar(
    title: String,
    backButtonId: String? = null,
    onBack: (() -> Unit)? = null,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (onBack != null && backButtonId != null) {
                JellyBackButton(buttonId = backButtonId, onClick = onBack)
            } else {
                Text(
                    text = "JELLY / QA",
                    style = MaterialTheme.typography.labelSmall,
                    color = JellyAmber,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = JellyMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        HorizontalDivider(color = JellyBorder, thickness = 1.dp)
    }
}

@Composable
fun SectionEyebrow(index: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = index,
            style = MaterialTheme.typography.labelSmall,
            color = JellyAmber,
        )
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(1.dp)
                .background(JellyBorder),
        )
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = JellyMuted,
        )
    }
}

@Composable
fun JellyCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = JellySurface),
        border = BorderStroke(1.dp, JellyBorder),
    ) {
        Box(modifier = Modifier.padding(18.dp)) {
            content()
        }
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(JellySurface)
            .padding(horizontal = 14.dp, vertical = 11.dp),
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = JellyMuted,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = JellyCream,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun JellyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = {
            if (placeholder.isNotBlank()) {
                Text(placeholder, color = JellyMuted)
            }
        },
        isError = isError,
        supportingText = supportingText?.let {
            { Text(it, color = if (isError) JellyError else JellyMuted) }
        },
        singleLine = singleLine,
        minLines = minLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = JellyCream,
            unfocusedTextColor = JellyCream,
            focusedContainerColor = JellySurface,
            unfocusedContainerColor = JellySurface,
            errorContainerColor = JellySurface,
            disabledContainerColor = JellySurface,
            cursorColor = JellyAmber,
            focusedBorderColor = JellyAmber,
            unfocusedBorderColor = JellyBorder,
            errorBorderColor = JellyError,
            focusedLabelColor = JellyAmber,
            unfocusedLabelColor = JellyMuted,
            errorLabelColor = JellyError,
        ),
    )
}

@Composable
fun StatusBanner(
    message: String,
    success: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (success) JellyAmberDim else MaterialTheme.colorScheme.errorContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(if (success) JellySuccess else JellyError),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = if (success) JellyCream else JellyError,
        )
    }
}

@Composable
fun ProductArtwork(
    product: Product,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(if (compact) 14.dp else 22.dp))
            .background(JellyAmberDim),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension * 0.38f
            drawCircle(
                color = JellyAmber,
                radius = radius,
                center = Offset(size.width * 0.72f, size.height * 0.3f),
            )
            drawCircle(
                color = JellyAmber.copy(alpha = 0.38f),
                radius = radius * 0.7f,
                center = Offset(size.width * 0.22f, size.height * 0.82f),
                style = Stroke(width = 2.dp.toPx()),
            )
            drawLine(
                color = JellyAmber.copy(alpha = 0.55f),
                start = Offset(0f, size.height * 0.72f),
                end = Offset(size.width, size.height * 0.42f),
                strokeWidth = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        Text(
            text = product.artworkCode,
            color = JellyBlack,
            fontFamily = Sora,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 18.sp else 38.sp,
            letterSpacing = (-1).sp,
        )
    }
}

@Composable
fun PriceText(
    cents: Int,
    modifier: Modifier = Modifier,
    large: Boolean = false,
) {
    Text(
        text = com.marcormz.jellyapp.data.Money.format(cents),
        modifier = modifier,
        color = JellyAmber,
        style = if (large) {
            MaterialTheme.typography.headlineMedium
        } else {
            MaterialTheme.typography.titleMedium
        }.copy(fontFeatureSettings = "tnum"),
    )
}
