package com.onion.model

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
data class ChatMessage(
        val message: String,
        val isUser: Boolean,
        val role: ChatRole = if (isUser) ChatRole.USER else ChatRole.ASSISTANT,
        val image: ByteArray? = null,
        val videoFrames: List<ByteArray>? = null,
        val metadata: Map<String, String>? = null,
        val toolCalls: List<PersistentToolCall> = emptyList(),
        val toolResponses: List<PersistentToolResponse> = emptyList(),
        val createdAtMillis: Long = Clock.System.now().toEpochMilliseconds(),
        val id: Long = Random.nextLong()
)
