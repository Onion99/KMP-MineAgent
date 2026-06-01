package org.onion.agent.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.onion.model.ChatMessage
import com.onion.model.LoraConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.google.ai.edge.litertlm.LiteRtLmJni
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.flow.catch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.math.roundToInt
import org.onion.agent.getPlatform
import org.onion.agent.native.llm.AgentTools
import org.onion.agent.native.llm.ToolCall
import org.onion.agent.native.llm.ToolResponse

class ChatViewModel  : ViewModel() {

    /** Format milliseconds into human-readable duration: "0.85s" / "12.3s" / "2m 15s" */
    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000.0
        return when {
            totalSeconds < 1.0 -> {
                val hundredths = (totalSeconds * 100).roundToInt()
                "${hundredths / 100}.${(hundredths % 100).toString().padStart(2, '0')}s"
            }
            totalSeconds < 60.0 -> {
                val tenths = (totalSeconds * 10).roundToInt()
                "${tenths / 10}.${tenths % 10}s"
            }
            else -> {
                val minutes = (totalSeconds / 60).toInt()
                val seconds = (totalSeconds % 60).toInt()
                "${minutes}m ${seconds}s"
            }
        }
    }


    var diffusionModelPath = mutableStateOf("")
    var vaePath = mutableStateOf("")
    var llmPath = mutableStateOf("")
    var clipLPath = mutableStateOf("")
    var clipGPath = mutableStateOf("")
    var t5xxlPath = mutableStateOf("")
    private var isInitializing = false
    private var activeModelPath: String? = null
    // 0 default,1 loading,2 loading completely
    var loadingModelState = MutableStateFlow(0)
    var isDiffusionModelLoading = mutableStateOf(false)
    var isVaeModelLoading = mutableStateOf(false)
    var isLlmModelLoading = mutableStateOf(false)
    var isClipLModelLoading = mutableStateOf(false)
    var isClipGModelLoading = mutableStateOf(false)
    var isT5xxlModelLoading = mutableStateOf(false)
    
    // ========================================================================================
    //                              Image Generation Settings
    // ========================================================================================
    /** Image width - options: 128, 256, 512, 768, 1024 */
    var imageWidth = mutableStateOf(512)
    
    /** Image height - options: 128, 256, 512, 768, 1024 */
    var imageHeight = mutableStateOf(512)
    
    /** Batch count - number of images to generate */
    var batchCount = mutableStateOf(1)
    
    /** Steps for generation - range: 1-50 */
    var generationSteps = mutableStateOf(5)
    
    /** CFG Scale - range: 1.0-15.0 */
    var cfgScale = mutableStateOf(2f)

    /** Flash Attention - optimize memory usage */
    var diffusionFlashAttn = mutableStateOf(false)

    /** Quantization Type - -1: Auto/Default, 0: F32, 1: F16, 2: Q4_0, etc. */
    var wtype = mutableStateOf(-1)

    /** Offload to CPU - offload model computations to CPU */
    var offloadToCpu = mutableStateOf(getPlatform().isIOS)

    /** Keep CLIP on CPU - keep CLIP model on CPU (enabled by default on macOS and iOS) */
    var keepClipOnCpu = mutableStateOf(getPlatform().isMacOS || getPlatform().isIOS)

    /** Keep VAE on CPU - keep VAE decoder on CPU */
    var keepVaeOnCpu = mutableStateOf(false)
    
    /** Enable MMAP - memory map the model weights */
    var enableMmap = mutableStateOf(false)


    /** Direct Convolution - optimize convolution in diffusion model */
    var diffusionConvDirect = mutableStateOf(false)

    /** Sampling Method - default is -1 (Auto/Euler) */
    var sampleMethod = mutableStateOf(-1)

    // ========================================================================================
    //                              Video Generation Settings
    // ========================================================================================
    /** Video frames - number of frames to generate */
    var videoFrames = mutableStateOf(33)

    /** Flow Shift - controls temporal flow for video generation models (e.g. Wan2.1) */
    var flowShift = mutableStateOf(3.0f)

    // ========================================================================================
    //                              LLM Settings (Gemma 4 LiteRT)
    // ========================================================================================
    var lmBackend = mutableStateOf("GPU")//NPU,CPU,GPU
    var lmVisionBackend = mutableStateOf("")
    var lmAudioBackend = mutableStateOf("")
    var lmMaxNumTokens = mutableStateOf(2048)
    var lmMaxNumImages = mutableStateOf(-1)
    var lmMainBackendNumThreads = mutableStateOf(2)
    var lmAudioBackendNumThreads = mutableStateOf(-1)

    // Model Parameter Adjustments
    var temperature = mutableStateOf(0.7f)
    var topP = mutableStateOf(0.9f)
    var topK = mutableStateOf(40)
    var enableThinking = mutableStateOf(false)
    var enableSpeculativeDecoding = mutableStateOf(false)
    var systemPrompt = mutableStateOf("You are Aura, an analytical and precise local intelligence. Prioritize factual accuracy and concise formatting. Maintain a calm, neutral tone.")
    var systemContextShift = mutableStateOf(true)

    private var lmEngine: org.onion.agent.native.llm.LmEngine? = null
    private var lmConversation: org.onion.agent.native.llm.LmConversation? = null
    private var activeEnableSpeculativeDecoding: Boolean? = null
    private var activeMaxNumTokens: Int? = null
    private val agentTools = AgentTools()

    // ========================================================================================
    //                              LoRA Settings
    // ========================================================================================
    val loraList = mutableStateListOf<LoraConfig>()

    fun addLora(path: String) {
        // Prevent duplicates
        if (loraList.any { it.path == path }) return
        
        // Extract filename for name
        val name = path.substringAfterLast('/').substringAfterLast('\\')
        loraList.add(LoraConfig(path = path, name = name))
    }

    fun removeLora(lora: LoraConfig) {
        loraList.remove(lora)
    }


    suspend fun selectLoraFile(): String {
        return LiteRtLmJni.getModelFilePath()
    }

    suspend fun selectDiffusionModelFile(): String{
        isDiffusionModelLoading.value = true
        val diffusionModelPath = LiteRtLmJni.getModelFilePath()
        this.diffusionModelPath.value = diffusionModelPath
        isDiffusionModelLoading.value = false
        return diffusionModelPath
    }

    suspend fun selectVaeFile(): String{
        isVaeModelLoading.value = true
        val path = LiteRtLmJni.getModelFilePath()
        vaePath.value = path
        isVaeModelLoading.value = false
        return path
    }

    suspend fun selectLlmFile(): String{
        isLlmModelLoading.value = true
        val path = LiteRtLmJni.getModelFilePath()
        llmPath.value = path
        isLlmModelLoading.value = false
        return path
    }

    suspend fun selectClipLFile(): String{
        isClipLModelLoading.value = true
        val path = LiteRtLmJni.getModelFilePath()
        clipLPath.value = path
        isClipLModelLoading.value = false
        return path
    }

    suspend fun selectClipGFile(): String{
        isClipGModelLoading.value = true
        val path = LiteRtLmJni.getModelFilePath()
        clipGPath.value = path
        isClipGModelLoading.value = false
        return path
    }

    suspend fun selectT5xxlFile(): String{
        isT5xxlModelLoading.value = true
        val path = LiteRtLmJni.getModelFilePath()
        t5xxlPath.value = path
        isT5xxlModelLoading.value = false
        return path
    }

    fun initLLM() {
        if (isInitializing) return
        if (lmEngine != null && llmPath.value == activeModelPath) {
            return
        }
        isInitializing = true
        viewModelScope.launch(Dispatchers.Default) {
            loadingModelState.emit(1)
            try {
                if (isGenerating.value) {
                    stopGeneration()
                }
                try {
                    lmConversation?.close()
                    lmConversation = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    lmEngine?.close()
                    lmEngine = null
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                println("=== Model Path ===")
                println("Model Path: ${diffusionModelPath.value}")
                println("VAE Path: ${vaePath.value}")
                println("LLM Path: ${llmPath.value}")
                println("CLIP-L Path: ${clipLPath.value}")
                println("CLIP-G Path: ${clipGPath.value}")
                println("T5XXL Path: ${t5xxlPath.value}")
                println("cacheDir path is: ${FileKit.cacheDir.path}")
                isLlmModelLoading.value = true
                val currentLlmPath = llmPath.value
                lmEngine = org.onion.agent.native.llm.LmEngine(
                    modelPath = currentLlmPath,
                    backend = lmBackend.value,
                    visionBackend = lmVisionBackend.value,
                    audioBackend = lmAudioBackend.value,
                    maxNumTokens = lmMaxNumTokens.value,
                    maxNumImages = lmMaxNumImages.value,
                    cacheDir = FileKit.cacheDir.path ?: "",
                    enableBenchmark = false,
                    enableSpeculativeDecoding = enableSpeculativeDecoding.value,
                    mainNpuNativeLibraryDir = "",
                    visionNpuNativeLibraryDir = "",
                    audioNpuNativeLibraryDir = "",
                    mainBackendNumThreads = lmMainBackendNumThreads.value,
                    audioBackendNumThreads = lmAudioBackendNumThreads.value
                )
                lmEngine?.initialize()

                lmConversation = lmEngine?.createConversation(
                    systemInstruction = systemPrompt.value,
                    toolsDescriptionJsonString = agentTools.getToolsDescriptionJson(),
                    samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                        temperature = temperature.value.toDouble(),
                        topP = topP.value.toDouble(),
                        topK = topK.value
                    )
                )
                activeModelPath = currentLlmPath
                activeEnableSpeculativeDecoding = enableSpeculativeDecoding.value
                activeMaxNumTokens = lmMaxNumTokens.value
            } catch (e: Exception) {
                e.printStackTrace()
                activeModelPath = null
            } finally {
                isInitializing = false
                isLlmModelLoading.value = false
            }
            loadingModelState.emit(2)
        }
    }

    fun applyConversationSettings() {
        val currentLlmPath = llmPath.value
        if (currentLlmPath.isBlank()) return
        viewModelScope.launch(Dispatchers.Default) {
            isLlmModelLoading.value = true
            loadingModelState.emit(1)
            try {
                if (isGenerating.value) {
                    stopGeneration()
                }
                
                val needsEngineReinit = lmEngine == null ||
                        activeModelPath != currentLlmPath ||
                        activeEnableSpeculativeDecoding != enableSpeculativeDecoding.value ||
                        activeMaxNumTokens != lmMaxNumTokens.value
                
                if (needsEngineReinit) {
                    try {
                        lmConversation?.close()
                        lmConversation = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        lmEngine?.close()
                        lmEngine = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    
                    lmEngine = org.onion.agent.native.llm.LmEngine(
                        modelPath = currentLlmPath,
                        backend = lmBackend.value,
                        visionBackend = lmVisionBackend.value,
                        audioBackend = lmAudioBackend.value,
                        maxNumTokens = lmMaxNumTokens.value,
                        maxNumImages = lmMaxNumImages.value,
                        cacheDir = FileKit.cacheDir.path ?: "",
                        enableBenchmark = false,
                        enableSpeculativeDecoding = enableSpeculativeDecoding.value,
                        mainNpuNativeLibraryDir = "",
                        visionNpuNativeLibraryDir = "",
                        audioNpuNativeLibraryDir = "",
                        mainBackendNumThreads = lmMainBackendNumThreads.value,
                        audioBackendNumThreads = lmAudioBackendNumThreads.value
                    )
                    lmEngine?.initialize()
                    activeModelPath = currentLlmPath
                    activeEnableSpeculativeDecoding = enableSpeculativeDecoding.value
                    activeMaxNumTokens = lmMaxNumTokens.value
                } else {
                    try {
                        lmConversation?.close()
                        lmConversation = null
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                
                val engine = lmEngine
                if (engine != null) {
                    lmConversation = engine.createConversation(
                        systemInstruction = systemPrompt.value,
                        toolsDescriptionJsonString = agentTools.getToolsDescriptionJson(),
                        samplerConfig = com.google.ai.edge.litertlm.SamplerConfig(
                            temperature = temperature.value.toDouble(),
                            topP = topP.value.toDouble(),
                            topK = topK.value
                        )
                    )
                }
                _currentChatMessages.clear()
                _currentChatMessages.add(ChatMessage("System: Model parameters applied. Conversation restarted.", false))
            } catch (e: Exception) {
                e.printStackTrace()
                _currentChatMessages.clear()
                _currentChatMessages.add(ChatMessage("System Error: Failed to apply parameters. ${e.message}", false))
            } finally {
                isLlmModelLoading.value = false
                loadingModelState.emit(2)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            lmConversation?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            lmEngine?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private var responseGenerationJob: Job? = null
    private var isInferenceOn: Boolean = false
    private val defaultNegative = ""
    // ========================================================================================
    //                              Chat Message State
    // ========================================================================================

    /** Current active chat conversation messages */
    private val _currentChatMessages = mutableStateListOf<ChatMessage>()
    val currentChatMessages: SnapshotStateList<ChatMessage> = _currentChatMessages

    /** Flag indicating if response generation is in progress */
    val isGenerating = mutableStateOf(false)

    // region Message Handling & Generation
    // ========================================================================================
    //                          Public Message Methods
    // ========================================================================================
    fun sendMessage(message: String, isUser: Boolean = true) {
        viewModelScope.launch {
            if(isGenerating.value) stopGeneration()
            if(message.isBlank()) return@launch
            _currentChatMessages.add(ChatMessage(message, isUser))
            val meta = mapOf("is_generating" to "true")
            _currentChatMessages.add(ChatMessage("", false, metadata = meta))
            isGenerating.value = true
            getTextTalkerResponse(message, {}, {
                println(it.message)
            })
        }

    }

    fun reGenerateMessage(message: ChatMessage) {
        val prompt = message.metadata?.get("prompt") ?: return
        val negativePrompt = message.metadata?.get("negative_prompt") ?: ""
    }

    fun stopGeneration() {
        isGenerating.value = false
        if (lmConversation != null && llmPath.value.isNotBlank()) {
            lmConversation?.cancelProcess()
        }
        responseGenerationJob?.cancel()
        val lastIndex = _currentChatMessages.lastIndex
        if (lastIndex >= 0) {
            _currentChatMessages.removeAt(lastIndex)
        }
    }
    
    @OptIn(ExperimentalTime::class)
    fun getTextTalkerResponse(query: String, onCancelled: () -> Unit, onError: (Throwable) -> Unit) {
        if (lmConversation == null) {
            isGenerating.value = false
            isInferenceOn = false
            if (_currentChatMessages.isNotEmpty()) {
                val lastIdx = _currentChatMessages.lastIndex
                val meta = mapOf("is_generating" to "false")
                _currentChatMessages[lastIdx] = _currentChatMessages[lastIdx].copy(
                    message = "System: LM Engine not initialized or model not loaded yet. Please wait or check the model path.",
                    metadata = meta
                )
            }
            onError(IllegalStateException("LM Engine not initialized"))
            return
        }
        
        responseGenerationJob = viewModelScope.launch(Dispatchers.Default) {
            isInferenceOn = true
            val promptContent = query
            val startTime = Clock.System.now().toEpochMilliseconds()
            
            var generatedResult = ""
            var generatedThought = ""
            var currentMessage = org.onion.agent.native.llm.Message.user(promptContent)
            var keepGoing = true
            var recursionCount = 0
            
            while (keepGoing && recursionCount < 10) {
                var toolCallsReceived: List<ToolCall>? = null
                
                val extraContext = if (enableThinking.value) mapOf("enable_thinking" to "true") else emptyMap()
                lmConversation?.sendMessageAsync(currentMessage, extraContext)
                    ?.catch { e ->
                        isGenerating.value = false
                        isInferenceOn = false
                        keepGoing = false
                        if (e is CancellationException) {
                            onCancelled()
                        } else {
                            if (_currentChatMessages.isNotEmpty()) {
                                val lastIdx = _currentChatMessages.lastIndex
                                val meta = mapOf("is_generating" to "false")
                                _currentChatMessages[lastIdx] = _currentChatMessages[lastIdx].copy(
                                    message = "Error: ${e.message ?: "Unknown error"}",
                                    metadata = meta
                                )
                            }
                            onError(e)
                        }
                    }
                    ?.collect { responseMsg ->
                        if (responseMsg.toolCalls.isNotEmpty()) {
                            toolCallsReceived = responseMsg.toolCalls
                        }
                        
                        val thoughtChunk = responseMsg.channels["thought"] ?: ""
                        if (thoughtChunk.isNotEmpty()) {
                            generatedThought += thoughtChunk
                        }
                        val chunk = responseMsg.contents.toString()
                        if (chunk.isNotEmpty()) {
                            generatedResult += chunk
                        }
                        
                        val fullMessage = buildString {
                            if (generatedThought.isNotEmpty()) {
                                append("> *Thinking...*\n")
                                generatedThought.lineSequence().forEach { line ->
                                    append("> ").append(line).append("\n")
                                }
                                append("\n")
                            }
                            append(generatedResult)
                        }
                        
                        if (chunk.isNotEmpty() || thoughtChunk.isNotEmpty()) {
                            if (_currentChatMessages.isNotEmpty()) {
                                val lastIdx = _currentChatMessages.lastIndex
                                val msgToUpdate = _currentChatMessages[lastIdx]
                                val meta = mapOf("is_generating" to "true")
                                _currentChatMessages[lastIdx] = msgToUpdate.copy(message = fullMessage.trim(), metadata = meta)
                            }
                        }
                    }
                
                val toolCalls = toolCallsReceived
                if (toolCalls != null && toolCalls.isNotEmpty()) {
                    recursionCount++
                    val responses = mutableListOf<ToolResponse>()
                    
                    for (toolCall in toolCalls) {
                        println("ChatViewModel: Executing tool '${toolCall.name}' with args: ${toolCall.arguments}")
                        
                        val toolLog = "\n`[Running tool: ${toolCall.name}...]`\n"
                        generatedResult += toolLog
                        if (_currentChatMessages.isNotEmpty()) {
                            val lastIdx = _currentChatMessages.lastIndex
                            val msgToUpdate = _currentChatMessages[lastIdx]
                            val meta = mapOf("is_generating" to "true")
                            _currentChatMessages[lastIdx] = msgToUpdate.copy(message = generatedResult.trim(), metadata = meta)
                        }
                        
                        val resultStr = agentTools.execute(toolCall.name, toolCall.arguments)
                        
                        val toolDoneLog = "\n`[Tool '${toolCall.name}' completed]`\n"
                        generatedResult += toolDoneLog
                        if (_currentChatMessages.isNotEmpty()) {
                            val lastIdx = _currentChatMessages.lastIndex
                            val msgToUpdate = _currentChatMessages[lastIdx]
                            val meta = mapOf("is_generating" to "true")
                            _currentChatMessages[lastIdx] = msgToUpdate.copy(message = generatedResult.trim(), metadata = meta)
                        }
                        
                        responses.add(ToolResponse(toolCall.name, resultStr))
                    }
                    
                    currentMessage = org.onion.agent.native.llm.Message.tool(responses)
                } else {
                    keepGoing = false
                }
            }
            
            println("ChatViewModel: collect finished!")
            val generationDuration = Clock.System.now().toEpochMilliseconds() - startTime
            if (_currentChatMessages.isNotEmpty()) {
                val lastIdx = _currentChatMessages.lastIndex
                val meta = mapOf(
                    "prompt" to promptContent,
                    "time_taken" to formatDuration(generationDuration),
                    "is_generating" to "false"
                )
                _currentChatMessages[lastIdx] = _currentChatMessages[lastIdx].copy(metadata = meta)
            }
            
            isGenerating.value = false
            isInferenceOn = false
        }
    }
}