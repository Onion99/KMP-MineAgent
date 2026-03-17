package com.onion.network.service

import com.onion.model.BookSource
import com.onion.network.constant.UA_NAME
import com.skydoves.sandwich.ktor.getApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsChannel
import kotlin.collections.addAll
import kotlin.collections.firstOrNull
import kotlin.collections.isEmpty

class BookSourceClient(val httpClient: HttpClient) {

    suspend fun importSourceUrl(url: String){
        val requestUrl: String
        val noUaRequest = url.endsWith("#requestWithoutUA")
        requestUrl = if (noUaRequest) {
            url.substringBeforeLast("#requestWithoutUA")
        } else {
            url
        }
        // 发起请求并获取响应
        httpClient.getApiResponse<List<BookSource>>(requestUrl) {
            if (noUaRequest) {
                header(UA_NAME, "null")
            }
        }
    }
}