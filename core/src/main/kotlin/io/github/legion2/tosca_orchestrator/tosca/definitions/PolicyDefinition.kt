package io.github.legion2.tosca_orchestrator.tosca.definitions

data class PolicyDefinition(
    val type: String,
    val description: String?,
    val metadata: Map<String, String>?,
    val properties: Map<String, PropertyAssignment>?,
    val targets: List<String>?,
    val triggers: Map<String, TriggerDefinition>?
)
