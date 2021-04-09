package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.*
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedCapabilityType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

class ResolvedCapability(
    val type: ResolvedCapabilityType,
    val properties: Map<String, ResolvedProperty>,
    val attributes: Map<String, ResolvedAttribute>,
    val validSourceTypes: List<TypeReference<ResolvedNodeType>>,
    val occurrences: Range
) {
    companion object {
        fun from(
            capabilityDefinition: CapabilityDefinition,
            capabilityTypes: TypeResolver<ResolvedCapabilityType>,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedCapability {
            val capabilityType = capabilityTypes(capabilityDefinition.type)
            val properties =
                capabilityType.properties.refine(capabilityDefinition.properties.orEmpty(), propertyResolverContext)
            val attributes =
                capabilityType.attributes.refine(capabilityDefinition.attributes.orEmpty(), propertyResolverContext)
            val validSourceTypes = capabilityDefinition.valid_source_types?.map(nodeTypeReferenceResolver)
                ?: capabilityType.validSourceTypes
            val occurrences = capabilityDefinition.occurrences ?: Range(LowerBound(1), UpperBound.UNBOUNDED)
            return ResolvedCapability(capabilityType, properties, attributes, validSourceTypes, occurrences)
        }
    }
}

fun ResolvedCapability.refine(
    capabilityDefinition: CapabilityDefinition,
    capabilityTypes: TypeResolver<ResolvedCapabilityType>,
    nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>,
    propertyResolverContext: PropertyResolverContext
): ResolvedCapability {
    val capabilityType = capabilityTypes(capabilityDefinition.type)
    if (this.type != capabilityType) {
        throw IllegalArgumentException("Type of Capability can not be changed")
    }

    val properties = properties.refine(capabilityDefinition.properties.orEmpty(), propertyResolverContext)
    val attributes = attributes.refine(capabilityDefinition.attributes.orEmpty(), propertyResolverContext)
    val validSourceTypes = capabilityDefinition.valid_source_types?.map { nodeTypeReferenceResolver(it) }
        ?: validSourceTypes
    val occurrences = occurrences.refine(capabilityDefinition.occurrences)

    return ResolvedCapability(type, properties, attributes, validSourceTypes, occurrences)
}
