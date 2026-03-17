package org.onion.agent.utils

/**
 * A simple utility to inject metadata into PNG files.
 * This implementation focuses on adding tEXt chunks which are standard for PNG metadata.
 * Stable Diffusion typically uses a "parameters" key in a tEXt chunk.
 * 
 * Implemented using pure Kotlin ByteArray manipulation to avoid extra dependencies.
 */
object PngMetadata {

    private const val PNG_SIGNATURE_SIZE = 8
    private val PNG_SIGNATURE = byteArrayOf(137.toByte(), 80, 78, 71, 13, 10, 26, 10)
    private const val IHDR_CHUNK_TYPE = "IHDR"
    private const val IEND_CHUNK_TYPE = "IEND"
    private const val TEXT_CHUNK_TYPE = "tEXt"

    /**
     * Injects metadata into a PNG image.
     * returns the original image if it's not a valid PNG or if an error occurs.
     */
    fun inject(imageBytes: ByteArray, metadata: Map<String, String>): ByteArray {
        if (!isValidPng(imageBytes)) {
            println("PngMetadata: Invalid PNG signature, returning original bytes.")
            return imageBytes
        }
        
        if (metadata.isEmpty()) return imageBytes

        try {
            val outputStream = ByteArrayOutputStream()
            val inputStream = ByteArrayInputStream(imageBytes)
            
            // 1. Write Signature
            val signature = inputStream.readNBytes(PNG_SIGNATURE_SIZE)
            outputStream.write(signature)
            
            // 2. Read chunks
            while (inputStream.available() > 0) {
                // Read Length (4 bytes)
                val lengthBytes = inputStream.readNBytes(4)
                if (lengthBytes.size < 4) break
                val length = readInt(lengthBytes)
                
                // Read Type (4 bytes)
                val typeBytes = inputStream.readNBytes(4)
                val type = typeBytes.decodeToString()
                
                // Read Data
                val data = inputStream.readNBytes(length)
                
                // Read CRC (4 bytes)
                val crcBytes = inputStream.readNBytes(4)
                
                // Write current chunk to output
                outputStream.write(lengthBytes)
                outputStream.write(typeBytes)
                outputStream.write(data)
                outputStream.write(crcBytes)
                
                // Insert metadata after IHDR
                if (type == IHDR_CHUNK_TYPE) {
                    metadata.forEach { (key, value) ->
                        if (value.isNotBlank()) {
                            val chunk = createTextChunk(key, value)
                            outputStream.write(chunk)
                        }
                    }
                }
                
                if (type == IEND_CHUNK_TYPE) {
                    break
                }
            }
            
            return outputStream.toByteArray()
        } catch (e: Exception) {
            println("PngMetadata: Error injecting metadata: ${e.message}")
            e.printStackTrace()
            return imageBytes
        }
    }

    private fun isValidPng(bytes: ByteArray): Boolean {
        if (bytes.size < PNG_SIGNATURE_SIZE) return false
        for (i in PNG_SIGNATURE.indices) {
            if (bytes[i] != PNG_SIGNATURE[i]) return false
        }
        return true
    }

    private fun createTextChunk(key: String, value: String): ByteArray {
        val keywordBytes = key.encodeToByteArray()
        // Truncate keyword if too long (max 79 bytes)
        val safeKeywordBytes = if (keywordBytes.size > 79) keywordBytes.copyOfRange(0, 79) else keywordBytes
        
        val textBytes = value.encodeToByteArray() // Latin1 ideally, but UTF-8 is common practice now
        
        val dataSize = safeKeywordBytes.size + 1 + textBytes.size
        
        val output = ByteArrayOutputStream()
        
        // Length
        output.writeInt(dataSize)
        
        // Type
        val typeBytes = TEXT_CHUNK_TYPE.encodeToByteArray()
        output.write(typeBytes)
        
        // Data for CRC calculation
        val crcInput = ByteArrayOutputStream()
        crcInput.write(typeBytes)
        
        // Keyword
        output.write(safeKeywordBytes)
        crcInput.write(safeKeywordBytes)
        
        // Null separator
        output.write(0)
        crcInput.write(0)
        
        // Text
        output.write(textBytes)
        crcInput.write(textBytes)
        
        // CRC
        val crc = crc32(crcInput.toByteArray())
        output.writeInt(crc)
        
        return output.toByteArray()
    }

    // --- Helper Classes/Functions for Pure Kotlin Implementation ---
    
    private fun readInt(bytes: ByteArray): Int {
        return ((bytes[0].toInt() and 0xFF) shl 24) or
               ((bytes[1].toInt() and 0xFF) shl 16) or
               ((bytes[2].toInt() and 0xFF) shl 8) or
               (bytes[3].toInt() and 0xFF)
    }

    private class ByteArrayInputStream(private val buffer: ByteArray) {
        private var position = 0
        
        fun available(): Int = buffer.size - position
        
        fun readNBytes(n: Int): ByteArray {
            if (position + n > buffer.size) return ByteArray(0)
            val result = buffer.copyOfRange(position, position + n)
            position += n
            return result
        }
    }

    private class ByteArrayOutputStream {
        private var buffer = ByteArray(256)
        private var count = 0
        
        fun write(b: Int) {
            ensureCapacity(count + 1)
            buffer[count] = b.toByte()
            count += 1
        }
        
        fun write(b: ByteArray) {
            ensureCapacity(count + b.size)
            b.copyInto(buffer, count, 0, b.size)
            count += b.size
        }
        
        fun writeInt(v: Int) {
            ensureCapacity(count + 4)
            buffer[count] = (v ushr 24).toByte()
            buffer[count + 1] = (v ushr 16).toByte()
            buffer[count + 2] = (v ushr 8).toByte()
            buffer[count + 3] = v.toByte()
            count += 4
        }
        
        private fun ensureCapacity(minCapacity: Int) {
            if (minCapacity - buffer.size > 0) {
                var newCapacity = buffer.size * 2
                if (newCapacity < minCapacity) newCapacity = minCapacity
                buffer = buffer.copyOf(newCapacity)
            }
        }
        
        fun toByteArray(): ByteArray {
            return buffer.copyOf(count)
        }
    }

    // CRC-32 implementation
    private fun crc32(data: ByteArray): Int {
        var crc = -1 // 0xFFFFFFFF
        for (b in data) {
            val index = (crc xor b.toInt()) and 0xFF
            crc = (crc ushr 8) xor crcTable[index]
        }
        return crc.inv() // xor 0xFFFFFFFF
    }

    private val crcTable: IntArray by lazy {
        IntArray(256).apply {
            for (n in 0 until 256) {
                var c = n
                for (k in 0 until 8) {
                    c = if ((c and 1) != 0) {
                        -0x12477ce0 // 0xEDB88320
                    } else {
                        c ushr 1
                    }
                    c = c xor (if ((c and 1) != 0) 0 else 0) // dummy xor to preserve logic structure
                }
                this[n] = c
            }
        }
    }
}
