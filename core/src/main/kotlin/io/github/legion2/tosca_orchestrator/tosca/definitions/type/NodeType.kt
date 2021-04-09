package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import io.github.legion2.tosca_orchestrator.tosca.definitions.*

data class NodeType(
    val derived_from: String,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val attributes: Map<String, AttributeDefinition>?,
    val properties: Map<String, PropertyDefinition>?,
    @JsonDeserialize(converter = RequirementDefinitionOrderedMappingConverter::class)
    @JsonSerialize(converter = RequirementDefinitionOrderedMappingSerializerConverter::class)
    val requirements: Map<String, RequirementDefinition>?,
    val capabilities: Map<String, CapabilityDefinition>?,
    val interfaces: Map<String, InterfaceDefinition>?,
    val artifacts: Map<String, ArtifactDefinition>?
)

private class RequirementDefinitionOrderedMappingConverter : OrderedMappingConverter<String, RequirementDefinition>()
private class RequirementDefinitionOrderedMappingSerializerConverter :
    OrderedMappingSerializerConverter<String, RequirementDefinition>()