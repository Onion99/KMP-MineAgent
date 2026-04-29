package org.onion.agent.native

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import org.onion.agent.utils.NativeLibraryLoader

actual class LLMLoader actual constructor() {

    init {
        NativeLibraryLoader.loadFromResources("GemmaModelConstraintProvider")
        NativeLibraryLoader.loadFromResources("litertlm_jni")
    }
    private var nativePtr = 0L

    actual suspend fun getModelFilePath(): String {
        return FileKit.openFilePicker()?.file?.absolutePath ?: ""
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