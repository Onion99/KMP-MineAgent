package org.onion.agent.native

expect class DiffusionLoader(){
    suspend fun getModelFilePath():String
    fun loadModel(
        modelPath: String,
        vaePath: String = "",
        llmPath: String = "",
        clipLPath: String = "",
        clipGPath: String = "",
        t5xxlPath: String = "",
        offloadToCpu: Boolean = false,
        keepClipOnCpu: Boolean = false,
        keepVaeOnCpu: Boolean = false,
        diffusionFlashAttn: Boolean = false,
        enableMmap: Boolean = false,
        diffusionConvDirect: Boolean = false,
        wtype: Int = -1,
        flowShift: Float = Float.POSITIVE_INFINITY
    )
    fun txt2Img(
        prompt: String, negative: String,
        width: Int, height: Int,
        steps: Int, cfg: Float, seed: Long,
        sampleMethod: Int = -1,
        loraPaths: Array<String>? = null,
        loraStrengths: FloatArray? = null
    ): ByteArray?

    fun videoGen(
        prompt: String, negative: String,
        width: Int, height: Int,
        videoFrames: Int, steps: Int,
        cfg: Float, seed: Long,
        sampleMethod: Int,
        loraPaths: Array<String>? = null,
        loraStrengths: FloatArray? = null
    ): List<ByteArray>?

    fun release()

    suspend fun saveImage(imageData: ByteArray, fileName: String, metadata: Map<String, String>? = null): Boolean

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