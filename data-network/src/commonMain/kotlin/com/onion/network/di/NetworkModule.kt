package com.onion.network.di

import com.onion.network.http.getPlatformHttpEngine
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRedirect
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import kotlin.time.Duration.Companion.seconds

val networkModule  = module {
    single {
        getHttpClient()
    }
}

fun getHttpClient() = HttpClient(getPlatformHttpEngine()) {
    // 内容协商，用于自动的 JSON/XML 等格式的序列化和反序列化
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            explicitNulls = false
        })
    }
    // 自动打印网络请求和响应的详细日志
    /*install(Logging) {
        level = LogLevel.INFO
        logger = object : Logger {
            override fun log(message: String) {
                println("Logger Ktor => $message")
            }
        }
    }*/
    // 配置全局或单个请求的超时时间
    install(HttpTimeout) {
        connectTimeoutMillis  = 15.seconds.inWholeMilliseconds // connectTimeout
        socketTimeoutMillis   = 60.seconds.inWholeMilliseconds // readTimeout
        requestTimeoutMillis  = 60.seconds.inWholeMilliseconds // callTimeout
    }
    install(HttpRedirect) {
        checkHttpMethod = true // Default is true
        allowHttpsDowngrade = false // Default is false, equivalent to followSslRedirects=true
    }
    /*defaultRequest {
        url {
            takeFrom("https://docs.google.com")
        }
    }*/
}