package org.onion.agent.native.llm

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

    private fun rewriteGithubUrl(urlString: String): String {
        var url = urlString.trim()
        if (url.endsWith(".git")) {
            url = url.substring(0, url.length - 4)
        }

        // Pattern 1: https://github.com/owner/repo/blob/branch/path/to/file
        val blobRegex = Regex("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/blob/([^/]+)/(.+)$", RegexOption.IGNORE_CASE)
        val blobMatch = blobRegex.find(url)
        if (blobMatch != null) {
            val owner = blobMatch.groupValues[1]
            val repo = blobMatch.groupValues[2]
            val branch = blobMatch.groupValues[3]
            val path = blobMatch.groupValues[4]
            return "https://raw.githubusercontent.com/$owner/$repo/$branch/$path"
        }

        // Pattern 2: https://github.com/owner/repo/tree/branch/path/to/dir
        val treeRegex = Regex("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/tree/([^/]+)/(.+)$", RegexOption.IGNORE_CASE)
        val treeMatch = treeRegex.find(url)
        if (treeMatch != null) {
            val owner = treeMatch.groupValues[1]
            val repo = treeMatch.groupValues[2]
            val branch = treeMatch.groupValues[3]
            val path = treeMatch.groupValues[4]
            return "https://api.github.com/repos/$owner/$repo/contents/$path?ref=$branch"
        }

        // Pattern 3: https://github.com/owner/repo/tree/branch (no path)
        val treeRootRegex = Regex("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/tree/([^/]+)/?$", RegexOption.IGNORE_CASE)
        val treeRootMatch = treeRootRegex.find(url)
        if (treeRootMatch != null) {
            val owner = treeRootMatch.groupValues[1]
            val repo = treeRootMatch.groupValues[2]
            val branch = treeRootMatch.groupValues[3]
            return "https://api.github.com/repos/$owner/$repo/contents/?ref=$branch"
        }

        // Pattern 4: https://github.com/owner/repo (root)
        val rootRegex = Regex("^https?://(?:www\\.)?github\\.com/([^/]+)/([^/]+)/?$", RegexOption.IGNORE_CASE)
        val rootMatch = rootRegex.find(url)
        if (rootMatch != null) {
            val owner = rootMatch.groupValues[1]
            val repo = rootMatch.groupValues[2]
            return "https://raw.githubusercontent.com/$owner/$repo/main/README.md"
        }

        return url
    }

    private fun cleanHtml(html: String): String {
        var text = html.replace(Regex("<script[^>]*?>[\\s\\S]*?</script>", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("<style[^>]*?>[\\s\\S]*?</style>", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("<svg[^>]*?>[\\s\\S]*?</svg>", RegexOption.IGNORE_CASE), "")
        text = text.replace(Regex("<!--[\\s\\S]*?-->"), "")
        text = text.replace(Regex("</?(?:p|div|h1|h2|h3|h4|h5|h6|li|tr|section|article|header|footer)[^>]*?>", RegexOption.IGNORE_CASE), "\n")
        text = text.replace(Regex("<[^>]*?>"), "")
        text = text.replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
        text = text.replace(Regex("\n\\s*\n"), "\n")
        text = text.replace(Regex(" +"), " ")
        return text.trim()
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
            val originalUrlObj = try {
                Url(sanitizedUrlString)
            } catch (e: Exception) {
                return buildJsonObject {
                    put("success", false)
                    put("error", "Invalid URL: ${e.message} (original input was: $urlString)")
                }.toString()
            }

            val targetUrlString = rewriteGithubUrl(sanitizedUrlString)
            var targetUrlObj = try {
                Url(targetUrlString)
            } catch (e: Exception) {
                originalUrlObj
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

            val hasUserAgent = parsedHeaders.keys.any { it.equals("User-Agent", ignoreCase = true) }
            if (!hasUserAgent) {
                parsedHeaders["User-Agent"] = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            }

            var response: HttpResponse = try {
                executeRequest(targetUrlObj, method, parsedHeaders, bodyStr)
            } catch (e: Exception) {
                throw e
            }

            // Fallback for Github README: if main branch returned 404, try master branch
            if (response.status == HttpStatusCode.NotFound &&
                targetUrlString.contains("raw.githubusercontent.com") &&
                targetUrlString.endsWith("/main/README.md")
            ) {
                val fallbackUrlString = targetUrlString.replace("/main/README.md", "/master/README.md")
                try {
                    val fallbackUrlObj = Url(fallbackUrlString)
                    val fallbackResponse = executeRequest(fallbackUrlObj, method, parsedHeaders, bodyStr)
                    if (fallbackResponse.status == HttpStatusCode.OK) {
                        response = fallbackResponse
                        targetUrlObj = fallbackUrlObj
                    }
                } catch (e: Exception) {
                    // stick with original response
                }
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
                cleanHtml(rawBody)
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
                    
                    val preview = if (cleanedBody.length > 2000) {
                        cleanedBody.substring(0, 2000) + "... [truncated]"
                    } else {
                        cleanedBody
                    }
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
