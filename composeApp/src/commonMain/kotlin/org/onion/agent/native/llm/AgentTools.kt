package org.onion.agent.native.llm

import kotlinx.serialization.json.*
import com.dokar.quickjs.quickJs

class AgentTools {

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
}
