/*
 * Ethereal Minimalism — Shape
 * Organic shape language with soft, approachable roundedness.
 * Prominent containers use 32dp radius; pills/badges use full rounding.
 */

package com.onion.theme.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

data class Shape(
    /** 4dp — subtle rounding for small elements */
    val sm: RoundedCornerShape,
    /** 8dp — default rounding */
    val regular: RoundedCornerShape,
    /** 12dp — medium rounding */
    val md: RoundedCornerShape,
    /** 16dp — large rounding for cards */
    val lg: RoundedCornerShape,
    /** 24dp — extra-large rounding */
    val xl: RoundedCornerShape,
    /** 32dp — prominent containers, glassmorphism cards */
    val xxl: RoundedCornerShape,
    /** 9999dp — pills, chips, badges */
    val full: RoundedCornerShape,
    // ── Compatibility aliases ──────────────────────────────────
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape,
) {
    companion object {
        fun regular() = Shape(
            sm = RoundedCornerShape(4.dp),
            regular = RoundedCornerShape(8.dp),
            md = RoundedCornerShape(12.dp),
            lg = RoundedCornerShape(16.dp),
            xl = RoundedCornerShape(24.dp),
            xxl = RoundedCornerShape(32.dp),
            full = RoundedCornerShape(9999.dp),
            // Compatibility
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(16.dp),
        )
    }
}
