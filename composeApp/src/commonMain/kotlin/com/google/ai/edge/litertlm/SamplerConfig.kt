package com.google.ai.edge.litertlm

data class SamplerConfig(
    val topK: Int = 40,
    val topP: Double = 0.9,
    val temperature: Double = 0.8,
    val seed: Int = 0
)
