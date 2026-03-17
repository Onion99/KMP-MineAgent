package com.onion.network.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory


expect fun getPlatformHttpEngine() : HttpClientEngine