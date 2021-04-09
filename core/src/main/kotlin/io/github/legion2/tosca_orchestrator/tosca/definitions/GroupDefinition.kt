package io.github.legion2.tosca_orchestrator.tosca.definitions

data class GroupDefinition(
    val type: String,
    val description: String?,
    val metadata: Map<String, String>?,
    val properties: Map<String, PropertyAssignment>?,
    val members: List<String>?
)
