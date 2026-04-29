package org.onion.agent.native.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.serialization.json.*
import org.onion.agent.native.LLMLoader
import kotlinx.coroutines.CancellationException

class LmConversation(
    private val LLMLoader: LLMLoader,
    private val handle: Long
) : AutoCloseable {

    private var isAlive = true

    fun sendMessageAsync(
        message: Message,
        extraContext: Map<String, String> = emptyMap()
    ): Flow<Message> = callbackFlow {
        checkIsAlive()
        
        val extraContextObj = buildJsonObject {
            for ((k, v) in extraContext) {
                put(k, v)
            }
        }

        LLMLoader.sendLmMessageAsync(
            conversationPointer = handle,
            messageJsonString = message.toJson().toString(),
            extraContextJsonString = extraContextObj.toString(),
            onMessage = { messageJsonString ->
                val messageJsonObject = Json.parseToJsonElement(messageJsonString).jsonObject
                
                if (messageJsonObject.containsKey("content") || messageJsonObject.containsKey("channels")) {
                    trySend(jsonToMessage(messageJsonObject))
                } else if (messageJsonObject.containsKey("tool_calls")) {
                    trySend(jsonToMessage(messageJsonObject))
                }
            },
            onDone = {
                close()
            },
            onError = { code, errorMsg ->
                if (code == 1) { // Cancelled
                    close(CancellationException(errorMsg))
                } else {
                    close(RuntimeException("Error $code: $errorMsg"))
                }
            }
        )
        
        awaitClose {
            // Wait for completion, process cancellation if needed via cancelProcess() explicitly handled externally
        }
    }

    fun cancelProcess() {
        checkIsAlive()
        LLMLoader.cancelLmConversation(handle)
    }

    override fun close() {
        if (isAlive) {
            isAlive = false
            LLMLoader.deleteLmConversation(handle)
        }
    }

    private fun checkIsAlive() {
        check(isAlive) { "Conversation is not alive." }
    }

    companion object {
        fun jsonToMessage(messageJsonObject: JsonObject): Message {
            val contentsList = mutableListOf<Content>()
            
            if (messageJsonObject.containsKey("content")) {
                val contentsJsonArray = messageJsonObject["content"]?.jsonArray
                contentsJsonArray?.forEach { contentElement ->
                    val contentObj = contentElement.jsonObject
                    val type = contentObj["type"]?.jsonPrimitive?.content
                    if (type == "text") {
                        val textStr = contentObj["text"]?.jsonPrimitive?.content ?: ""
                        contentsList.add(Content.Text(textStr))
                    }
                }
            }

            val toolCallsList = mutableListOf<ToolCall>()
            if (messageJsonObject.containsKey("tool_calls")) {
                val toolCallsJsonArray = messageJsonObject["tool_calls"]?.jsonArray
                toolCallsJsonArray?.forEach { toolCallElement ->
                    val toolCallObj = toolCallElement.jsonObject
                    if (toolCallObj.containsKey("function")) {
                        val functionObj = toolCallObj["function"]?.jsonObject
                        if (functionObj != null) {
                            val name = functionObj["name"]?.jsonPrimitive?.content ?: ""
                            val arguments = functionObj["arguments"]?.jsonObject ?: buildJsonObject {}
                            toolCallsList.add(ToolCall(name, arguments))
                        }
                    }
                }
            }

            val channelsMap = mutableMapOf<String, String>()
            if (messageJsonObject.containsKey("channels")) {
                val channelsObj = messageJsonObject["channels"]?.jsonObject
                channelsObj?.forEach { (key, element) ->
                    channelsMap[key] = element.jsonPrimitive.content
                }
            }

            return Message.model(Contents.of(contentsList), toolCallsList, channelsMap)
        }
    }
}
