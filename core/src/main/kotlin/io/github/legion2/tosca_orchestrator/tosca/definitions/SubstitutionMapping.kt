package io.github.legion2.tosca_orchestrator.tosca.definitions

data class SubstitutionMapping(
    val node_type: String,
    val substitution_filter: NodeFilter?,
    val properties: Map<String, PropertyMapping>?,
    val attributes: Map<String, AttributeMapping>?,
    val capabilities: Map<String, CapabilityMapping>?,
    val requirements: Map<String, RequirementMapping>?,
    val interfaces: Map<String, Any>?
)
