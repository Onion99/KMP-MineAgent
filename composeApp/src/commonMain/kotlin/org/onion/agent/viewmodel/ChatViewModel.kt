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
    private var initModel = false
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
    var lmBackend = mutableStateOf("CPU")//NPU,CPU,GPU
    var lmVisionBackend = mutableStateOf("")
    var lmAudioBackend = mutableStateOf("")
    var lmMaxNumTokens = mutableStateOf(-1)
    var lmMaxNumImages = mutableStateOf(-1)
    var lmMainBackendNumThreads = mutableStateOf(-1)
    var lmAudioBackendNumThreads = mutableStateOf(-1)

    private var lmEngine: org.onion.agent.native.llm.LmEngine? = null
    private var lmConversation: org.onion.agent.native.llm.LmConversation? = null

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

    fun initLLM(){
        if(initModel) return
        initModel = true
        viewModelScope.launch(Dispatchers.Default) {
            loadingModelState.emit(1)
            println("=== Model Path ===")
            println("Model Path: ${diffusionModelPath.value}")
            println("VAE Path: ${vaePath.value}")
            println("LLM Path: ${llmPath.value}")
            println("CLIP-L Path: ${clipLPath.value}")
            println("CLIP-G Path: ${clipGPath.value}")
            println("T5XXL Path: ${t5xxlPath.value}")
            println("cacheDir path is: ${FileKit.cacheDir.path}")
            isLlmModelLoading.value = true
            try {
                lmEngine = org.onion.agent.native.llm.LmEngine(
                    modelPath = llmPath.value,
                    backend = lmBackend.value,
                    visionBackend = lmVisionBackend.value,
                    audioBackend = lmAudioBackend.value,
                    maxNumTokens = lmMaxNumTokens.value,
                    maxNumImages = lmMaxNumImages.value,
                    cacheDir = FileKit.cacheDir.path ?: "",
                    enableBenchmark = false,
                    enableSpeculativeDecoding = null,
                    mainNpuNativeLibraryDir = "",
                    visionNpuNativeLibraryDir = "",
                    audioNpuNativeLibraryDir = "",
                    mainBackendNumThreads = lmMainBackendNumThreads.value,
                    audioBackendNumThreads = lmAudioBackendNumThreads.value
                )
                lmEngine?.initialize()

                lmConversation = lmEngine?.createConversation(
                    systemInstruction = "You are a helpful assistant."
                )
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLlmModelLoading.value = false
            }
            loadingModelState.emit(2)
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
            
            lmConversation?.sendMessageAsync(org.onion.agent.native.llm.Message.user(promptContent))
                ?.catch { e ->
                    isGenerating.value = false
                    isInferenceOn = false
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
                    val chunk = responseMsg.contents.toString()
                    println("ChatViewModel: collect received chunk: '$chunk'")
                    generatedResult += chunk
                    println("ChatViewModel: generatedResult is now: '$generatedResult'")
                    if (_currentChatMessages.isNotEmpty()) {
                        val lastIdx = _currentChatMessages.lastIndex
                        val msgToUpdate = _currentChatMessages[lastIdx]
                        val meta = mapOf("is_generating" to "true")
                        _currentChatMessages[lastIdx] = msgToUpdate.copy(message = generatedResult.trim(), metadata = meta)
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