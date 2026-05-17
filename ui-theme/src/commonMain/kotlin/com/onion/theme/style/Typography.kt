/*
 * Ethereal Minimalism — Typography
 * Retains Metropolis font family. Hierarchy established through
 * font weight and generous line heights rather than excessive
 * size variations. Headlines use light weight to feel "airy."
 */

package com.onion.theme.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mineagent.ui_theme.generated.resources.metropolis_bold
import mineagent.ui_theme.generated.resources.metropolis_light
import mineagent.ui_theme.generated.resources.metropolis_medium
import mineagent.ui_theme.generated.resources.metropolis_regular
import mineagent.ui_theme.generated.resources.metropolis_semibold
import mineagent.ui_theme.generated.resources.Res
import org.jetbrains.compose.resources.Font

data class Typography(
    val displayLarge: TextStyle = TextStyle(),
    val displayMedium: TextStyle = TextStyle(),
    val displaySmall: TextStyle = TextStyle(),
    val headlineLarge: TextStyle = TextStyle(),
    val headlineMedium: TextStyle = TextStyle(),
    val headlineSmall: TextStyle = TextStyle(),
    val titleLarge: TextStyle = TextStyle(),
    val titleMedium: TextStyle = TextStyle(),
    val titleSmall: TextStyle = TextStyle(),
    val labelLarge: TextStyle = TextStyle(),
    val labelMedium: TextStyle = TextStyle(),
    val labelSmall: TextStyle = TextStyle(),
    val bodyLarge: TextStyle = TextStyle(),
    val bodyMedium: TextStyle = TextStyle(),
    val bodySmall: TextStyle = TextStyle()
)

@Composable
fun provideTypography(scale: Float = 1f): Typography {
    val metropolis = FontFamily(
        Font(Res.font.metropolis_light, FontWeight.Light),
        Font(Res.font.metropolis_regular, FontWeight.Normal),
        Font(Res.font.metropolis_medium, FontWeight.Medium),
        Font(Res.font.metropolis_semibold, FontWeight.SemiBold),
        Font(Res.font.metropolis_bold, FontWeight.Bold),
    )

    return Typography(
        // ── Display — monumental, reserved for hero text ──────────
        displayLarge = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Light,
            fontSize = 57.sp * scale,
            lineHeight = 72.sp * scale,
            letterSpacing = (-0.25).sp,
        ),
        displayMedium = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Light,
            fontSize = 45.sp * scale,
            lineHeight = 58.sp * scale,
        ),
        displaySmall = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Light,
            fontSize = 36.sp * scale,
            lineHeight = 48.sp * scale,
        ),

        // ── Headline — "airy" with light weight (300) ────────────
        // Design.md: headline-lg = 40px/300/52px/-0.02em
        headlineLarge = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Light,
            fontSize = 40.sp * scale,
            lineHeight = 52.sp * scale,
            letterSpacing = (-0.02).sp,
        ),
        // Design.md: headline-md = 28px/400/36px
        headlineMedium = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp * scale,
            lineHeight = 36.sp * scale,
        ),
        // headline-lg-mobile equivalent: 30px/300/38px/-0.01em
        headlineSmall = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Light,
            fontSize = 30.sp * scale,
            lineHeight = 38.sp * scale,
            letterSpacing = (-0.01).sp,
        ),

        // ── Title ────────────────────────────────────────────────
        titleLarge = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp * scale,
            lineHeight = 33.sp * scale,
        ),
        titleMedium = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp * scale,
            lineHeight = 24.sp * scale,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp * scale,
            lineHeight = 21.sp * scale,
            letterSpacing = 0.1.sp,
        ),

        // ── Label — semi-bold with wider tracking ────────────────
        // Design.md: label-md = 14px/600/20px/0.05em
        labelLarge = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp * scale,
            lineHeight = 24.sp * scale,
            letterSpacing = 0.05.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp * scale,
            lineHeight = 20.sp * scale,
            letterSpacing = 0.05.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp * scale,
            lineHeight = 16.sp * scale,
            letterSpacing = 0.05.sp,
        ),

        // ── Body — ≥ 1.5x line-height for "ethereal whitespace" ─
        // Design.md: body-lg = 18px/400/30px (1.67x)
        bodyLarge = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp * scale,
            lineHeight = 30.sp * scale,
        ),
        // Design.md: body-md = 16px/400/26px (1.625x)
        bodyMedium = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp * scale,
            lineHeight = 26.sp * scale,
        ),
        // Design.md: caption = 12px/400/16px
        bodySmall = TextStyle(
            fontFamily = metropolis,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp * scale,
            lineHeight = 16.sp * scale,
        )
    )
}