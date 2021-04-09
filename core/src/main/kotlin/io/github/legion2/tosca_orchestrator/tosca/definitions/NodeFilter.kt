package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class NodeFilter(
    @JsonDeserialize(converter = PropertyFilterDefinitionOrderedMappingConverter::class)
    @JsonSerialize(converter = PropertyFilterDefinitionOrderedMappingSerializerConverter::class)
    val properties: Map<String, PropertyFilterDefinition>?,
    @JsonDeserialize(converter = PropertyDefinitionOrderedMappingConverter::class)
    @JsonSerialize(converter = PropertyDefinitionOrderedMappingSerializerConverter::class)
    val capabilities: Map<String, PropertyDefinition>?
)

private class PropertyFilterDefinitionOrderedMappingConverter :
    OrderedMappingConverter<String, PropertyFilterDefinition>()

private class PropertyFilterDefinitionOrderedMappingSerializerConverter :
    OrderedMappingSerializerConverter<String, PropertyFilterDefinition>()

private class PropertyDefinitionOrderedMappingConverter : OrderedMappingConverter<String, PropertyDefinition>()
private class PropertyDefinitionOrderedMappingSerializerConverter :
    OrderedMappingSerializerConverter<String, PropertyDefinition>()