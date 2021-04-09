package io.github.legion2.tosca_orchestrator.tosca.definitions

data class NotificationTypeDefinition(
    val description: String?,
    val outputs: Map<String, AttributeMapping>?
)
