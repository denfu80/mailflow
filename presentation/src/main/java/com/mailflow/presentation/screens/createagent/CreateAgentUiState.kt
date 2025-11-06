package com.mailflow.presentation.screens.createagent

data class CreateAgentUiState(
    val name: String = "",
    val description: String = "",
    val yamlConfig: String = DEFAULT_YAML_CONFIG,
    val nameError: String? = null,
    val yamlError: String? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
) {
    val isValid: Boolean
        get() = name.isNotBlank() &&
                nameError == null &&
                yamlError == null &&
                yamlConfig.isNotBlank()

    companion object {
        const val DEFAULT_YAML_CONFIG = """
agent:
  name: "My Agent"
  description: "Agent description"

filters:
  senders: []
  subjects: []
  hasAttachments: false

processing:
  prompt: "Analyze this email and extract key information"
  extractFields:
    - summary
    - action_items
    - priority
"""
    }
}
