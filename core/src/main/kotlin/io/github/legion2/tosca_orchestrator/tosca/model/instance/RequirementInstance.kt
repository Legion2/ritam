package io.github.legion2.tosca_orchestrator.tosca.model.instance

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.legion2.tosca_orchestrator.tosca.definitions.Range
import io.github.legion2.tosca_orchestrator.tosca.definitions.Relationship
import io.github.legion2.tosca_orchestrator.tosca.definitions.RequirementAssignment
import io.github.legion2.tosca_orchestrator.tosca.definitions.refine
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedRequirement
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedCapabilityType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedRelationshipType
import io.github.legion2.tosca_orchestrator.tosca.normative.nodesRootType

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class CapabilityRef {
    data class CapabilityTypeRef(val type: ResolvedCapabilityType) : CapabilityRef()
    data class CapabilityDefinitionRef(val name: String) : CapabilityRef()
    companion object {
        fun from(
            requirementAssignment: RequirementAssignment?,
            resolvedRequirement: ResolvedRequirement,
            capabilityTypeResolver: TypeResolver<ResolvedCapabilityType>
        ): CapabilityRef {
            return if (requirementAssignment?.capability == null) {
                CapabilityTypeRef(capabilityTypeResolver(resolvedRequirement.capability))
            } else {
                runCatching {
                    CapabilityTypeRef(capabilityTypeResolver(requirementAssignment.capability))
                }.getOrElse {
                    CapabilityDefinitionRef(requirementAssignment.capability)
                }
            }
        }
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class NodeRef {
    data class NodeTypeRef(val type: TypeReference<ResolvedNodeType>) : NodeRef()
    data class NodeTemplateRef(val name: String) : NodeRef()
    companion object {
        fun from(
            requirementAssignment: RequirementAssignment?,
            resolvedRequirement: ResolvedRequirement,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>
        ): NodeRef {
            return if (requirementAssignment?.node == null) {
                NodeTypeRef(resolvedRequirement.node ?: nodesRootType)
            } else {
                runCatching {
                    NodeTypeRef(nodeTypeReferenceResolver(requirementAssignment.node))
                }.getOrElse {
                    NodeTemplateRef(requirementAssignment.node)
                }
            }
        }
    }
}

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class RelationshipRef {
    data class RelationshipTypeRef(val type: ResolvedRelationshipType) : RelationshipRef()
    data class RelationshipTemplateRef(val name: String) : RelationshipRef()
    companion object {
        fun from(
            requirementAssignment: RequirementAssignment?,
            resolvedRequirement: ResolvedRequirement,
            relationshipTypeResolver: TypeResolver<ResolvedRelationshipType>
        ): RelationshipRef {
            val relationship = requirementAssignment?.relationship
            return if (relationship == null) {
                RelationshipTypeRef(
                    relationshipTypeResolver(
                        resolvedRequirement.relationship
                            ?: throw IllegalArgumentException("No relationship type given")
                    )
                )
            } else when (relationship) {
                is Relationship.RelationshipNameOrType -> RelationshipTemplateRef(relationship.value)
                is Relationship.RelationshipOverride -> throw IllegalArgumentException("Embedded Relationship templates are not supported.")
            }
        }
    }
}

data class RequirementInstance(
    val capability: CapabilityRef,
    val node: NodeRef,
    val relationship: RelationshipRef,
    val occurrences: Range
) {
    companion object {
        fun validate(
            requirementAssignments: Map<String, RequirementAssignment>,
            requirements: Map<String, ResolvedRequirement>,
            capabilityTypeResolver: TypeResolver<ResolvedCapabilityType>,
            relationshipTypeResolver: TypeResolver<ResolvedRelationshipType>,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>
        ): Map<String, RequirementInstance> {
            return requirements.combine(requirementAssignments) { name, requirement, requirementAssignment ->
                requirement
                    ?: throw IllegalArgumentException("Requirement Assignments must have matching Requirement definitions: $name")
                val capability = CapabilityRef.from(requirementAssignment, requirement, capabilityTypeResolver)
                val node = NodeRef.from(requirementAssignment, requirement, nodeTypeReferenceResolver)
                val relationship = RelationshipRef.from(requirementAssignment, requirement, relationshipTypeResolver)
                val occurrences = requirement.occurrences.refine(requirementAssignment?.occurrences)

                RequirementInstance(capability, node, relationship, occurrences)
            }
        }
    }
}
