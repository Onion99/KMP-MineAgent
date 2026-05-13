package org.onion.agent.native.llm

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.serialization.json.*
import com.google.ai.edge.litertlm.LiteRtLmJni
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class LmConversation(
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

        LiteRtLmJni.sendLmMessageAsync(
            conversationPointer = handle,
            messageJsonString = message.toJson().toString(),
            extraContextJsonString = extraContextObj.toString(),
            onMessage = { messageJsonString ->
                println("LmConversation: onMessage received: $messageJsonString")
                try {
                    val messageJsonObject = Json.parseToJsonElement(messageJsonString).jsonObject
                    
                    if (messageJsonObject.containsKey("content") || messageJsonObject.containsKey("channels")) {
                        launch {
                            send(jsonToMessage(messageJsonObject))
                            println("LmConversation: send success")
                        }
                    } else if (messageJsonObject.containsKey("tool_calls")) {
                        launch {
                            send(jsonToMessage(messageJsonObject))
                            println("LmConversation: send tool_calls success")
                        }
                    }
                } catch (e: Exception) {
                    println("LmConversation: Error parsing message: ${e.message}")
                    e.printStackTrace()
                }
            },
            onDone = {
                println("LmConversation: onDone called")
                this@callbackFlow.close()
            },
            onError = { code, errorMsg ->
                println("LmConversation: onError called with code $code: $errorMsg")
                if (code == 1) { // Cancelled
                    this@callbackFlow.close(CancellationException(errorMsg))
                } else {
                    this@callbackFlow.close(RuntimeException("Error $code: $errorMsg"))
                }
            }
        )
        
        awaitClose {
            // Wait for completion, process cancellation if needed via cancelProcess() explicitly handled externally
        }
    }

    fun cancelProcess() {
        checkIsAlive()
        LiteRtLmJni.cancelLmConversation(handle)
    }

    override fun close() {
        if (isAlive) {
            isAlive = false
            LiteRtLmJni.deleteLmConversation(handle)
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
