/*
 * Ethereal Minimalism — Color Scheme
 * Full Material3 color role set with light and dark variants.
 */

package com.onion.theme.style

import androidx.compose.material3.ColorScheme as M3ColorScheme
import androidx.compose.material3.darkColorScheme as m3DarkColorScheme
import androidx.compose.material3.lightColorScheme as m3LightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
class ColorScheme(
    // ── Primary ─────────────────────────────────────────────────
    val primary: Color = Color.Unspecified,
    val onPrimary: Color = Color.Unspecified,
    val primaryContainer: Color = Color.Unspecified,
    val onPrimaryContainer: Color = Color.Unspecified,
    val inversePrimary: Color = Color.Unspecified,

    // ── Secondary ───────────────────────────────────────────────
    val secondary: Color = Color.Unspecified,
    val onSecondary: Color = Color.Unspecified,
    val secondaryContainer: Color = Color.Unspecified,
    val onSecondaryContainer: Color = Color.Unspecified,

    // ── Tertiary ────────────────────────────────────────────────
    val tertiary: Color = Color.Unspecified,
    val onTertiary: Color = Color.Unspecified,
    val tertiaryContainer: Color = Color.Unspecified,
    val onTertiaryContainer: Color = Color.Unspecified,

    // ── Error ───────────────────────────────────────────────────
    val error: Color = Color.Unspecified,
    val onError: Color = Color.Unspecified,
    val errorContainer: Color = Color.Unspecified,
    val onErrorContainer: Color = Color.Unspecified,

    // ── Surface ─────────────────────────────────────────────────
    val surface: Color = Color.Unspecified,
    val surfaceDim: Color = Color.Unspecified,
    val surfaceBright: Color = Color.Unspecified,
    val surfaceContainerLowest: Color = Color.Unspecified,
    val surfaceContainerLow: Color = Color.Unspecified,
    val surfaceContainer: Color = Color.Unspecified,
    val surfaceContainerHigh: Color = Color.Unspecified,
    val surfaceContainerHighest: Color = Color.Unspecified,
    val onSurface: Color = Color.Unspecified,
    val onSurfaceVariant: Color = Color.Unspecified,
    val inverseSurface: Color = Color.Unspecified,
    val inverseOnSurface: Color = Color.Unspecified,
    val surfaceTint: Color = Color.Unspecified,

    // ── Outline ─────────────────────────────────────────────────
    val outline: Color = Color.Unspecified,
    val outlineVariant: Color = Color.Unspecified,

    // ── Background ──────────────────────────────────────────────
    val background: Color = Color.Unspecified,
    val onBackground: Color = Color.Unspecified,
    val surfaceVariant: Color = Color.Unspecified,

    // ── Fixed Colors ────────────────────────────────────────────
    val primaryFixed: Color = Color.Unspecified,
    val primaryFixedDim: Color = Color.Unspecified,
    val onPrimaryFixed: Color = Color.Unspecified,
    val onPrimaryFixedVariant: Color = Color.Unspecified,
    val secondaryFixed: Color = Color.Unspecified,
    val secondaryFixedDim: Color = Color.Unspecified,
    val onSecondaryFixed: Color = Color.Unspecified,
    val onSecondaryFixedVariant: Color = Color.Unspecified,
    val tertiaryFixed: Color = Color.Unspecified,
    val tertiaryFixedDim: Color = Color.Unspecified,
    val onTertiaryFixed: Color = Color.Unspecified,
    val onTertiaryFixedVariant: Color = Color.Unspecified,
)

/**
 * Light scheme — Ethereal Minimalism
 * Direct mapping from Design.md values: warm parchment base,
 * sage green primary, dusty blue secondary, slate gray tertiary.
 */
internal val lightScheme = ColorScheme(
    // Primary (Sage Green)
    primary = sageGreen,
    onPrimary = sageGreenOnPrimary,
    primaryContainer = sageGreenContainer,
    onPrimaryContainer = sageGreenOnContainer,
    inversePrimary = sageGreenInverse,
    // Secondary (Dusty Blue)
    secondary = dustyBlue,
    onSecondary = dustyBlueOnSecondary,
    secondaryContainer = dustyBlueContainer,
    onSecondaryContainer = dustyBlueOnContainer,
    // Tertiary (Slate Gray)
    tertiary = slateGray,
    onTertiary = slateGrayOnTertiary,
    tertiaryContainer = slateGrayContainer,
    onTertiaryContainer = slateGrayOnContainer,
    // Error
    error = errorRed,
    onError = errorOnRed,
    errorContainer = com.onion.theme.style.errorContainer,
    onErrorContainer = com.onion.theme.style.errorOnContainer,
    // Surface (Parchment)
    surface = parchment,
    surfaceDim = parchmentDim,
    surfaceBright = parchmentBright,
    surfaceContainerLowest = parchmentContainerLowest,
    surfaceContainerLow = parchmentContainerLow,
    surfaceContainer = parchmentContainer,
    surfaceContainerHigh = parchmentContainerHigh,
    surfaceContainerHighest = parchmentContainerHighest,
    onSurface = parchmentOnSurface,
    onSurfaceVariant = parchmentOnSurfaceVariant,
    inverseSurface = parchmentInverseSurface,
    inverseOnSurface = parchmentInverseOnSurface,
    surfaceTint = parchmentSurfaceTint,
    // Outline
    outline = outlineDefault,
    outlineVariant = com.onion.theme.style.outlineVariant,
    // Background
    background = parchment,
    onBackground = parchmentOnSurface,
    surfaceVariant = parchmentSurfaceVariant,
    // Fixed
    primaryFixed = com.onion.theme.style.primaryFixed,
    primaryFixedDim = com.onion.theme.style.primaryFixedDim,
    onPrimaryFixed = com.onion.theme.style.onPrimaryFixed,
    onPrimaryFixedVariant = com.onion.theme.style.onPrimaryFixedVariant,
    secondaryFixed = com.onion.theme.style.secondaryFixed,
    secondaryFixedDim = com.onion.theme.style.secondaryFixedDim,
    onSecondaryFixed = com.onion.theme.style.onSecondaryFixed,
    onSecondaryFixedVariant = com.onion.theme.style.onSecondaryFixedVariant,
    tertiaryFixed = com.onion.theme.style.tertiaryFixed,
    tertiaryFixedDim = com.onion.theme.style.tertiaryFixedDim,
    onTertiaryFixed = com.onion.theme.style.onTertiaryFixed,
    onTertiaryFixedVariant = com.onion.theme.style.onTertiaryFixedVariant,
)

