package io.github.legion2.tosca_orchestrator.tosca.definitions

data class RelationshipTemplate(
    val type: String,
    val description: String?,
    val metadata: Map<String, String>?,
    val properties: Map<String, PropertyAssignment>?,
    val attributes: Map<String, AttributeAssignment>?,
    val interfaces: Map<String, InterfaceAssignment>?,
    val copy: String?
)
