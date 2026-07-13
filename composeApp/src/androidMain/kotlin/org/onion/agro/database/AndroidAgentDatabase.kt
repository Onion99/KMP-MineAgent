package org.onion.agro.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

private lateinit var databaseContext: Context

fun initAgentDatabase(context: Context) {
    databaseContext = context.applicationContext
}

actual fun createAgentDatabaseBuilder(): RoomDatabase.Builder<AgentDatabase> {
    val dbFile = databaseContext.getDatabasePath("agent_chat.db")
    return Room.databaseBuilder<AgentDatabase>(
        context = databaseContext,
        name = dbFile.absolutePath
    )
}
