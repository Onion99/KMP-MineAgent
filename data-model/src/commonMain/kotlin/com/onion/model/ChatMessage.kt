package com.onion.model

import kotlin.random.Random

data class ChatMessage(
        val message: String,
        val isUser: Boolean,
        val image: ByteArray? = null,
        val videoFrames: List<ByteArray>? = null,
        val metadata: Map<String, String>? = null,
        val id: Long = Random.nextLong()
)