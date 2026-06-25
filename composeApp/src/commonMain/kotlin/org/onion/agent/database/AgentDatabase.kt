package org.onion.agent.database

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ChatToolLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
@ConstructedBy(AgentDatabaseConstructor::class)
abstract class AgentDatabase : RoomDatabase() {
    abstract fun chatHistoryDao(): ChatHistoryDao
}

@Suppress("KotlinNoActualForExpect")
expect object AgentDatabaseConstructor : RoomDatabaseConstructor<AgentDatabase> {
    override fun initialize(): AgentDatabase
}

fun createAgentDatabase(builder: RoomDatabase.Builder<AgentDatabase>): AgentDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

expect fun createAgentDatabaseBuilder(): RoomDatabase.Builder<AgentDatabase>
