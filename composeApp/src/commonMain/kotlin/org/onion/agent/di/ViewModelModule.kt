package org.onion.agent.di

import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import org.onion.agent.viewmodel.BookSourceViewModel
import org.onion.agent.viewmodel.ChatViewModel

val viewModelModule  = module {
    viewModelOf(::BookSourceViewModel)
    singleOf(::ChatViewModel)
}