package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator

class RequirementMapping @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
constructor(
    val mapping: List<String>,
    val properties: Map<String, PropertyAssignment>?,
    val attributes: Map<String, AttributeAssignment>?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(mapping: List<String>) : this(mapping, null, null)
}
