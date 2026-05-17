/*
 * Ethereal Minimalism — Spacing
 * Based on an 8px rhythmic scale. Includes layout tokens for
 * container padding and section gaps to create "breathing room."
 */

package com.onion.theme.style

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class Spacing(
    /** 8dp — base unit */
    val unit: Dp,
    /** 4dp — extra small */
    val xs: Dp,
    /** 8dp — small */
    val sm: Dp,
    /** 16dp — medium */
    val md: Dp,
    /** 24dp — large (= gutter) */
    val lg: Dp,
    /** 32dp — extra large */
    val xl: Dp,
    /** 48dp — 2x extra large */
    val xxl: Dp,
    /** 24dp — mobile container padding */
    val containerPaddingMobile: Dp,
    /** 64dp — desktop container padding */
    val containerPaddingDesktop: Dp,
    /** 24dp — grid gutter */
    val gutter: Dp,
    /** 80dp — gap between major content sections */
    val sectionGap: Dp,
    // ── Compatibility aliases (8px scale) ─────────────────────
    val s100: Dp,
    val s200: Dp,
    val s250: Dp,
    val s300: Dp,
    val s350: Dp,
    val s400: Dp,
    val s450: Dp,
    val s500: Dp,
) {
    companion object {
        fun regular() = Spacing(
            unit = 8.dp,
            xs = 4.dp,
            sm = 8.dp,
            md = 16.dp,
            lg = 24.dp,
            xl = 32.dp,
            xxl = 48.dp,
            containerPaddingMobile = 24.dp,
            containerPaddingDesktop = 64.dp,
            gutter = 24.dp,
            sectionGap = 80.dp,
            // Compatibility (unchanged)
            s100 = 2.dp,
            s200 = 4.dp,
            s250 = 6.dp,
            s300 = 8.dp,
            s350 = 12.dp,
            s400 = 16.dp,
            s450 = 24.dp,
            s500 = 32.dp,
        )
    }
}