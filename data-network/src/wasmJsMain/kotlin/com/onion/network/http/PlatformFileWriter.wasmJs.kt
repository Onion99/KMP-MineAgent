package com.onion.network.http

actual class PlatformFileWriter actual constructor(filePath: String) {
    actual fun write(bytes: ByteArray, offset: Int, length: Int) {
        throw UnsupportedOperationException("File writing is not supported on wasmJs")
    }

    actual fun seek(position: Long) {
        throw UnsupportedOperationException("File seeking is not supported on wasmJs")
    }

    actual fun close() {
        // No-op
    }
}

actual object PlatformFileUtil {
    actual fun getFileSize(filePath: String): Long {
        return 0L
    }

    actual fun deleteFile(filePath: String): Boolean {
        return false
    }
}
