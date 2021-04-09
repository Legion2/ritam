package io.github.legion2.tosca_orchestrator.tosca.definitions

data class OperationTypeDefinition(
    val description: String?,
    val inputs: Map<String, ParameterDefinition>?,
    val outputs: Map<String, AttributeMapping>?
)


