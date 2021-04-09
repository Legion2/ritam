package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.NotificationTypeDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.OperationTypeDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition

data class InterfaceType(
    val derived_from: String,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val inputs: Map<String, PropertyDefinition>?,
    val operations: Map<String, OperationTypeDefinition>?,
    val notifications: Map<String, NotificationTypeDefinition>?
)
