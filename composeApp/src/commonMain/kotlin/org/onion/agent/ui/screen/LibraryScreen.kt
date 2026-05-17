package org.onion.agent.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.library
import org.jetbrains.compose.resources.stringResource
import ui.theme.AppTheme


@Composable
fun LibraryScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            Icon(
                imageVector = Icons.Filled.LocalLibrary,
                contentDescription = stringResource(Res.string.library),
                modifier = Modifier.size(AppTheme.size.avatarLarge),
                tint = AppTheme.colors.secondary.copy(alpha = 0.4f)
            )
            Text(
                text = stringResource(Res.string.library),
                style = AppTheme.typography.headlineSmall,
                color = AppTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
