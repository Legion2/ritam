package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.*
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

data class ResolvedRequirement(
    val capability: String,//TODO resolve type
    val node: TypeReference<ResolvedNodeType>?,
    val relationship: String?,//TODO
    val node_filter: NodeFilter?,
    val occurrences: Range
) {
    companion object {
        fun from(
            requirementDefinition: RequirementDefinition,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>
        ): ResolvedRequirement {
            return ResolvedRequirement(
                requirementDefinition.capability,
                requirementDefinition.node?.let { nodeTypeReferenceResolver(it) },
                requirementDefinition.relationship,
                requirementDefinition.node_filter,
                requirementDefinition.occurrences ?: Range(LowerBound(1), UpperBound.Scalar(1))
            )
        }
    }
}
