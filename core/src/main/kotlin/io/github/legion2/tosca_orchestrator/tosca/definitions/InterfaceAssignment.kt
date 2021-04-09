package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonAnySetter

data class InterfaceAssignment(
    val inputs: Map<String, PropertyAssignment>? = null,
    val operations: Map<String, OperationAssignment>? = null,
    val notifications: Map<String, NotificationDefinition>? = null
) {
    @JsonAnySetter
    @get:JsonAnyGetter
    val deprecatedOperationsSyntax: Map<String, OperationAssignment> = mutableMapOf()
}
