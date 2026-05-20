package com.onion.network.http

import java.io.File
import java.io.RandomAccessFile

actual class PlatformFileWriter actual constructor(filePath: String) {
    private val file = File(filePath)
    private var raf: RandomAccessFile? = null

    init {
        file.parentFile?.mkdirs()
        raf = RandomAccessFile(file, "rw")
    }

    actual fun write(bytes: ByteArray, offset: Int, length: Int) {
        raf?.write(bytes, offset, length)
    }

    actual fun seek(position: Long) {
        raf?.seek(position)
    }

    actual fun close() {
        raf?.close()
        raf = null
    }
}

actual object PlatformFileUtil {
    actual fun getFileSize(filePath: String): Long {
        val file = File(filePath)
        return if (file.exists() && file.isFile) file.length() else 0L
    }

    actual fun deleteFile(filePath: String): Boolean {
        val file = File(filePath)
        return if (file.exists()) file.delete() else false
    }
}
