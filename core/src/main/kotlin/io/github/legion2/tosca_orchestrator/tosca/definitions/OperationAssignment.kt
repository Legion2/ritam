package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class OperationAssignment
@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
constructor(
    val description: String?,
    @JsonDeserialize(using = OperationImplementationDefinitionDeserializer::class)
    val implementation: OperationImplementationDefinition?,
    val inputs: Map<String, PropertyAssignment>?,
    val outputs: Map<String, AttributeMapping>?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(implementation: String) : this(
        null,
        OperationImplementationDefinition(ArtifactDefinitionOrName.ArtifactNameOrFileURI(implementation)),
        null,
        null
    )
}
