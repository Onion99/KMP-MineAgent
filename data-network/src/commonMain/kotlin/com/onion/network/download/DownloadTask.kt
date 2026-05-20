package com.onion.network.download

import kotlinx.serialization.Serializable

@Serializable
enum class DownloadStatus {
    IDLE,
    QUEUED,
    DOWNLOADING,
    PAUSED,
    COMPLETED,
    FAILED,
    CANCELLED
}

@Serializable
data class DownloadTask(
    val id: String,
    val url: String,
    val filePath: String,
    val status: DownloadStatus = DownloadStatus.IDLE,
    val progress: Float = 0f,
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = -1L,
    val speedBytesPerSecond: Long = 0L,
    val etaSeconds: Long = -1L,
    val errorMessage: String? = null
)
