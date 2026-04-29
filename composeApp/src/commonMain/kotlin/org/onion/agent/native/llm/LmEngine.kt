package org.onion.agent.native.llm

import com.google.ai.edge.litertlm.LiteRtLmJni
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.buildJsonArray

class LmEngine(
    val liteRtLmJni: LiteRtLmJni,
    val modelPath: String,
    val backend: String = "cpu",
    val visionBackend: String = "",
    val audioBackend: String = "",
    val maxNumTokens: Int = -1,
    val maxNumImages: Int = -1,
    val cacheDir: String = "",
    val mainBackendNumThreads: Int = 4,
    val audioBackendNumThreads: Int = 4,
    val enableBenchmark: Boolean = false,
    val enableSpeculativeDecoding: Boolean? = null,
    val mainNpuNativeLibraryDir: String = "",
    val visionNpuNativeLibraryDir: String = "",
    val audioNpuNativeLibraryDir: String = ""
) : AutoCloseable {

    private val mutex = Mutex()
    private var handle: Long? = null

    fun isInitialized(): Boolean = handle != null

    suspend fun initialize() {
        mutex.withLock {
            check(!isInitialized()) { "Engine is already initialized." }
            handle = liteRtLmJni.loadLmEngine(
                modelPath = modelPath,
                backend = backend,
                visionBackend = visionBackend,
                audioBackend = audioBackend,
                maxNumTokens = maxNumTokens,
                maxNumImages = maxNumImages,
                cacheDir = cacheDir,
                enableBenchmark = enableBenchmark,
                enableSpeculativeDecoding = enableSpeculativeDecoding,
                mainNpuNativeLibraryDir = mainNpuNativeLibraryDir,
                visionNpuNativeLibraryDir = visionNpuNativeLibraryDir,
                audioNpuNativeLibraryDir = audioNpuNativeLibraryDir,
                mainBackendNumThreads = mainBackendNumThreads,
                audioBackendNumThreads = audioBackendNumThreads
            )
        }
    }

    override fun close() {
        handle?.let {
            liteRtLmJni.deleteLmEngine(it)
            handle = null
        }
    }

    suspend fun createConversation(
        systemInstruction: String? = null,
        initialMessages: List<Message> = emptyList(),
        toolsDescriptionJsonString: String = "",
        enableConversationConstrainedDecoding: Boolean = false
    ): LmConversation {
        mutex.withLock {
            checkInitialized()
            val messageJsonString = if (systemInstruction != null || initialMessages.isNotEmpty()) {
                val messagesJson = buildJsonArray {
                    systemInstruction?.let {
                        add(Message.system(it).toJson())
                    }
                    initialMessages.forEach {
                        add(it.toJson())
                    }
                }
                messagesJson.toString()
            } else ""

            val ptr = liteRtLmJni.createLmConversation(
                enginePointer = handle!!,
                messageJsonString = messageJsonString,
                toolsDescriptionJsonString = toolsDescriptionJsonString,
                channelsJsonString = null,
                extraContextJsonString = "{}",
                enableConversationConstrainedDecoding = enableConversationConstrainedDecoding
            )
            return LmConversation(liteRtLmJni, ptr)
        }
    }

    private fun checkInitialized() {
        check(isInitialized()) { "Engine is not initialized." }
    }
}
