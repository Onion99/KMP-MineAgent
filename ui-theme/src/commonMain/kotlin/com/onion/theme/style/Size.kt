/*
 * Ethereal Minimalism — Size
 * Streamlined fixed-size tokens aligned to the design system.
 */

package com.onion.theme.style

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Size(
    // ── Icon sizes ──────────────────────────────────────────────
    val iconSmall: Dp = Dp.Unspecified,
    val icon: Dp = Dp.Unspecified,
    val iconLarge: Dp = Dp.Unspecified,
    val iconButton: Dp = Dp.Unspecified,
    val iconButtonSmall: Dp = Dp.Unspecified,

    // ── Component sizes ─────────────────────────────────────────
    val buttonHeight: Dp = Dp.Unspecified,
    val chipHeight: Dp = Dp.Unspecified,
    val avatarSmall: Dp = Dp.Unspecified,
    val avatarMedium: Dp = Dp.Unspecified,
    val avatarLarge: Dp = Dp.Unspecified,

    // ── Layout sizes ────────────────────────────────────────────
    val borderWidth: Dp = Dp.Unspecified,
    val borderWidthThin: Dp = Dp.Unspecified,
    val maxContentWidth: Dp = Dp.Unspecified,

    // ── Card & container sizes ──────────────────────────────────
    val cardSmall: Dp = Dp.Unspecified,
    val cardMedium: Dp = Dp.Unspecified,
    val cardLarge: Dp = Dp.Unspecified,
)

fun provideSize(scale: Float = 1f): Size = Size(
    // Icons
    iconSmall = 12.dp * scale,
    icon = 18.dp * scale,
    iconLarge = 24.dp * scale,
    iconButton = 40.dp * scale,
    iconButtonSmall = 32.dp * scale,
    // Components
    buttonHeight = 48.dp * scale,
    chipHeight = 32.dp * scale,
    avatarSmall = 32.dp * scale,
    avatarMedium = 48.dp * scale,
    avatarLarge = 72.dp * scale,
    // Layout
    borderWidth = 1.dp * scale,
    borderWidthThin = 0.5.dp * scale,
    maxContentWidth = 1200.dp * scale,
    // Cards
    cardSmall = 160.dp * scale,
    cardMedium = 240.dp * scale,
    cardLarge = 320.dp * scale,
)