package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * @property capability The required reserved keyname used that can be used to provide the name of a valid Capability Type that can fulfill the requirement.
 * @property node The optional reserved keyname used to provide the name of a valid [NodeType] that contains the capability definition that can be used to fulfill the requirement.
 */
data class RequirementDefinition @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    val capability: String,
    val node: String?,
    val relationship: String?,//TODO
    val node_filter: NodeFilter?,
    val occurrences: Range?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(capability: String) : this(capability, null, null, null, null)
}
