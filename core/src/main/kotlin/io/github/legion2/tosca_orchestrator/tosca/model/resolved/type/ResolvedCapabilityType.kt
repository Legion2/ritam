package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.type.CapabilityType
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.PropertyResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttribute
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.refine

data class ResolvedCapabilityType(
    val name: String,
    val derivedFrom: ResolvedCapabilityType? = null,
    val properties: Map<String, ResolvedProperty> = emptyMap(),
    val attributes: Map<String, ResolvedAttribute> = emptyMap(),
    val validSourceTypes: List<TypeReference<ResolvedNodeType>> = emptyList()
) {
    companion object {
        fun resolve(
            name: String,
            capabilityType: CapabilityType,
            capabilityTypes: TypeResolver<ResolvedCapabilityType>,
            propertyResolverContext: PropertyResolverContext,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>
        ): ResolvedCapabilityType {
            val baseType = capabilityTypes(capabilityType.derived_from)
            val properties = baseType.properties.refine(capabilityType.properties.orEmpty(), propertyResolverContext)
            val attributes = baseType.attributes.refine(capabilityType.attributes.orEmpty(), propertyResolverContext)
            val validSourceTypes =
                capabilityType.valid_source_types?.map(nodeTypeReferenceResolver) ?: baseType.validSourceTypes
            return ResolvedCapabilityType(name, baseType, properties, attributes, validSourceTypes)
        }
    }
}

tailrec fun ResolvedCapabilityType.isSubtypeOf(type: ResolvedCapabilityType): Boolean {
    if (this == type) {
        return true
    }
    if (this.derivedFrom == null) {
        return false
    }
    return this.derivedFrom.isSubtypeOf(type)
}

