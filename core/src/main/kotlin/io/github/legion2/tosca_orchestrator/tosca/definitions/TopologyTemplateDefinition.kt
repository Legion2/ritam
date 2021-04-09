package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class TopologyTemplateDefinition(
    val description: String? = null,
    val inputs: Map<String, ParameterDefinition>? = null,
    val node_templates: Map<String, NodeTemplate>? = null,
    val relationship_templates: Map<String, RelationshipTemplate>? = null,
    val groups: Map<String, GroupDefinition>? = null,
    @JsonDeserialize(converter = PolicyDefinitionOrderedMappingConverter::class)
    @JsonSerialize(converter = PolicyDefinitionOrderedMappingSerializerConverter::class)
    val policies: Map<String, PolicyDefinition>? = null,
    val outputs: Map<String, AttributeAssignment>? = null,
    val substitution_mapping: SubstitutionMapping? = null,
    val workflows: Map<String, ImperativeWorkflowDefinition>? = null
)

private class PolicyDefinitionOrderedMappingConverter : OrderedMappingConverter<String, PolicyDefinition>()
private class PolicyDefinitionOrderedMappingSerializerConverter :
    OrderedMappingSerializerConverter<String, PolicyDefinition>()
