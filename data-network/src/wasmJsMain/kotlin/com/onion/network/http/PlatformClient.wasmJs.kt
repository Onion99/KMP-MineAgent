package com.onion.network.http

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.js.Js

actual fun getPlatformHttpEngine(): HttpClientEngine = Js.create()
