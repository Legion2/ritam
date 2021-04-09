package io.github.legion2.tosca_orchestrator.tosca.definitions

data class CapabilityAssignment(
    val properties: Map<String, PropertyAssignment>?,
    val attributes: Map<String, AttributeAssignment>?,
    val occurrences: Range?
)
