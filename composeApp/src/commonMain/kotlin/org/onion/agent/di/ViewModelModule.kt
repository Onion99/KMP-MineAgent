package org.onion.agent.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.onion.agent.database.createAgentDatabase
import org.onion.agent.database.createAgentDatabaseBuilder
import org.onion.agent.database.ChatHistoryRepository
import org.onion.agent.viewmodel.BookSourceViewModel
import org.onion.agent.viewmodel.ChatViewModel

val viewModelModule  = module {
    single { createAgentDatabase(createAgentDatabaseBuilder()) }
    single { get<org.onion.agent.database.AgentDatabase>().chatHistoryDao() }
    singleOf(::ChatHistoryRepository)
    viewModelOf(::BookSourceViewModel)
    singleOf(::ChatViewModel)
}
