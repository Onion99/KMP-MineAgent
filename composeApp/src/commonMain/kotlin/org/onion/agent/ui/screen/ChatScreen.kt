package org.onion.agent.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardDoubleArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.onion.model.ChatMessage
import com.onion.theme.state.ContentType
import com.onion.theme.style.MediumOutlinedTextField
import com.onion.theme.style.glassSurface
import com.onion.theme.style.watercolorGradient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mineagent.composeapp.generated.resources.Res
import mineagent.composeapp.generated.resources.ai_image
import mineagent.composeapp.generated.resources.attachment
import mineagent.composeapp.generated.resources.copy
import mineagent.composeapp.generated.resources.creating
import mineagent.composeapp.generated.resources.error_no_interrupt_api
import mineagent.composeapp.generated.resources.feature_not_available
import mineagent.composeapp.generated.resources.regenerate
import mineagent.composeapp.generated.resources.save_image
import mineagent.composeapp.generated.resources.scroll_to_bottom
import mineagent.composeapp.generated.resources.send_message
import mineagent.composeapp.generated.resources.stop_generation
import mineagent.composeapp.generated.resources.text_copied
import mineagent.composeapp.generated.resources.user_image
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.onion.agent.utils.Animations
import org.onion.agent.viewmodel.ChatViewModel
import ui.theme.AppTheme
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
fun ChatScreen(
    onSettingsClick: () -> Unit = {},
    onAdvancedSettingsClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .safeDrawingPadding()
    ) {
        val chatViewModel = koinInject<ChatViewModel>()
        val chatMessages = chatViewModel.currentChatMessages
        var text by remember { mutableStateOf("") }
        val keyboardController = LocalSoftwareKeyboardController.current
        val focusManager = LocalFocusManager.current
        val snackbarHostState = remember { SnackbarHostState() }
        val coroutineScope = rememberCoroutineScope()

        // Ambient Watercolor Background Effects
        Box(modifier = Modifier.fillMaxSize().zIndex(0f)) {
            // Top-Left Glow
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = (-100).dp, y = (-100).dp)
                    .size(500.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppTheme.colors.primaryContainer.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Bottom-Right Glow
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 120.dp, y = 120.dp)
                    .size(600.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppTheme.colors.tertiaryContainer.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            // Center-Left Glow
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = 150.dp, y = 0.dp)
                    .size(400.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AppTheme.colors.secondaryContainer.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        Column(
            modifier = Modifier.fillMaxSize().zIndex(10f)
        ) {
            // Mobile Top Header
            if (AppTheme.contentType == ContentType.Single) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AppTheme.colors.surface.copy(alpha = 0.8f))
                        .padding(horizontal = AppTheme.spacing.containerPaddingMobile, vertical = 16.dp)
                        .zIndex(30f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aura",
                        style = AppTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Light, letterSpacing = 2.sp),
                        color = AppTheme.colors.primary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                chatViewModel.currentChatMessages.clear()
                            },
                            modifier = Modifier.size(40.dp).background(AppTheme.colors.primaryContainer.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "New Chat",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar(getString(Res.string.feature_not_available))
                                }
                            },
                            modifier = Modifier.size(40.dp).background(AppTheme.colors.primaryContainer.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.History,
                                contentDescription = "History",
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            // Chat History Scrollable Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                ChatMessagesList(
                    chatMessages = chatMessages,
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Input Area
            InputArea(
                text = text,
                isGenerating = chatViewModel.isGenerating.value,
                onAttachClick = {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(getString(Res.string.feature_not_available))
                    }
                },
                onSendClick = {
                    if (chatViewModel.isGenerating.value) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(getString(Res.string.error_no_interrupt_api))
                        }
                    } else {
                        if (text.isNotEmpty()) {
                            chatViewModel.sendMessage(text)
                            text = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }
                    }
                },
                onTextChange = { text = it }
            )
        }

        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
                .zIndex(50f),
            snackbar = { snackbarData ->
                Snackbar(
                    snackbarData,
                    modifier = Modifier
                        .widthIn(min = 100.dp, max = 300.dp)
                        .heightIn(min = 40.dp, max = 120.dp)
                        .padding(8.dp),
                    shape = RoundedCornerShape(26.dp),
                    containerColor = AppTheme.colors.tertiaryContainer,
                    contentColor = AppTheme.colors.onTertiaryContainer
                )
            }
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun ChatMessagesList(
    chatMessages: List<ChatMessage>,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val chatViewModel = koinInject<ChatViewModel>()
    val clipboardManager = LocalClipboardManager.current

    val showScrollButton by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = chatMessages.size
            if (totalItems == 0) false else {
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                lastVisibleItem < totalItems - 1
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 24.dp,
                bottom = 32.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (AppTheme.contentType == ContentType.Single) "Today, 10:24 AM" else "Today, serene morning",
                        style = AppTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .background(
                                color = AppTheme.colors.surfaceVariant.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = AppTheme.colors.outlineVariant.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            items(chatMessages, key = { it.id }) { message ->
                Box(modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()) {
                    ChatBubble(
                        message = message,
                        onSaveImage = { imageData ->
                            coroutineScope.launch(Dispatchers.Default) {
                                val fileName = "diffusion_${Clock.System.now().toEpochMilliseconds()}.png"
                                // val success = chatViewModel.diffusionLoader.saveImage(imageData, fileName, message.metadata)
                            }
                        },
                        onRegenerate = if (message.metadata?.containsKey("prompt") == true) {
                            {
                                if (chatViewModel.isGenerating.value) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(getString(Res.string.error_no_interrupt_api))
                                    }
                                } else chatViewModel.reGenerateMessage(message)
                            }
                        } else null,
                        onCopyText = { textToCopy ->
                            clipboardManager.setText(AnnotatedString(textToCopy))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(getString(Res.string.text_copied))
                            }
                        }
                    )
                }
            }
        }

        ScrollToBottomButton(
            onClick = {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(chatMessages.size)
                }
            },
            visibility = showScrollButton,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp)
        )
    }

    val lastMessageLength by remember(chatMessages.size) {
        derivedStateOf { chatMessages.lastOrNull()?.message?.length ?: 0 }
    }

    LaunchedEffect(chatMessages.size, lastMessageLength) {
        if (chatMessages.isNotEmpty()) {
            val lastIndex = chatMessages.lastIndex
            val scrollThreshold = 3
            val layoutInfo = lazyListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if ((visibleItems.lastOrNull()?.index ?: 0) >= lastIndex - scrollThreshold) {
                lazyListState.scrollToItem(chatMessages.size)
            }
        }
    }
}

