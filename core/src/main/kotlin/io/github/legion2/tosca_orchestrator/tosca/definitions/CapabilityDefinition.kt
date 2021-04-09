package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator

data class CapabilityDefinition
@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
constructor(
    val type: String,
    val description: String?,
    val properties: Map<String, PropertyDefinition>?,
    val attributes: Map<String, AttributeDefinition>?,
    val valid_source_types: List<String>?,
    val occurrences: Range?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(type: String) : this(type, null, null, null, null, null)
}
