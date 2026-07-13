package org.onion.agro.database

import androidx.room.Room
import androidx.room.RoomDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

actual fun createAgentDatabaseBuilder(): RoomDatabase.Builder<AgentDatabase> {
    val documents = NSFileManager.defaultManager
        .URLsForDirectory(NSDocumentDirectory, NSUserDomainMask)
        .first() as NSURL
    val dbUrl = documents.URLByAppendingPathComponent("agent_chat.db")!!
    return Room.databaseBuilder<AgentDatabase>(dbUrl.path!!)
}
