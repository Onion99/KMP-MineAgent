package com.google.ai.edge.litertlm

import androidx.core.net.toUri
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.context
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.name
import java.io.File
import java.io.FileOutputStream

internal actual object LiteRtLmJni {

    init {
        System.loadLibrary("GemmaModelConstraintProvider")
        System.loadLibrary("litertlm_jni")
    }

    actual suspend fun getModelFilePath(): String {
        val androidFile = FileKit.openFilePicker(type = FileKitType.File(listOf(
            "litertlm"
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
        return nativeCreateEngine(modelPath, backend, visionBackend, audioBackend, maxNumTokens, maxNumImages, cacheDir, enableBenchmark, enableSpeculativeDecoding, mainNpuNativeLibraryDir, visionNpuNativeLibraryDir, audioNpuNativeLibraryDir, mainBackendNumThreads, audioBackendNumThreads)
    }

    actual fun createLmConversation(
        enginePointer: Long, samplerConfig: Any?, messageJsonString: String, toolsDescriptionJsonString: String,
        channelsJsonString: String?, extraContextJsonString: String, enableConversationConstrainedDecoding: Boolean,
        filterChannelContentFromKvCache: Boolean, overwritePromptTemplate: String?
    ): Long {
        return nativeCreateConversation(enginePointer, samplerConfig, messageJsonString, toolsDescriptionJsonString, channelsJsonString, extraContextJsonString, enableConversationConstrainedDecoding, filterChannelContentFromKvCache, overwritePromptTemplate)
    }

    actual fun sendLmMessage(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String
    ): String {
        return nativeSendMessage(conversationPointer, messageJsonString, extraContextJsonString, null)
    }

    interface LmMessageCallback {
        fun onMessage(messageJsonString: String)
        fun onDone()
        fun onError(statusCode: Int, message: String)
    }

    actual fun sendLmMessageAsync(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String,
        onMessage: (String) -> Unit, onDone: () -> Unit, onError: (Int, String) -> Unit,
        visualTokenBudget: Int?
    ) {
        nativeSendMessageAsync(conversationPointer, messageJsonString, extraContextJsonString, object : LmMessageCallback {
            override fun onMessage(messageJsonString: String) = onMessage(messageJsonString)
            override fun onDone() = onDone()
            override fun onError(statusCode: Int, message: String) = onError(statusCode, message)
        }, visualTokenBudget)
    }

    actual fun cancelLmConversation(conversationPointer: Long) {
        nativeConversationCancelProcess(conversationPointer)
    }

    actual fun deleteLmConversation(conversationPointer: Long) {
        nativeDeleteConversation(conversationPointer)
    }

    actual fun deleteLmEngine(enginePointer: Long) {
        nativeDeleteEngine(enginePointer)
    }

    private external fun nativeCreateEngine(
        modelPath: String, backend: String, visionBackend: String, audioBackend: String,
        maxNumTokens: Int, maxNumImages: Int, cacheDir: String, enableBenchmark: Boolean,
        enableSpeculativeDecoding: Boolean?, mainNpuNativeLibraryDir: String,
        visionNpuNativeLibraryDir: String, audioNpuNativeLibraryDir: String,
        mainBackendNumThreads: Int, audioBackendNumThreads: Int
    ): Long

    private external fun nativeCreateConversation(
        enginePointer: Long, samplerConfig: Any?, messageJsonString: String, toolsDescriptionJsonString: String,
        channelsJsonString: String?, extraContextJsonString: String, enableConversationConstrainedDecoding: Boolean,
        filterChannelContentFromKvCache: Boolean, overwritePromptTemplate: String?
    ): Long

    private external fun nativeSendMessage(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String,
        visualTokenBudget: Int?
    ): String

    private external fun nativeSendMessageAsync(
        conversationPointer: Long, messageJsonString: String, extraContextJsonString: String,
        callback: LmMessageCallback, visualTokenBudget: Int?
    )

    private external fun nativeConversationCancelProcess(conversationPointer: Long)
    private external fun nativeDeleteConversation(conversationPointer: Long)
    private external fun nativeDeleteEngine(enginePointer: Long)
}