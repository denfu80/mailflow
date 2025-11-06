package com.mailflow.data.yaml

import org.snakeyaml.engine.v2.api.Load
import org.snakeyaml.engine.v2.api.LoadSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YamlParser @Inject constructor() {

    private val settings = LoadSettings.builder().build()
    private val yaml = Load(settings)

    fun parseAgentConfig(yamlString: String): Result<AgentConfig> {
        return try {
            val data = yaml.loadFromString(yamlString) as? Map<*, *>
                ?: return Result.failure(IllegalArgumentException("Invalid YAML format"))

            val agent = data["agent"] as? Map<*, *>
                ?: return Result.failure(IllegalArgumentException("Missing 'agent' key"))

            val config = AgentConfig(
                name = agent["name"] as? String
                    ?: return Result.failure(IllegalArgumentException("Missing 'name' field")),
                description = agent["description"] as? String,
                filters = parseFilters(agent["filters"] as? Map<*, *>),
                geminiPrompt = agent["gemini_prompt"] as? String
                    ?: return Result.failure(IllegalArgumentException("Missing 'gemini_prompt' field")),
                contextSchema = parseContextSchema(agent["context_schema"] as? Map<*, *>),
                actions = parseActions(agent["actions"] as? List<*>)
            )

            Result.success(config)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun validateAgentConfig(yamlString: String): Result<List<String>> {
        val errors = mutableListOf<String>()

        return try {
            val data = yaml.loadFromString(yamlString) as? Map<*, *>
            if (data == null) {
                errors.add("Invalid YAML format")
                return Result.success(errors)
            }

            val agent = data["agent"] as? Map<*, *>
            if (agent == null) {
                errors.add("Missing 'agent' key")
                return Result.success(errors)
            }

            if (agent["name"] == null || (agent["name"] as? String)?.isBlank() == true) {
                errors.add("Missing or empty 'name' field")
            }

            if (agent["gemini_prompt"] == null || (agent["gemini_prompt"] as? String)?.isBlank() == true) {
                errors.add("Missing or empty 'gemini_prompt' field")
            }

            Result.success(errors)
        } catch (e: Exception) {
            errors.add("YAML parsing error: ${e.message}")
            Result.success(errors)
        }
    }

    fun generateDefaultYaml(name: String, description: String = ""): String {
        return """
            |agent:
            |  name: "$name"
            |  description: "$description"
            |  filters:
            |    senders: []
            |    subjects: []
            |    keywords: []
            |    has_attachments: false
            |  gemini_prompt: |
            |    Analyze this email and extract relevant information.
            |    Provide a structured summary of the key points.
            |  context_schema:
            |    summary: string
            |    category: string
            |    priority: string
            |    action_required: boolean
            |  actions: []
        """.trimMargin()
    }

    private fun parseFilters(filtersMap: Map<*, *>?): EmailFilters? {
        if (filtersMap == null) return null

        return EmailFilters(
            senders = (filtersMap["senders"] as? List<*>)?.mapNotNull { it as? String },
            subjects = (filtersMap["subjects"] as? List<*>)?.mapNotNull { it as? String },
            hasAttachments = filtersMap["has_attachments"] as? Boolean,
            keywords = (filtersMap["keywords"] as? List<*>)?.mapNotNull { it as? String }
        )
    }

    private fun parseContextSchema(schemaMap: Map<*, *>?): Map<String, String> {
        if (schemaMap == null) return emptyMap()

        return schemaMap.entries.associate { (key, value) ->
            (key as String) to (value as? String ?: "string")
        }
    }

    private fun parseActions(actionsList: List<*>?): List<AgentAction>? {
        if (actionsList == null) return null

        return actionsList.mapNotNull { item ->
            val actionMap = item as? Map<*, *> ?: return@mapNotNull null
            val type = actionMap["type"] as? String ?: return@mapNotNull null
            @Suppress("UNCHECKED_CAST")
            val config = (actionMap["config"] as? Map<String, Any>)

            AgentAction(type = type, config = config)
        }
    }
}
