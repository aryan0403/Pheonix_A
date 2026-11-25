package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DatavisionDarkColorScheme = darkColorScheme(
    primary = CyanGlow,
    onPrimary = Navy950,
    primaryContainer = Navy700,
    onPrimaryContainer = CyanGlow,
    secondary = VioletGlow,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF2E1E59),
    onSecondaryContainer = Color(0xFFDDD6FE),
    tertiary = EmeraldGlow,
    onTertiary = Navy950,
    tertiaryContainer = Color(0xFF064E3B),
    onTertiaryContainer = Color(0xFFA7F3D0),
    background = Navy950,
    onBackground = Slate100,
    surface = Navy900,
    onSurface = Slate100,
    surfaceVariant = Navy800,
    onSurfaceVariant = Slate300,
    outline = GlassBorder,
    outlineVariant = Color(0xFF1E293B),
    error = RoseError,
    onError = Navy950
)

private val DatavisionShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DatavisionTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DatavisionDarkColorScheme,
        typography = DatavisionTypography,
        shapes = DatavisionShapes,
        content = content
    )
}
