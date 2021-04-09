package io.github.legion2.tosca_orchestrator.tosca.definitions

data class NotificationDefinition(
    val description: String?,
    val implementation: NotificationImplementationDefinition?,
    val outputs: Map<String, AttributeMapping>?
)
