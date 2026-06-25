package com.onion.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class ChatRole {
    @SerialName("system")
    SYSTEM,

    @SerialName("user")
    USER,

    @SerialName("assistant")
    ASSISTANT,

    @SerialName("tool")
    TOOL
}

@Serializable
data class PersistentToolCall(
    val name: String,
    val arguments: String,
    val createdAtMillis: Long
)

@Serializable
data class PersistentToolResponse(
    val name: String,
    val response: String,
    val createdAtMillis: Long
)
