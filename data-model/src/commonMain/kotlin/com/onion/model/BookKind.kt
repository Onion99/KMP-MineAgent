package com.onion.model

import kotlinx.serialization.Serializable

@Serializable
data class BookKind(
    val title: String = "",
    val url: String? = null,
)