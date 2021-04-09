package io.github.legion2.tosca_orchestrator.tosca.definitions

data class AttributeDefinition(
    val type: String,
    val description: String?,
    val default: Expression?,
    val status: Status?,
    val key_schema: SchemaDefinition?,
    val entry_schema: SchemaDefinition?
)
