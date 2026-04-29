package org.onion.agent.native

import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import java.io.File
import java.io.FileOutputStream

actual class LLMLoader actual constructor() {

    init {
        System.loadLibrary("litertlm_jni")
    }

    actual suspend fun getModelFilePath(): String {
        val androidFile = FileKit.openFilePicker(type = FileKitType.File(listOf(
            "safetensors","ckpt","pt","bin","gguf"
        )))
        androidFile ?: return ""
        val file = File(FileKit.context.filesDir, androidFile!!.name)
        if(file.exists()) return file.absolutePath
        FileKit.context.contentResolver.openInputStream((androidFile?.absolutePath() ?: return "").toUri()).use { inputStream ->
            FileOutputStream(File(FileKit.context.filesDir, androidFile.name)).use { outputStream ->
                inputStream?.copyTo(outputStream)
            }
        }
        return File(FileKit.context.filesDir, androidFile.name).absolutePath
    }


    // ========================================================================================
    //                              LiteRT LM API Implementations
    // ========================================================================================
    
    actual fun loadLmEngine(
        modelPath: String, backend: String, visionBackend: String, audioBackend: String,
        maxNumTokens: Int, maxNumImages: Int, cacheDir: String, enableBenchmark: Boolean,
        enableSpeculativeDecoding: Boolean?, mainNpuNativeLibraryDir: String,
        visionNpuNativeLibraryDir: String, audioNpuNativeLibraryDir: String,
        mainBackendNumThreads: Int, audioBackendNumThreads: Int
    ): Long {
        return nativeLoadLmEngine(modelPath, backend, visionBackend, audioBackend, maxNumTokens, maxNumImages, cacheDir, enableBenchmark, enableSpeculativeDecoding, mainNpuNativeLibraryDir, visionNpuNativeLibraryDir, audioNpuNativeLibraryDir, mainBackendNumThreads, audioBackendNumThreads)
    }

    actual fun createLmConversation(
        enginePointer: Long, messageJsonString: String, toolsDescriptionJsonString: String,
        channelsJsonString: String?, extraContextJsonString: String, enableConversationConstrainedDecoding: Boolean
    ): Long {
        return nativeCreateLmConversation(enginePointer, messageJsonString, toolsDescriptionJsonString, channelsJsonString, extraContextJsonString, enableConversationConstrainedDecoding)
    }

    actual fun sendLmMessage(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String
    ): String {
        return nativeSendLmMessage(conversationPointer, messageJsonString, extraContextJsonString)
    }

    interface LmMessageCallback {
        fun onMessage(messageJsonString: String)
        fun onDone()
        fun onError(statusCode: Int, message: String)
    }

    actual fun sendLmMessageAsync(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String,
        onMessage: (String) -> Unit, onDone: () -> Unit, onError: (Int, String) -> Unit
    ) {
        nativeSendLmMessageAsync(conversationPointer, messageJsonString, extraContextJsonString, object : LmMessageCallback {
            override fun onMessage(messageJsonString: String) = onMessage(messageJsonString)
            override fun onDone() = onDone()
            override fun onError(statusCode: Int, message: String) = onError(statusCode, message)
        })
    }

    actual fun cancelLmConversation(conversationPointer: Long) {
        nativeCancelLmConversation(conversationPointer)
    }

    actual fun deleteLmConversation(conversationPointer: Long) {
        nativeDeleteLmConversation(conversationPointer)
    }

    actual fun deleteLmEngine(enginePointer: Long) {
        nativeDeleteLmEngine(enginePointer)
    }

    private external fun nativeLoadLmEngine(
        modelPath: String, backend: String, visionBackend: String, audioBackend: String,
        maxNumTokens: Int, maxNumImages: Int, cacheDir: String, enableBenchmark: Boolean,
        enableSpeculativeDecoding: Boolean?, mainNpuNativeLibraryDir: String,
        visionNpuNativeLibraryDir: String, audioNpuNativeLibraryDir: String,
        mainBackendNumThreads: Int, audioBackendNumThreads: Int
    ): Long

    private external fun nativeCreateLmConversation(
        enginePointer: Long, messageJsonString: String, toolsDescriptionJsonString: String,
        channelsJsonString: String?, extraContextJsonString: String, enableConversationConstrainedDecoding: Boolean
    ): Long

    private external fun nativeSendLmMessage(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String
    ): String

    private external fun nativeSendLmMessageAsync(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String, callback: LmMessageCallback
    )

    private external fun nativeCancelLmConversation(conversationPointer: Long)
    private external fun nativeDeleteLmConversation(conversationPointer: Long)
    private external fun nativeDeleteLmEngine(enginePointer: Long)
}