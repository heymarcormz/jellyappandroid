package com.marcormz.jellyapp.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val JellyColors = darkColorScheme(
    primary = JellyAmber,
    onPrimary = JellyBlack,
    primaryContainer = JellyAmberDim,
    onPrimaryContainer = JellyAmber,
    secondary = JellyCream,
    onSecondary = JellyBlack,
    background = JellyBlack,
    onBackground = JellyCream,
    surface = JellySurface,
    onSurface = JellyCream,
    surfaceVariant = JellySurfaceRaised,
    onSurfaceVariant = JellyMuted,
    outline = JellyBorder,
    error = JellyError,
    onError = JellyBlack,
)

private val JellyShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

@Composable
fun JellyTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = JellyColors,
        typography = JellyTypography,
        shapes = JellyShapes,
        content = content,
    )
}
