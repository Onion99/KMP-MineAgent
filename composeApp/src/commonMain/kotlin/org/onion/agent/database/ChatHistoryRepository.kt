package org.onion.agent.database

import com.onion.model.ChatMessage
import com.onion.model.ChatRole
import com.onion.model.PersistentToolCall
import com.onion.model.PersistentToolResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class ChatHistoryRepository(
    private val dao: ChatHistoryDao
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun observeSessions(query: String = ""): Flow<List<ChatSessionEntity>> {
        return if (query.isBlank()) dao.observeSessions() else dao.searchSessions(query.trim())
    }

    suspend fun getMostRecentSession(): ChatSessionEntity? = dao.getMostRecentSession()

    @OptIn(ExperimentalTime::class)
    suspend fun createSession(title: String = DEFAULT_TITLE): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val id = newId("session")
        dao.upsertSession(
            ChatSessionEntity(
                id = id,
                title = title.ifBlank { DEFAULT_TITLE },
                createdAtMillis = now,
                updatedAtMillis = now,
                messageCount = 0,
                lastMessagePreview = ""
            )
        )
        return id
    }

    suspend fun loadMessages(sessionId: String): List<ChatMessage> {
        return dao.getMessages(sessionId).map { it.toChatMessage() }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun saveMessage(sessionId: String, message: ChatMessage) {
        dao.upsertMessage(message.toEntity(sessionId))
        refreshSessionSummary(sessionId, message.message)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun renameSession(sessionId: String, title: String) {
        dao.renameSession(
            sessionId = sessionId,
            title = title.trim().ifBlank { DEFAULT_TITLE },
            updatedAtMillis = Clock.System.now().toEpochMilliseconds()
        )
    }

    suspend fun deleteSession(sessionId: String) {
        dao.deleteSession(sessionId)
    }

    suspend fun clearSessionMessages(sessionId: String) {
        dao.deleteMessages(sessionId)
        refreshSessionSummary(sessionId, "")
    }

    suspend fun upsertToolLog(toolLog: ChatToolLogEntity) {
        dao.upsertToolLog(toolLog)
    }

    suspend fun exportSessionMarkdown(sessionId: String): String {
        val session = dao.getSession(sessionId) ?: return ""
        val messages = dao.getMessages(sessionId)
        val toolLogs = dao.getToolLogs(sessionId).groupBy { it.messageId }
        return buildString {
            appendLine("# ${session.title}")
            appendLine()
            appendLine("- Created: ${session.createdAtMillis}")
            appendLine("- Updated: ${session.updatedAtMillis}")
            appendLine()
            messages.forEach { message ->
                appendLine("## ${message.role}")
                appendLine()
                appendLine(message.content)
                appendLine()
                val logs = toolLogs[message.id].orEmpty()
                if (logs.isNotEmpty()) {
                    appendLine("### Tool Logs")
                    logs.forEach { log ->
                        appendLine("- ${log.status}: ${log.toolName}")
                        appendLine("  - arguments: ${log.arguments}")
                        if (log.response.isNotBlank()) {
                            appendLine("  - response: ${log.response}")
                        }
                    }
                    appendLine()
                }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun refreshSessionSummary(sessionId: String, latestContent: String) {
        val current = dao.getSession(sessionId) ?: return
        val count = dao.countMessages(sessionId)
        val preview = latestContent.ifBlank {
            dao.getLastMessagePreview(sessionId).orEmpty()
        }.take(PREVIEW_LIMIT)
        val title = if (current.title == DEFAULT_TITLE && latestContent.isNotBlank()) {
            latestContent.titleFromContent()
        } else {
            current.title
        }
        dao.upsertSession(
            current.copy(
                title = title,
                updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
                messageCount = count,
                lastMessagePreview = preview
            )
        )
    }

    private fun ChatMessage.toEntity(sessionId: String): ChatMessageEntity {
        return ChatMessageEntity(
            id = id.toString(),
            sessionId = sessionId,
            role = role.name.lowercase(),
            content = message,
            toolCallsJson = json.encodeToString(toolCalls),
            toolResponsesJson = json.encodeToString(toolResponses),
            metadataJson = json.encodeToString(metadata ?: emptyMap()),
            createdAtMillis = createdAtMillis
        )
    }

    private fun ChatMessageEntity.toChatMessage(): ChatMessage {
        val role = runCatching { ChatRole.valueOf(role.uppercase()) }.getOrDefault(ChatRole.ASSISTANT)
        val metadata = runCatching {
            json.decodeFromString<Map<String, String>>(metadataJson)
        }.getOrDefault(emptyMap())
        val toolCalls = runCatching {
            json.decodeFromString<List<PersistentToolCall>>(toolCallsJson)
        }.getOrDefault(emptyList())
        val toolResponses = runCatching {
            json.decodeFromString<List<PersistentToolResponse>>(toolResponsesJson)
        }.getOrDefault(emptyList())
        return ChatMessage(
            message = content,
            isUser = role == ChatRole.USER,
            role = role,
            metadata = metadata,
            toolCalls = toolCalls,
            toolResponses = toolResponses,
            createdAtMillis = createdAtMillis,
            id = id.toLongOrNull() ?: Random.nextLong()
        )
    }

    private fun String.titleFromContent(): String {
        return lineSequence()
            .firstOrNull { it.isNotBlank() }
            ?.trim()
            ?.take(TITLE_LIMIT)
            ?: DEFAULT_TITLE
    }

    companion object {
        const val DEFAULT_TITLE = "New Chat"
        private const val TITLE_LIMIT = 36
        private const val PREVIEW_LIMIT = 120

        @OptIn(ExperimentalTime::class)
        fun newId(prefix: String): String {
            val now = Clock.System.now().toEpochMilliseconds()
            val suffix = Random.nextLong().toULong().toString(16)
            return "${prefix}_${now}_$suffix"
        }
    }
}
