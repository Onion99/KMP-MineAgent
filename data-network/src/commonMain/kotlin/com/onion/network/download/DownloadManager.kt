package com.onion.network.download

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface DownloadManager {
    val tasksFlow: StateFlow<List<DownloadTask>>

    /**
     * Submits a new download task.
     * Returns the taskId.
     */
    fun download(url: String, filePath: String): String

    /**
     * Pauses the active download task.
     */
    fun pause(id: String)

    /**
     * Resumes the paused download task.
     */
    fun resume(id: String)

    /**
     * Cancels the active download task and resets its downloadedBytes.
     */
    fun cancel(id: String)

    /**
     * Removes the task from the manager. Optionally deletes the downloaded file.
     */
    fun remove(id: String, deleteFile: Boolean = false)

    /**
     * Gets the download task state by id.
     */
    fun getTask(id: String): DownloadTask?

    /**
     * Gets a Flow of a single download task by id.
     */
    fun getTaskFlow(id: String): Flow<DownloadTask?>
}
