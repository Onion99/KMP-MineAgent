package org.onion.agent.ui.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onion.theme.state.ContentType
import com.onion.theme.style.glassSurface
import com.onion.theme.style.watercolorGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.onion.agent.viewmodel.ChatViewModel
import ui.theme.AppTheme

private data class ModelItem(
    val id: String,
    val titleRes: StringResource,
    val taglineRes: StringResource,
    val descRes: StringResource,
    val icon: ImageVector,
    val defaultPath: String
)

@Composable
fun ModelScreen() {
    val chatViewModel = koinInject<ChatViewModel>()
    val loadingState by chatViewModel.loadingModelState.collectAsState(0)
    val currentPath by chatViewModel.llmPath
    val coroutineScope = rememberCoroutineScope()

    val modelItems = remember {
        listOf(
            ModelItem(
                id = "llama3",
                titleRes = Res.string.model_llama3_title,
                taglineRes = Res.string.model_llama3_tagline,
                descRes = Res.string.model_llama3_desc,
                icon = Icons.Default.Memory,
                defaultPath = "llama3_8b_instruct.tflite"
            ),
            ModelItem(
                id = "mistral",
                titleRes = Res.string.model_mistral_title,
                taglineRes = Res.string.model_mistral_tagline,
                descRes = Res.string.model_mistral_desc,
                icon = Icons.Default.Speed,
                defaultPath = "mistral_7b_v0.2.tflite"
            ),
            ModelItem(
                id = "gemma",
                titleRes = Res.string.model_gemma_title,
                taglineRes = Res.string.model_gemma_tagline,
                descRes = Res.string.model_gemma_desc,
                icon = Icons.Default.AutoAwesome,
                defaultPath = "gemma_2b.tflite"
            ),
            ModelItem(
                id = "phi3",
                titleRes = Res.string.model_phi3_title,
                taglineRes = Res.string.model_phi3_tagline,
                descRes = Res.string.model_phi3_desc,
                icon = Icons.Default.Storage,
                defaultPath = "phi3_mini.tflite"
            )
        )
    }

    val activeId = remember(currentPath) {
        when {
            currentPath.contains("llama3", ignoreCase = true) -> "llama3"
            currentPath.contains("mistral", ignoreCase = true) -> "mistral"
            currentPath.contains("gemma", ignoreCase = true) -> "gemma"
            currentPath.contains("phi3", ignoreCase = true) -> "phi3"
            currentPath.isNotEmpty() -> "custom"
            else -> "" // default active if empty
        }
    }

    val onLoadClick: (ModelItem) -> Unit = { item ->
        coroutineScope.launch(Dispatchers.Default) {
            if (loadingState == 1) return@launch
            chatViewModel.llmPath.value = item.defaultPath
            chatViewModel.loadingModelState.emit(1)
            chatViewModel.initLLM()
        }
    }

    val onSelectCustomClick: () -> Unit = {
        coroutineScope.launch(Dispatchers.Default) {
            if (loadingState == 1) return@launch
            val path = chatViewModel.selectLlmFile()
            if (path.isNotBlank()) {
                chatViewModel.loadingModelState.emit(1)
                chatViewModel.initLLM()
            }
        }
    }

    val contentType = AppTheme.contentType

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.surface)
    ) {
        if (contentType == ContentType.Single) {
            // Mobile Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.spacing.lg),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                    Text(
                        text = stringResource(Res.string.model_select_mobile_title),
                        style = AppTheme.typography.headlineSmall,
                        color = AppTheme.colors.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.model_select_mobile_subtitle),
                        style = AppTheme.typography.bodyMedium,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }

                // Active Section
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                    Text(
                        text = stringResource(Res.string.model_section_active),
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.primary
                    )
                    val activeItem = modelItems.find { it.id == activeId }
                    if (activeItem != null) {
                        ModelCard(
                            item = activeItem,
                            isActive = true,
                            isLoading = loadingState == 1,
                            onLoadClick = {}
                        )
                    } else if (activeId == "custom") {
                        CustomModelCard(
                            currentPath = currentPath,
                            isActive = true,
                            isLoading = loadingState == 1,
                            onSelectClick = onSelectCustomClick
                        )
                    }
                }

                // Available Section
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                    Text(
                        text = stringResource(Res.string.model_section_available),
                        style = AppTheme.typography.labelMedium,
                        color = AppTheme.colors.tertiary
                    )
                    modelItems.filter { it.id != activeId }.forEach { item ->
                        ModelCard(
                            item = item,
                            isActive = false,
                            isLoading = false,
                            onLoadClick = { onLoadClick(item) }
                        )
                    }
                    if (activeId != "custom") {
                        CustomModelCard(
                            currentPath = currentPath,
                            isActive = false,
                            isLoading = false,
                            onSelectClick = onSelectCustomClick
                        )
                    }
                }
            }
        } else {
            // Desktop Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(AppTheme.spacing.containerPaddingDesktop),
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xl)
            ) {
                // Header
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
                    Text(
                        text = stringResource(Res.string.model_select_title),
                        style = AppTheme.typography.headlineLarge,
                        color = AppTheme.colors.onSurface
                    )
                    Text(
                        text = stringResource(Res.string.model_select_subtitle),
                        style = AppTheme.typography.bodyLarge,
                        color = AppTheme.colors.onSurfaceVariant
                    )
                }

                // Grid of cards
                val desktopItems = modelItems.toList()
                Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
                    // Row 1: Llama 3 & Mistral
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModelCard(
                                item = desktopItems[0],
                                isActive = desktopItems[0].id == activeId,
                                isLoading = desktopItems[0].id == activeId && loadingState == 1,
                                onLoadClick = { onLoadClick(desktopItems[0]) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ModelCard(
                                item = desktopItems[1],
                                isActive = desktopItems[1].id == activeId,
                                isLoading = desktopItems[1].id == activeId && loadingState == 1,
                                onLoadClick = { onLoadClick(desktopItems[1]) }
                            )
                        }
                    }
                    // Row 2: Gemma & Phi-3
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
                        Box(modifier = Modifier.weight(1f)) {
                            ModelCard(
                                item = desktopItems[2],
                                isActive = desktopItems[2].id == activeId,
                                isLoading = desktopItems[2].id == activeId && loadingState == 1,
                                onLoadClick = { onLoadClick(desktopItems[2]) }
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            ModelCard(
                                item = desktopItems[3],
                                isActive = desktopItems[3].id == activeId,
                                isLoading = desktopItems[3].id == activeId && loadingState == 1,
                                onLoadClick = { onLoadClick(desktopItems[3]) }
                            )
                        }
                    }
                    // Row 3: Custom Model Card
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
                        Box(modifier = Modifier.weight(1f)) {
                            CustomModelCard(
                                currentPath = currentPath,
                                isActive = activeId == "custom",
                                isLoading = activeId == "custom" && loadingState == 1,
                                onSelectClick = onSelectCustomClick
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    item: ModelItem,
    isActive: Boolean,
    isLoading: Boolean,
    onLoadClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) AppTheme.colors.primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "card_border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(
                shape = AppTheme.shape.xxl,
                alpha = if (isActive) AppTheme.elevation.glassSurfaceAlpha else 0.4f,
                borderAlpha = if (isActive) 0.5f else AppTheme.elevation.glassBorderAlpha
            )
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = borderColor,
                shape = AppTheme.shape.xxl
            )
            .let {
                if (isActive) {
                    it.watercolorGradient(
                        startColor = AppTheme.colors.primaryContainer.copy(alpha = 0.3f),
                        endColor = AppTheme.colors.secondaryContainer.copy(alpha = 0.2f)
                    )
                } else it
            }
            .padding(AppTheme.spacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Header: Icon + Title + Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isActive) AppTheme.colors.primaryContainer else AppTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            shape = AppTheme.shape.lg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = stringResource(item.titleRes),
                        modifier = Modifier.size(AppTheme.size.iconLarge),
                        tint = if (isActive) AppTheme.colors.onPrimaryContainer else AppTheme.colors.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(item.titleRes),
                        style = AppTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(item.taglineRes),
                        style = AppTheme.typography.labelMedium,
                        color = if (isActive) AppTheme.colors.primary else AppTheme.colors.secondary
                    )
                }
            }

            // Description
            Text(
                text = stringResource(item.descRes),
                style = AppTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceVariant,
                modifier = Modifier.padding(vertical = AppTheme.spacing.xs)
            )

            // Action Button / Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isActive) {
                    Row(
                        modifier = Modifier
                            .height(AppTheme.size.buttonHeight)
                            .background(
                                color = AppTheme.colors.primary,
                                shape = AppTheme.shape.full
                            )
                            .padding(horizontal = AppTheme.spacing.lg),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = AppTheme.colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = stringResource(Res.string.loading),
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.onPrimary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AppTheme.colors.onPrimary
                            )
                            Text(
                                text = stringResource(Res.string.model_action_active),
                                style = AppTheme.typography.labelMedium,
                                color = AppTheme.colors.onPrimary
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onLoadClick,
                        modifier = Modifier.height(AppTheme.size.buttonHeight),
                        shape = AppTheme.shape.full,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = AppTheme.colors.primary
                        ),
                        border = BorderStroke(1.dp, AppTheme.colors.primary)
                    ) {
                        Text(
                            text = stringResource(Res.string.model_action_load),
                            style = AppTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomModelCard(
    currentPath: String,
    isActive: Boolean,
    isLoading: Boolean,
    onSelectClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isActive) AppTheme.colors.primary else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "custom_card_border"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassSurface(
                shape = AppTheme.shape.xxl,
                alpha = if (isActive) AppTheme.elevation.glassSurfaceAlpha else 0.4f,
                borderAlpha = if (isActive) 0.5f else AppTheme.elevation.glassBorderAlpha
            )
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = borderColor,
                shape = AppTheme.shape.xxl
            )
            .let {
                if (isActive) {
                    it.watercolorGradient(
                        startColor = AppTheme.colors.primaryContainer.copy(alpha = 0.3f),
                        endColor = AppTheme.colors.secondaryContainer.copy(alpha = 0.2f)
                    )
                } else it
            }
            .padding(AppTheme.spacing.lg)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
        ) {
            // Header: Icon + Title + Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = if (isActive) AppTheme.colors.primaryContainer else AppTheme.colors.surfaceVariant.copy(alpha = 0.5f),
                            shape = AppTheme.shape.lg
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = stringResource(Res.string.model_custom_title),
                        modifier = Modifier.size(AppTheme.size.iconLarge),
                        tint = if (isActive) AppTheme.colors.onPrimaryContainer else AppTheme.colors.onSurfaceVariant
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.model_custom_title),
                        style = AppTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                        color = AppTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(Res.string.model_custom_tagline),
                        style = AppTheme.typography.labelMedium,
                        color = if (isActive) AppTheme.colors.primary else AppTheme.colors.secondary
                    )
                }
            }

            // Description & Current Path
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
                Text(
                    text = stringResource(Res.string.model_custom_desc),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant
                )
                if (currentPath.isNotEmpty() && isActive) {
                    Text(
                        text = "Path: ${currentPath.split("/", "\\").lastOrNull() ?: currentPath}",
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onSelectClick,
                    modifier = Modifier.height(AppTheme.size.buttonHeight),
                    shape = AppTheme.shape.full,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppTheme.colors.primary,
                        contentColor = AppTheme.colors.onPrimary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppTheme.colors.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(Res.string.loading),
                            style = AppTheme.typography.labelMedium
                        )
                    } else {
                        Text(
                            text = stringResource(Res.string.model_action_select_file),
                            style = AppTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}
