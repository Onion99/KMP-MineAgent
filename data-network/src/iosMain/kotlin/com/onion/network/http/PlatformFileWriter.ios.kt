package com.onion.network.http

import kotlinx.cinterop.*
import platform.posix.*

actual class PlatformFileWriter actual constructor(filePath: String) {
    private var filePtr: CPointer<FILE>? = null

    init {
        // Create the file if it doesn't exist by opening in append-binary mode
        val testOpen = fopen(filePath, "ab")
        if (testOpen != null) {
            fclose(testOpen)
        }
        filePtr = fopen(filePath, "r+b")
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun write(bytes: ByteArray, offset: Int, length: Int) {
        if (length <= 0) return
        val ptr = filePtr ?: return
        bytes.usePinned { pinned ->
            val address = pinned.addressOf(offset)
            fwrite(address, 1.convert(), length.convert(), ptr)
        }
    }

    actual fun seek(position: Long) {
        val ptr = filePtr ?: return
        fseek(ptr, position.convert(), SEEK_SET)
    }

    actual fun close() {
        val ptr = filePtr ?: return
        fclose(ptr)
        filePtr = null
    }
}

actual object PlatformFileUtil {
    @OptIn(ExperimentalForeignApi::class)
    actual fun getFileSize(filePath: String): Long {
        val filePtr = fopen(filePath, "rb") ?: return 0L
        fseek(filePtr, 0.convert(), SEEK_END)
        val size = ftell(filePtr)
        fclose(filePtr)
        return size.toLong()
    }

    actual fun deleteFile(filePath: String): Boolean {
        return remove(filePath) == 0
    }
}
