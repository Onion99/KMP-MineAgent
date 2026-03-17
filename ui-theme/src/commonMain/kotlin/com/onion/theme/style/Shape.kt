/*
 * Copyright 2024 The ZZZ Archive Open Source Project by mrfatworm
 * License: MIT License
 */

package com.onion.theme.style

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp


data class Shape(
    val r100: RoundedCornerShape,
    val r200: RoundedCornerShape,
    val r250: RoundedCornerShape,
    val r300: RoundedCornerShape,
    val r350: RoundedCornerShape,
    val r400: RoundedCornerShape,
    val r450: RoundedCornerShape,
    val r500: RoundedCornerShape,
    val small: RoundedCornerShape,
    val medium: RoundedCornerShape,
    val large: RoundedCornerShape
) {
    companion object {
        fun regular() = Shape(
            r100 = RoundedCornerShape(2.dp),
            r200 = RoundedCornerShape(4.dp),
            r250 = RoundedCornerShape(6.dp),
            r300 = RoundedCornerShape(8.dp),
            r350 = RoundedCornerShape(12.dp),
            r400 = RoundedCornerShape(16.dp),
            r450 = RoundedCornerShape(24.dp),
            r500 = RoundedCornerShape(32.dp),
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(16.dp)
        )
    }
}
