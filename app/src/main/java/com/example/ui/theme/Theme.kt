package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val CineGlassColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ObsidianDeep,
    primaryContainer = GlassCyanTint,
    onPrimaryContainer = NeonCyan,
    secondary = CinemaCoral,
    onSecondary = InkPure,
    secondaryContainer = GlassCoralTint,
    onSecondaryContainer = CinemaCoralSoft,
    tertiary = AuroraViolet,
    onTertiary = InkPure,
    tertiaryContainer = GlassVioletTint,
    onTertiaryContainer = InkPure,
    background = ObsidianDeep,
    onBackground = InkPure,
    surface = ObsidianSurface,
    onSurface = InkPure,
    surfaceVariant = ObsidianElevated,
    onSurfaceVariant = InkSecondary,
    surfaceContainer = ObsidianCard,
    outline = GlassBorderBright,
    outlineVariant = GlassBorderSubtle,
    error = CinemaCoral,
    onError = InkPure
)

private val CineGlassShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun CineGlassTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CineGlassColorScheme,
        typography = CineGlassTypography,
        shapes = CineGlassShapes,
        content = content
    )
}
