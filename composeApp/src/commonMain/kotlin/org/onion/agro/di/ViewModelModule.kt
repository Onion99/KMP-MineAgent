package org.onion.agro.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.onion.agro.database.createAgentDatabase
import org.onion.agro.database.createAgentDatabaseBuilder
import org.onion.agro.database.ChatHistoryRepository
import org.onion.agro.viewmodel.BookSourceViewModel
import org.onion.agro.viewmodel.ChatViewModel

val viewModelModule  = module {
    single { createAgentDatabase(createAgentDatabaseBuilder()) }
    single { get<org.onion.agro.database.AgentDatabase>().chatHistoryDao() }
    singleOf(::ChatHistoryRepository)
    viewModelOf(::BookSourceViewModel)
    singleOf(::ChatViewModel)
}
