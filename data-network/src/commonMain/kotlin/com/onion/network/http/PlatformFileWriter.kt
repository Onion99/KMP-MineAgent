package com.onion.network.http

expect class PlatformFileWriter(filePath: String) {
    fun write(bytes: ByteArray, offset: Int, length: Int)
    fun seek(position: Long)
    fun close()
}

expect object PlatformFileUtil {
    fun getFileSize(filePath: String): Long
    fun deleteFile(filePath: String): Boolean
}
