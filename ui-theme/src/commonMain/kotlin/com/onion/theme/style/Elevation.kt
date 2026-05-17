/*
 * Ethereal Minimalism — Elevation
 * Depth without weight: glassmorphism + tonal layering + ambient shadows.
 * Avoids heavy shadows; communicates depth through translucency and blur.
 */

package com.onion.theme.style

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ui.theme.AppTheme

/**
 * Elevation tokens for the Ethereal Minimalism design system.
 *
 * Three-layer depth model:
 * 1. **Base Layer** — Parchment neutral with watercolor texture
 * 2. **Surface Layer** — Semi-transparent frosted glass (60-80% opacity, 16-32dp blur)
 * 3. **Elevation Shadows** — Ambient, extra-diffused (20dp+ blur, 5-8% opacity)
 */
@Immutable
data class Elevation(
    /** Glass surface background opacity (0.6 ~ 0.8) */
    val glassSurfaceAlpha: Float = 0.7f,
    /** Backdrop blur radius for frosted glass effect */
    val glassBlurRadius: Dp = 24.dp,
    /** 1px glass border opacity */
    val glassBorderAlpha: Float = 0.15f,
    /** Ambient shadow opacity (5-8%) */
    val ambientShadowAlpha: Float = 0.06f,
    /** Ambient shadow blur radius (20dp+) */
    val ambientShadowBlur: Dp = 24.dp,
    /** Shadow tint color — Slate Gray tinted for natural feel */
    val ambientShadowColor: Color = Color(0x0F50606F),
) {
    companion object {
        fun regular() = Elevation()
    }
}

/**
 * Apply glassmorphism surface style.
 * Creates a frosted-glass card with translucent background,
 * soft border, and ambient shadow.
 */
@Composable
fun Modifier.glassSurface(
    shape: androidx.compose.foundation.shape.RoundedCornerShape = AppTheme.shape.xxl,
    alpha: Float = AppTheme.elevation.glassSurfaceAlpha,
    borderAlpha: Float = AppTheme.elevation.glassBorderAlpha,
): Modifier {
    val surfaceColor = AppTheme.colors.surfaceContainerLow
    val borderColor = AppTheme.colors.outline

    return this
        .shadow(
            elevation = AppTheme.elevation.ambientShadowBlur,
            shape = shape,
            ambientColor = AppTheme.elevation.ambientShadowColor,
            spotColor = Color.Transparent,
        )
        .clip(shape)
        .background(
            color = surfaceColor.copy(alpha = alpha),
            shape = shape,
        )
        .border(
            width = 1.dp,
            color = borderColor.copy(alpha = borderAlpha),
            shape = shape,
        )
}

/**
 * Apply a watercolor-inspired gradient background.
 * Soft transitions between primary and secondary tones.
 */
@Composable
fun Modifier.watercolorGradient(
    startColor: Color = AppTheme.colors.primaryFixed.copy(alpha = 0.3f),
    endColor: Color = AppTheme.colors.secondaryFixed.copy(alpha = 0.2f),
): Modifier {
    return this.background(
        brush = Brush.linearGradient(
            colors = listOf(startColor, endColor),
        )
    )
}
