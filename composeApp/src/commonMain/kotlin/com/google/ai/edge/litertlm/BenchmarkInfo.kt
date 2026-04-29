package com.google.ai.edge.litertlm

data class BenchmarkInfo(
    val totalInitTimeMs: Double,
    val timeToFirstToken: Double,
    val lastPrefillTokenCount: Int,
    val lastDecodeTokenCount: Int,
    val lastPrefillTokensPerSecond: Double,
    val lastDecodeTokensPerSecond: Double
)
