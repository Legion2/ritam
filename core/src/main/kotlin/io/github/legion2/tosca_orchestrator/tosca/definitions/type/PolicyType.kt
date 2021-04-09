package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.TriggerDefinition

data class PolicyType(
    val derived_from: String,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val properties: Map<String, PropertyDefinition>?,
    val targets: List<String>?,
    val triggers: Map<String, TriggerDefinition>?
)

