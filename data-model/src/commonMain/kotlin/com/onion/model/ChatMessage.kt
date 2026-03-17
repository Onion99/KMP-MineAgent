package com.onion.model

import kotlin.random.Random

data class ChatMessage(
        val message: String,
        val isUser: Boolean,
        val image: ByteArray? = null,
        val videoFrames: List<ByteArray>? = null,
        val metadata: Map<String, String>? = null,
        val id: Long = Random.nextLong()
) {
        override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other == null || this::class != other::class) return false

                other as ChatMessage

                if (id != other.id) return false

                return true
        }

        override fun hashCode(): Int {
                return id.hashCode()
        }
}