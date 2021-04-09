package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.AttributeDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition

data class GroupType(
    val derived_from: String?,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val attributes: Map<String, AttributeDefinition>?,
    val properties: Map<String, PropertyDefinition>?,
    val members: List<String>?
)
