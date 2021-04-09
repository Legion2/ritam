package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

data class NodeTemplate(
    val type: String,
    val description: String? = null,
    val metadata: Map<String, String>? = null,
    val directives: List<String>? = null,
    val properties: Map<String, PropertyAssignment>? = null,
    val attributes: Map<String, AttributeAssignment>? = null,
    @JsonDeserialize(converter = RequirementAssignmentOrderedMappingConverter::class)
    @JsonSerialize(converter = RequirementAssignmentOrderedMappingSerializerConverter::class)
    val requirements: Map<String, RequirementAssignment>? = null,
    val capabilities: Map<String, CapabilityAssignment>? = null,
    val interfaces: Map<String, InterfaceAssignment>? = null,
    val artifacts: Map<String, ArtifactDefinition>? = null,
    val node_filter: NodeFilter? = null,
    val copy: String? = null
)

private class RequirementAssignmentOrderedMappingConverter : OrderedMappingConverter<String, RequirementAssignment>()
private class RequirementAssignmentOrderedMappingSerializerConverter :
    OrderedMappingSerializerConverter<String, RequirementAssignment>()
