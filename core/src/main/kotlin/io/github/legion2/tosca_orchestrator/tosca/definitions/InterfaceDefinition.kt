package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

data class InterfaceDefinition(
    val type: String?,
    val inputs: Map<String, PropertyDefinition>? = null,
    val operations: Map<String, OperationDefinition>? = null,
    val notifications: Map<String, NotificationDefinition>? = null
) {
    @JsonAnySetter
    @get:JsonAnyGetter
    val deprecatedOperationsSyntax: Map<String, OperationAssignment> = mutableMapOf()
}
