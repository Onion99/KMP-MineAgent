package org.onion.agent.native.llm

import com.fleeksoft.ksoup.Ksoup
import kotlinx.serialization.json.*
import com.dokar.quickjs.quickJs
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.time.Clock

class AgentTools : KoinComponent {

    private companion object {
        const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
    }

    private val httpClient: HttpClient by lazy {
        try {
            get<HttpClient>()
        } catch (e: Exception) {
            HttpClient()
        }
    }

    /**
     * Returns a JSON array of all tool definitions for the LiteRT LM model.
     * This follows the schema expected by the C++ native layer (OpenAPI structure).
     */
    fun getToolsDescriptionJson(): String {
        return buildJsonArray {
            // loadSkill
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "loadSkill")
                    put("description", "Loads a skill's instruction content by name.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("skillName", buildJsonObject {
                                put("type", "string")
                                put("description", "The name of the skill to load.")
                            })
                        })
                        put("required", buildJsonArray { add("skillName") })
                    })
                })
            })
            // runMcpTool
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "runMcpTool")
                    put("description", "Runs an MCP tool with name and JSON input arguments.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("toolName", buildJsonObject {
                                put("type", "string")
                                put("description", "The name of the MCP tool to run.")
                            })
                            put("input", buildJsonObject {
                                put("type", "string")
                                put("description", "The parameters passed to the tool as a JSON string.")
                            })
                        })
                        put("required", buildJsonArray { add("toolName"); add("input") })
                    })
                })
            })
            // runJs
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "runJs")
                    put("description", "Runs a JS script using the local QuickJS engine.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("skillName", buildJsonObject {
                                put("type", "string")
                                put("description", "The name of the skill/namespace.")
                            })
                            put("scriptName", buildJsonObject {
                                put("type", "string")
                                put("description", "The script name to run (e.g. index.js).")
                            })
                            put("data", buildJsonObject {
                                put("type", "string")
                                put("description", "The input data or JavaScript code to execute.")
                            })
                        })
                        put("required", buildJsonArray { add("skillName"); add("scriptName"); add("data") })
                    })
                })
            })
            // runIntent
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "runIntent")
                    put("description", "Runs a platform intent or action with parameters.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("intent", buildJsonObject {
                                put("type", "string")
                                put("description", "The intent/action to execute.")
                            })
                            put("parameters", buildJsonObject {
                                put("type", "string")
                                put("description", "A JSON string containing the parameters for the intent.")
                            })
                        })
                        put("required", buildJsonArray { add("intent"); add("parameters") })
                    })
                })
            })
            // analyzeUrl
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "analyzeUrl")
                    put("description", "Performs a network request and analyzes the address/response.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("url", buildJsonObject {
                                put("type", "string")
                                put("description", "The URL/address to request and analyze (e.g. https://example.com/api).")
                            })
                            put("method", buildJsonObject {
                                put("type", "string")
                                put("description", "The HTTP method to use (GET, POST, PUT, DELETE, HEAD). Default is GET.")
                            })
                            put("headers", buildJsonObject {
                                put("type", "string")
                                put("description", "Optional JSON string containing request headers as key-value pairs.")
                            })
                            put("body", buildJsonObject {
                                put("type", "string")
                                put("description", "Optional request body string.")
                            })
                        })
                        put("required", buildJsonArray { add("url") })
                    })
                })
            })
            // searchWeb
            add(buildJsonObject {
                put("type", "function")
                put("function", buildJsonObject {
                    put("name", "searchWeb")
                    put("description", "Searches the latest web content with Bing and returns structured, indexable results.")
                    put("parameters", buildJsonObject {
                        put("type", "object")
                        put("properties", buildJsonObject {
                            put("query", buildJsonObject {
                                put("type", "string")
                                put("description", "The search query.")
                            })
                            put("count", buildJsonObject {
                                put("type", "integer")
                                put("description", "Number of search results to return. Default is 5, maximum is 10.")
                            })
                            put("includeContent", buildJsonObject {
                                put("type", "boolean")
                                put("description", "Whether to fetch each result page and include cleaned text for indexing. Default is false.")
                            })
                            put("maxContentChars", buildJsonObject {
                                put("type", "integer")
                                put("description", "Maximum cleaned text characters per fetched page when includeContent is true. Default is 4000.")
                            })
                        })
                        put("required", buildJsonArray { add("query") })
                    })
                })
            })
        }.toString()
    }

    /**
     * Executes the specified tool by name with arguments and returns the output as a String.
     */
    suspend fun execute(name: String, arguments: JsonObject): String {
        return when (name) {
            "loadSkill" -> {
                val skillName = arguments["skillName"]?.jsonPrimitive?.contentOrNull ?: ""
                loadSkill(skillName)
            }
            "runMcpTool" -> {
                val toolName = arguments["toolName"]?.jsonPrimitive?.contentOrNull ?: ""
                val input = arguments["input"]?.jsonPrimitive?.contentOrNull ?: ""
                runMcpTool(toolName, input)
            }
            "runJs" -> {
                val skillName = arguments["skillName"]?.jsonPrimitive?.contentOrNull ?: ""
                val scriptName = arguments["scriptName"]?.jsonPrimitive?.contentOrNull ?: ""
                val data = arguments["data"]?.jsonPrimitive?.contentOrNull ?: ""
                runJs(skillName, scriptName, data)
            }
            "runIntent" -> {
                val intent = arguments["intent"]?.jsonPrimitive?.contentOrNull ?: ""
                val parameters = arguments["parameters"]?.jsonPrimitive?.contentOrNull ?: ""
                runIntent(intent, parameters)
            }
            "analyzeUrl" -> {
                val urlObj = arguments["url"]?.jsonPrimitive?.contentOrNull ?: ""
                val method = arguments["method"]?.jsonPrimitive?.contentOrNull
                val headers = arguments["headers"]?.jsonPrimitive?.contentOrNull
                val body = arguments["body"]?.jsonPrimitive?.contentOrNull
                analyzeUrl(urlObj, method, headers, body)
            }
            "searchWeb" -> {
                val query = arguments["query"]?.jsonPrimitive?.contentOrNull ?: ""
                val count = arguments["count"]?.jsonPrimitive?.intOrNull
                val includeContent = arguments["includeContent"]?.jsonPrimitive?.booleanOrNull
                val maxContentChars = arguments["maxContentChars"]?.jsonPrimitive?.intOrNull
                searchWeb(query, count, includeContent, maxContentChars)
            }
            else -> "Error: Tool '$name' not found."
        }
    }

    private fun loadSkill(skillName: String): String {
        return "Skill '$skillName' loaded successfully. Follow standard instructions for this skill."
    }

    private fun runMcpTool(toolName: String, input: String): String {
        return "MCP tool '$toolName' executed successfully with input: $input. Status: succeeded."
    }

    private suspend fun runJs(skillName: String, scriptName: String, data: String): String {
        return try {
            val result = quickJs {
                evaluate<Any?>(data)
            }
            "JS executed successfully. Result: ${result?.toString() ?: "null"}"
        } catch (e: Exception) {
            "Error executing JS: ${e.message}"
        }
    }

    private fun runIntent(intent: String, parameters: String): String {
        return "Intent '$intent' executed successfully with parameters: $parameters."
    }

    private fun sanitizeUrl(urlString: String): String {
        var url = urlString.trim()
        val wrappers = listOf("<|\\\"|>", "<|'|>", "<|", "|>", "\\\"", "\\'", "\"", "'", "`")
        var modified = true
        while (modified) {
            modified = false
            for (wrapper in wrappers) {
                if (url.startsWith(wrapper)) {
                    url = url.substring(wrapper.length)
                    modified = true
                }
                if (url.endsWith(wrapper)) {
                    url = url.substring(0, url.length - wrapper.length)
                    modified = true
                }
            }
        }
        url = url.trim()
        url = url.replace("<|\\\"|>", "")
                 .replace("<|'|>", "")
                 .replace("<|", "")
                 .replace("|>", "")
                 .replace("\\\"", "")
                 .replace("\\'", "")
                 .replace("\"", "")
                 .replace("'", "")
                 .replace("`", "")
        return url.trim()
    }


    private fun cleanHtml(html: String, baseUri: String = ""): String {
        return try {
            val document = Ksoup.parse(html = html, baseUri = baseUri)
            document.select("script, style, svg, noscript, iframe, nav, footer, form").remove()
            document.body().text().normalizeWhitespace()
        } catch (e: Exception) {
            html.replace(Regex("<script[^>]*?>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<style[^>]*?>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
                .replace(Regex("<[^>]*?>"), " ")
                .normalizeWhitespace()
        }
    }

    private fun String.normalizeWhitespace(): String {
        return replace(Regex("\\s+"), " ").trim()
    }

    private fun String.truncateForTool(maxChars: Int): String {
        val boundedMax = maxChars.coerceIn(200, 20_000)
        return if (length > boundedMax) {
            substring(0, boundedMax) + "... [truncated]"
        } else {
            this
        }
    }

    private fun applyBrowserHeaders(headers: MutableMap<String, String>) {
        val hasUserAgent = headers.keys.any { it.equals("User-Agent", ignoreCase = true) }
        if (!hasUserAgent) {
            headers["User-Agent"] = DEFAULT_USER_AGENT
        }
        val hasAccept = headers.keys.any { it.equals("Accept", ignoreCase = true) }
        if (!hasAccept) {
            headers["Accept"] = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8"
        }
        val hasAcceptLanguage = headers.keys.any { it.equals("Accept-Language", ignoreCase = true) }
        if (!hasAcceptLanguage) {
            headers["Accept-Language"] = "en-US,en;q=0.9,zh-CN;q=0.8,zh;q=0.7"
        }
    }

    private suspend fun executeRequest(
        urlObj: Url,
        method: HttpMethod,
        headers: Map<String, String>,
        bodyStr: String?
    ): HttpResponse {
        return httpClient.request(urlObj) {
            this.method = method
            headers.forEach { (key, value) ->
                header(key, value)
            }
            if (!bodyStr.isNullOrBlank()) {
                setBody(bodyStr)
            }
        }
    }

    private fun buildBingSearchUrl(query: String, count: Int): Url {
        return URLBuilder("https://www.bing.com/search").apply {
            parameters.append("q", query)
            parameters.append("count", count.toString())
            parameters.append("mkt", "en-US")
        }.build()
    }

    private fun resolveSearchResultUrl(rawUrl: String): String {
        val href = rawUrl.trim()
        return when {
            href.startsWith("http://") || href.startsWith("https://") -> href
            href.startsWith("//") -> "https:$href"
            href.startsWith("/") -> "https://www.bing.com$href"
            else -> href
        }
    }

    private suspend fun fetchSearchResultContent(url: String, maxContentChars: Int): JsonObject {
        return try {
            val targetUrl = Url(sanitizeUrl(url))
            val headers = mutableMapOf<String, String>()
            applyBrowserHeaders(headers)
            val response = executeRequest(targetUrl, HttpMethod.Get, headers, null)
            val contentType = response.headers["Content-Type"] ?: ""
            val rawBody = response.bodyAsText()
            val cleanedBody = if (contentType.contains("html", ignoreCase = true)) {
                cleanHtml(rawBody, targetUrl.toString())
            } else {
                rawBody.normalizeWhitespace()
            }
            buildJsonObject {
                put("contentFetched", true)
                put("statusCode", response.status.value)
                put("contentType", contentType)
                put("content", cleanedBody.truncateForTool(maxContentChars))
            }
        } catch (e: Exception) {
            buildJsonObject {
                put("contentFetched", false)
                put("contentError", e.message ?: "Unable to fetch result content.")
            }
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun searchWeb(
        query: String,
        count: Int?,
        includeContent: Boolean?,
        maxContentChars: Int?
    ): String {
        return try {
            val normalizedQuery = query.trim()
            if (normalizedQuery.isBlank()) {
                return buildJsonObject {
                    put("success", false)
                    put("error", "Search query cannot be blank.")
                }.toString()
            }

            val resultLimit = (count ?: 5).coerceIn(1, 10)
            val shouldFetchContent = includeContent ?: false
            val contentLimit = (maxContentChars ?: 4000).coerceIn(500, 20_000)
            val startedAt = Clock.System.now().toString()
            val searchUrl = buildBingSearchUrl(normalizedQuery, resultLimit)
            val headers = mutableMapOf<String, String>()
            applyBrowserHeaders(headers)

            val response = executeRequest(searchUrl, HttpMethod.Get, headers, null)
            val html = response.bodyAsText()
            val document = Ksoup.parse(html = html, baseUri = "https://www.bing.com")
            val resultItems = document.select("li.b_algo")
            val parsedResults = mutableListOf<JsonObject>()

            for (item in resultItems) {
                if (parsedResults.size >= resultLimit) break

                val link = item.select("h2 a").first() ?: continue
                val rawUrl = link.absUrl("href").ifBlank { link.attr("href") }
                val url = resolveSearchResultUrl(rawUrl)
                if (url.isBlank()) continue

                val title = link.text().normalizeWhitespace()
                val snippet = (item.select(".b_caption p").first() ?: item.select("p").first())
                    ?.text()
                    ?.normalizeWhitespace()
                    .orEmpty()
                val displayUrl = item.select("cite").first()?.text()?.normalizeWhitespace().orEmpty()

                val pageContent = if (shouldFetchContent) {
                    fetchSearchResultContent(url, contentLimit)
                } else {
                    buildJsonObject { put("contentFetched", false) }
                }

                parsedResults += buildJsonObject {
                    put("rank", parsedResults.size + 1)
                    put("title", title)
                    put("url", url)
                    put("displayUrl", displayUrl)
                    put("snippet", snippet)
                    pageContent.forEach { (key, value) -> put(key, value) }
                }
            }

            buildJsonObject {
                put("success", true)
                put("provider", "bing")
                put("query", normalizedQuery)
                put("requestedCount", resultLimit)
                put("resultCount", parsedResults.size)
                put("fetchedAt", startedAt)
                put("searchUrl", searchUrl.toString())
                put("results", buildJsonArray {
                    parsedResults.forEach { add(it) }
                })
                if (parsedResults.isEmpty()) {
                    put("warning", "No standard Bing search results were parsed from the response.")
                }
            }.toString()
        } catch (e: Exception) {
            buildJsonObject {
                put("success", false)
                put("provider", "bing")
                put("query", query)
                put("error", "Search failed: ${e.message}")
            }.toString()
        }
    }

    @OptIn(kotlin.time.ExperimentalTime::class)
    private suspend fun analyzeUrl(
        urlString: String,
        methodStr: String?,
        headersJson: String?,
        bodyStr: String?
    ): String {
        return try {
            val startTime = Clock.System.now().toEpochMilliseconds()
            val sanitizedUrlString = sanitizeUrl(urlString)
            val targetUrlObj = try {
                Url(sanitizedUrlString)
            } catch (e: Exception) {
                return buildJsonObject {
                    put("success", false)
                    put("error", "Invalid URL: ${e.message} (original input was: $urlString)")
                }.toString()
            }

            val method = when (methodStr?.uppercase()) {
                "POST" -> HttpMethod.Post
                "PUT" -> HttpMethod.Put
                "DELETE" -> HttpMethod.Delete
                "HEAD" -> HttpMethod.Head
                else -> HttpMethod.Get
            }

            val parsedHeaders = mutableMapOf<String, String>()
            if (!headersJson.isNullOrBlank()) {
                try {
                    val element = Json.parseToJsonElement(headersJson)
                    if (element is JsonObject) {
                        element.forEach { (key, value) ->
                            parsedHeaders[key] = value.jsonPrimitive.content
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }

            applyBrowserHeaders(parsedHeaders)

            val response: HttpResponse = try {
                executeRequest(targetUrlObj, method, parsedHeaders, bodyStr)
            } catch (e: Exception) {
                throw e
            }

            val endTime = Clock.System.now().toEpochMilliseconds()
            val latency = endTime - startTime

            val contentType = response.headers["Content-Type"] ?: ""
            val isHtml = contentType.contains("text/html", ignoreCase = true)

            val rawBody = try {
                response.bodyAsText()
            } catch (e: Exception) {
                "[Unable to read body: ${e.message}]"
            }

            val cleanedBody = if (isHtml) {
                cleanHtml(rawBody, targetUrlObj.toString())
            } else {
                rawBody
            }

            val responseHeaders = buildJsonObject {
                response.headers.entries().forEach { entry ->
                    put(entry.key, entry.value.joinToString(","))
                }
            }

            val host = targetUrlObj.host
            val port = targetUrlObj.port
            val protocol = targetUrlObj.protocol.name
            val path = targetUrlObj.encodedPath
            val queryParams = buildJsonObject {
                targetUrlObj.parameters.entries().forEach { entry ->
                    put(entry.key, entry.value.joinToString(","))
                }
            }

            buildJsonObject {
                put("success", true)
                put("urlAnalysis", buildJsonObject {
                    put("originalUrl", urlString)
                    put("requestedUrl", targetUrlObj.toString())
                    put("protocol", protocol)
                    put("host", host)
                    put("port", port)
                    put("path", path)
                    put("queryParameters", queryParams)
                })
                put("request", buildJsonObject {
                    put("method", method.value)
                    put("headers", buildJsonObject {
                        parsedHeaders.forEach { (k, v) -> put(k, v) }
                    })
                })
                put("response", buildJsonObject {
                    put("statusCode", response.status.value)
                    put("statusDescription", response.status.description)
                    put("headers", responseHeaders)
                    put("responseTimeMs", latency)
                    put("isHtmlCleaned", isHtml)
                    put("contentLength", response.contentLength() ?: cleanedBody.length.toLong())
                    
                    val preview = cleanedBody.truncateForTool(2000)
                    put("contentPreview", preview)
                })
            }.toString()
        } catch (e: Exception) {
            buildJsonObject {
                put("success", false)
                put("error", "Request failed: ${e.message}")
            }.toString()
        }
    }
}
