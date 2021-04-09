package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.AttributeDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.InterfaceDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition

data class RelationshipType(
    val derived_from: String,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val properties: Map<String, PropertyDefinition>?,
    val attributes: Map<String, AttributeDefinition>?,
    val interfaces: Map<String, InterfaceDefinition>?,
    val valid_target_types: List<String>?
)
