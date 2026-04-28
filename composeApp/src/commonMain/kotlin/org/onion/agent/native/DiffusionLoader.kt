package org.onion.agent.native

expect class DiffusionLoader(){
    suspend fun getModelFilePath():String

    // ========================================================================================
    //                              LiteRT LM (Text Generation) Methods
    // ========================================================================================
    
    fun loadLmEngine(
        modelPath: String,
        backend: String,
        visionBackend: String,
        audioBackend: String,
        maxNumTokens: Int,
        maxNumImages: Int,
        cacheDir: String,
        enableBenchmark: Boolean,
        enableSpeculativeDecoding: Boolean?,
        mainNpuNativeLibraryDir: String,
        visionNpuNativeLibraryDir: String,
        audioNpuNativeLibraryDir: String,
        mainBackendNumThreads: Int,
        audioBackendNumThreads: Int
    ): Long

    fun createLmConversation(
        enginePointer: Long,
        messageJsonString: String,
        toolsDescriptionJsonString: String,
        channelsJsonString: String?,
        extraContextJsonString: String,
        enableConversationConstrainedDecoding: Boolean
    ): Long

    fun sendLmMessage(
        conversationPointer: Long,
        messageJsonString: String,
        extraContextJsonString: String
    ): String

    fun sendLmMessageAsync(
        conversationPointer: Long,
        messageJsonString: String,
        extraContextJsonString: String,
        onMessage: (String) -> Unit,
        onDone: () -> Unit,
        onError: (Int, String) -> Unit
    )

    fun cancelLmConversation(conversationPointer: Long)
    fun deleteLmConversation(conversationPointer: Long)
    fun deleteLmEngine(enginePointer: Long)
}