/**
 * Dark scheme — derived from the light scheme by inverting tonal mapping.
 * Maintains the Ethereal Minimalism aesthetic with a warm dark base
 * rather than pure black — feels like twilight parchment.
 */
internal val darkScheme = ColorScheme(
    // Primary (Sage Green — lighter in dark mode)
    primary = sageGreenInverse,
    onPrimary = sageGreenDark,
    primaryContainer = sageGreenDark,
    onPrimaryContainer = com.onion.theme.style.primaryFixed,
    inversePrimary = sageGreen,
    // Secondary (Dusty Blue — lighter in dark mode)
    secondary = secondaryFixedDim,
    onSecondary = dustyBlueDark,
    secondaryContainer = dustyBlueDark,
    onSecondaryContainer = com.onion.theme.style.secondaryFixed,
    // Tertiary (Slate Gray — lighter in dark mode)
    tertiary = tertiaryFixedDim,
    onTertiary = slateGrayDark,
    tertiaryContainer = slateGrayDark,
    onTertiaryContainer = com.onion.theme.style.tertiaryFixed,
    // Error
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    // Surface (Warm dark — twilight parchment)
    surface = Color(0xFF141311),
    surfaceDim = Color(0xFF141311),
    surfaceBright = Color(0xFF3B3935),
    surfaceContainerLowest = Color(0xFF0F0E0C),
    surfaceContainerLow = Color(0xFF1C1C18),
    surfaceContainer = Color(0xFF21201C),
    surfaceContainerHigh = Color(0xFF2B2A26),
    surfaceContainerHighest = Color(0xFF363530),
    onSurface = parchmentContainerHighest,
    onSurfaceVariant = Color(0xFFC6CBC4),
    inverseSurface = parchmentContainerHighest,
    inverseOnSurface = Color(0xFF31312C),
    surfaceTint = sageGreenInverse,
    // Outline
    outline = Color(0xFF90968E),
    outlineVariant = Color(0xFF424842),
    // Background
    background = Color(0xFF141311),
    onBackground = parchmentContainerHighest,
    surfaceVariant = Color(0xFF424842),
    // Fixed (same in both themes)
    primaryFixed = com.onion.theme.style.primaryFixed,
    primaryFixedDim = com.onion.theme.style.primaryFixedDim,
    onPrimaryFixed = com.onion.theme.style.onPrimaryFixed,
    onPrimaryFixedVariant = com.onion.theme.style.onPrimaryFixedVariant,
    secondaryFixed = com.onion.theme.style.secondaryFixed,
    secondaryFixedDim = com.onion.theme.style.secondaryFixedDim,
    onSecondaryFixed = com.onion.theme.style.onSecondaryFixed,
    onSecondaryFixedVariant = com.onion.theme.style.onSecondaryFixedVariant,
    tertiaryFixed = com.onion.theme.style.tertiaryFixed,
    tertiaryFixedDim = com.onion.theme.style.tertiaryFixedDim,
    onTertiaryFixed = com.onion.theme.style.onTertiaryFixed,
    onTertiaryFixedVariant = com.onion.theme.style.onTertiaryFixedVariant,
)

/**
 * Bridge to Material3 ColorScheme — enables MaterialTheme components
 * to automatically pick up our Ethereal Minimalism colors.
 */
fun ColorScheme.toMaterial3ColorScheme(isDark: Boolean): M3ColorScheme {
    return if (isDark) {
        m3DarkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            surface = surface,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            surfaceTint = surfaceTint,
            outline = outline,
            outlineVariant = outlineVariant,
            background = background,
            onBackground = onBackground,
            surfaceVariant = surfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
        )
    } else {
        m3LightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            inversePrimary = inversePrimary,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary,
            onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer,
            onTertiaryContainer = onTertiaryContainer,
            error = error,
            onError = onError,
            errorContainer = errorContainer,
            onErrorContainer = onErrorContainer,
            surface = surface,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            inverseSurface = inverseSurface,
            inverseOnSurface = inverseOnSurface,
            surfaceTint = surfaceTint,
            outline = outline,
            outlineVariant = outlineVariant,
            background = background,
            onBackground = onBackground,
            surfaceVariant = surfaceVariant,
            surfaceContainerLowest = surfaceContainerLowest,
            surfaceContainerLow = surfaceContainerLow,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            surfaceContainerHighest = surfaceContainerHighest,
            surfaceDim = surfaceDim,
            surfaceBright = surfaceBright,
        )
    }
}