@Composable
private fun ScrollToBottomButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    visibility: Boolean
) {
    AnimatedVisibility(
        visible = visibility,
        enter = Animations.slideFadeIn(),
        exit = Animations.slideFadeOut(),
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .background(
                    color = AppTheme.colors.primaryContainer,
                    shape = CircleShape
                )
                .shadow(6.dp, CircleShape)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardDoubleArrowDown,
                contentDescription = stringResource(Res.string.scroll_to_bottom),
                tint = AppTheme.colors.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    onSaveImage: ((ByteArray) -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null
) {
    val isSingle = AppTheme.contentType == ContentType.Single
    val isUser = message.isUser
    val textContent = message.message
    val image = message.image
    val metadata = message.metadata

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isUser) (if (isSingle) 48.dp else 88.dp) else 0.dp,
                end = if (isUser) 0.dp else (if (isSingle) 48.dp else 88.dp),
                top = 8.dp,
                bottom = 8.dp
            ),
        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        if (isUser) {
            // User Message Bubble
            if (isSingle) {
                // Mobile User Bubble
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 4.dp, bottomStart = 24.dp),
                            spotColor = AppTheme.colors.primary.copy(alpha = 0.3f)
                        )
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    AppTheme.colors.primary.copy(alpha = 0.9f),
                                    AppTheme.colors.surfaceTint.copy(alpha = 0.9f)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 4.dp, bottomStart = 24.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    UserMessageContent(textContent, image, onCopyText, isSingle = true)
                }
            } else {
                // Desktop User Bubble
                Box(
                    modifier = Modifier
                        .glassSurface(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 24.dp),
                            alpha = AppTheme.elevation.glassSurfaceAlpha,
                            borderAlpha = AppTheme.elevation.glassBorderAlpha
                        )
                        .background(
                            color = AppTheme.colors.surfaceContainerHigh.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppTheme.colors.outlineVariant.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomEnd = 24.dp, bottomStart = 24.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    UserMessageContent(textContent, image, onCopyText, isSingle = false)
                }
            }
        } else {
            // AI Message Bubble
            if (isSingle) {
                // Mobile AI Bubble
                Box(
                    modifier = Modifier
                        .glassSurface(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp),
                            alpha = AppTheme.elevation.glassSurfaceAlpha,
                            borderAlpha = AppTheme.elevation.glassBorderAlpha
                        )
                        .background(
                            color = AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppTheme.colors.surfaceContainerHigh,
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 4.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    AiMessageContent(textContent, image, onSaveImage, onRegenerate, onCopyText, metadata, isSingle = true)
                }
            } else {
                // Desktop AI Bubble with Avatar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // AI Avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .shadow(
                                elevation = 8.dp,
                                shape = CircleShape,
                                spotColor = AppTheme.colors.primary.copy(alpha = 0.2f)
                            )
                            .background(
                                color = AppTheme.colors.primaryContainer.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                            .border(
                                width = 1.dp,
                                color = AppTheme.colors.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "AI Avatar",
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // AI Bubble Container
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .glassSurface(
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 8.dp),
                                alpha = AppTheme.elevation.glassSurfaceAlpha,
                                borderAlpha = AppTheme.elevation.glassBorderAlpha
                            )
                            .background(
                                color = AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppTheme.colors.outlineVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 8.dp)
                            )
                            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 24.dp, bottomStart = 8.dp))
                    ) {
                        // Soft internal glow simulating watercolor bleeding
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            AppTheme.colors.primaryContainer.copy(alpha = 0.15f),
                                            Color.Transparent
                                        )
                                    )
                                )
                                .align(Alignment.TopCenter)
                        )

                        Box(modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp)) {
                            AiMessageContent(textContent, image, onSaveImage, onRegenerate, onCopyText, metadata, isSingle = false)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserMessageContent(
    message: String,
    image: ByteArray? = null,
    onCopyText: ((String) -> Unit)? = null,
    isSingle: Boolean
) {
    val textColor = if (isSingle) AppTheme.colors.onPrimary else AppTheme.colors.onSurface

    Column(modifier = Modifier.fillMaxWidth()) {
        if (image != null) {
            AsyncImage(
                model = image,
                contentDescription = stringResource(Res.string.user_image),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .padding(bottom = 8.dp)
            )
        }
        if (message.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = message,
                    style = if (isSingle) AppTheme.typography.bodyMedium else AppTheme.typography.bodyLarge,
                    color = textColor,
                    modifier = Modifier
                        .padding(top = 4.dp, end = 8.dp)
                        .weight(1f)
                )
                if (onCopyText != null) {
                    IconButton(
                        onClick = { onCopyText.invoke(message) },
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(Res.string.copy),
                            tint = if (isSingle) AppTheme.colors.onPrimary.copy(alpha = 0.7f) else AppTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiMessageContent(
    message: String,
    image: ByteArray? = null,
    onSaveImage: ((ByteArray) -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null,
    onCopyText: ((String) -> Unit)? = null,
    metadata: Map<String, String>? = null,
    isSingle: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (image != null) {
            Box(modifier = Modifier.wrapContentSize().padding(bottom = 12.dp)) {
                val widthStr = metadata?.get("width")
                val heightStr = metadata?.get("height")
                val imgModifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .let { m ->
                        if (widthStr != null && heightStr != null && widthStr.toFloatOrNull() != null && heightStr.toFloatOrNull() != null) {
                            val w = widthStr.toFloat()
                            val h = heightStr.toFloat()
                            m.aspectRatio(w / h)
                        } else {
                            m.wrapContentSize()
                        }
                    }

                AsyncImage(
                    model = image,
                    contentDescription = stringResource(Res.string.ai_image),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit,
                    modifier = imgModifier
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = AppTheme.colors.surface.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onRegenerate != null) {
                        IconButton(
                            onClick = onRegenerate,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                contentDescription = stringResource(Res.string.regenerate),
                                tint = AppTheme.colors.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { onSaveImage?.invoke(image) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.SaveAlt,
                            contentDescription = stringResource(Res.string.save_image),
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        val isGenerating = metadata?.get("is_generating") == "true"

        if (message.isEmpty() && isGenerating) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = AppTheme.colors.primary,
                    strokeWidth = 2.dp
                )
                Text(
                    text = stringResource(Res.string.creating) + "...",
                    style = if (isSingle) AppTheme.typography.bodyMedium else AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.onSurfaceVariant,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        } else if (message.isNotEmpty() || isGenerating) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val displayText = if (isGenerating) "$message ▌" else message
                Text(
                    text = displayText,
                    style = if (isSingle) AppTheme.typography.bodyMedium else AppTheme.typography.bodyLarge,
                    color = AppTheme.colors.onSurface.copy(alpha = 0.9f),
                    modifier = Modifier
                        .padding(top = 4.dp, end = 8.dp)
                        .weight(1f)
                )
                if (onCopyText != null) {
                    IconButton(
                        onClick = { onCopyText.invoke(message) },
                        modifier = Modifier.size(24.dp).padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = stringResource(Res.string.copy),
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InputArea(
    text: String,
    isGenerating: Boolean,
    onAttachClick: () -> Unit,
    onSendClick: () -> Unit,
    onTextChange: (String) -> Unit
) {
    val isSingle = AppTheme.contentType == ContentType.Single

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        AppTheme.colors.background.copy(alpha = 0.8f),
                        AppTheme.colors.background
                    )
                )
            )
            .padding(
                start = if (isSingle) AppTheme.spacing.containerPaddingMobile else 48.dp,
                end = if (isSingle) AppTheme.spacing.containerPaddingMobile else 48.dp,
                top = 24.dp,
                bottom = if (isSingle) 16.dp else 32.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth()
        ) {
            if (isSingle) {
                // Mobile Input Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 10.dp,
                            shape = RoundedCornerShape(32.dp),
                            spotColor = AppTheme.colors.primary.copy(alpha = 0.15f)
                        )
                        .glassSurface(
                            shape = RoundedCornerShape(32.dp),
                            alpha = AppTheme.elevation.glassSurfaceAlpha,
                            borderAlpha = AppTheme.elevation.glassBorderAlpha
                        )
                        .background(
                            color = AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.8f),
                            shape = RoundedCornerShape(32.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = AppTheme.colors.surfaceContainerHigh,
                            shape = RoundedCornerShape(32.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onAttachClick,
                        modifier = Modifier.size(40.dp).align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AttachFile,
                            contentDescription = stringResource(Res.string.attachment),
                            tint = AppTheme.colors.outline,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 120.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (text.isEmpty()) {
                            Text(
                                text = "Share your thoughts...",
                                style = AppTheme.typography.bodyMedium,
                                color = AppTheme.colors.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        MediumOutlinedTextField(
                            value = text,
                            onValueChange = onTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                            ),
                            style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface)
                        )
                    }

                    IconButton(
                        onClick = onSendClick,
                        modifier = Modifier
                            .background(
                                color = AppTheme.colors.primaryContainer.copy(alpha = 0.3f),
                                shape = CircleShape
                            ).align(Alignment.CenterVertically).size(40.dp)
                    ) {
                        Icon(
                            imageVector = if (isGenerating) Icons.Filled.Stop else Icons.Filled.ArrowUpward,
                            contentDescription = if (isGenerating) stringResource(Res.string.stop_generation) else stringResource(Res.string.send_message),
                            tint = AppTheme.colors.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                // Desktop Input Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .glassSurface(
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
                                alpha = AppTheme.elevation.glassSurfaceAlpha,
                                borderAlpha = AppTheme.elevation.glassBorderAlpha
                            )
                            .background(
                                color = AppTheme.colors.surfaceContainerLowest.copy(alpha = 0.6f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = AppTheme.colors.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom,
                        //horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = onAttachClick,
                            modifier = Modifier.size(40.dp).align(Alignment.CenterVertically)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AttachFile,
                                contentDescription = stringResource(Res.string.attachment),
                                tint = AppTheme.colors.outline,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 30.dp, max = 150.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (text.isEmpty()) {
                                Text(
                                    text = "Whisper your thoughts to the void...",
                                    style = AppTheme.typography.bodyMedium,
                                    color = AppTheme.colors.outline.copy(alpha = 0.5f)
                                )
                            }
                            MediumOutlinedTextField(
                                value = text,
                                onValueChange = onTextChange,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                singleLine = false,
                                colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent
                                ),
                                style = AppTheme.typography.bodyMedium.copy(color = AppTheme.colors.onSurface)
                            )
                        }

                        IconButton(
                            onClick = onSendClick,
                            modifier = Modifier
                                .background(
                                    color = AppTheme.colors.primaryContainer.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ).align(Alignment.CenterVertically).size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (isGenerating) Icons.Filled.Stop else Icons.Filled.ArrowUpward,
                                contentDescription = if (isGenerating) stringResource(Res.string.stop_generation) else stringResource(Res.string.send_message),
                                tint = AppTheme.colors.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Text(
                        text = "Aura weaves responses from probability. Seek truth with gentle scrutiny.",
                        style = AppTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = AppTheme.colors.outline.copy(alpha = 0.6f),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}