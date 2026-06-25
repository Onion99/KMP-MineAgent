package org.onion.agent.database

import androidx.room.Room
import androidx.room.RoomDatabase
import java.io.File

actual fun createAgentDatabaseBuilder(): RoomDatabase.Builder<AgentDatabase> {
    val appData = System.getenv("APPDATA")
    val root = if (appData.isNullOrBlank()) {
        File(System.getProperty("user.home"), ".aura-llm")
    } else {
        File(appData, "AuraLLM")
    }
    val dbFile = File(root, "agent_chat.db")
    dbFile.parentFile?.mkdirs()
    return Room.databaseBuilder<AgentDatabase>(dbFile.absolutePath)
}
