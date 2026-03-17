package com.onion.theme.helper

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.onion.theme.state.AdaptiveLayoutType
import ui.theme.AppTheme

// Vertical edge-to-edge Padding for Container
@Composable
fun verticalSafePadding(): PaddingValues {
    val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val bottomPadding = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding()
    val spacing = AppTheme.spacing
    return PaddingValues(
        top = if (topPadding > 0.dp) topPadding else when (AppTheme.adaptiveLayoutType) {
            AdaptiveLayoutType.Expanded -> spacing.s400
            AdaptiveLayoutType.Medium -> spacing.s350
            AdaptiveLayoutType.Compact -> spacing.s300
        },
        bottom = if (bottomPadding > 0.dp) bottomPadding else when (AppTheme.adaptiveLayoutType) {
            AdaptiveLayoutType.Expanded -> spacing.s400
            AdaptiveLayoutType.Medium -> spacing.s350
            AdaptiveLayoutType.Compact -> spacing.s300
        }
    )
}