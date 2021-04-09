package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.type.RelationshipType
import io.github.legion2.tosca_orchestrator.tosca.model.addExceptionContextInfo
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.*

data class ResolvedRelationshipType(
    val name: String,
    val properties: Map<String, ResolvedProperty> = emptyMap(),
    val attributes: Map<String, ResolvedAttribute> = emptyMap(),
    val interfaces: Map<String, ResolvedInterface> = emptyMap(),
    val valid_target_types: List<String> = emptyList()
) {
    companion object {
        fun resolve(
            name: String,
            relationshipType: RelationshipType,
            relationshipTypes: (String) -> ResolvedRelationshipType,
            propertyResolverContext: PropertyResolverContext,
            interfaceTypeResolver: (String) -> ResolvedInterfaceType,
            artifactResolverContext: ArtifactResolverContext
        ): ResolvedRelationshipType = kotlin.runCatching {
            val relationshipTypePropertyResolverContext = propertyResolverContext.copy(
                expressionResolverContext = propertyResolverContext.expressionResolverContext.copy(
                    contextReferences = propertyResolverContext.expressionResolverContext.contextReferences.copy(
                        self = EntityReference.RelationshipTypeSelfReference
                    )
                )
            )

            val baseType = relationshipTypes(relationshipType.derived_from)
            val properties =
                baseType.properties.refine(
                    relationshipType.properties.orEmpty(),
                    relationshipTypePropertyResolverContext
                )
            val attributes =
                baseType.attributes.refine(
                    relationshipType.attributes.orEmpty(),
                    relationshipTypePropertyResolverContext
                )
            val operationResolverContext =
                OperationResolverContext(artifactResolverContext) { throw IllegalArgumentException("This context does not support artifact references") }
            val interfaces = baseType.interfaces.refine(
                relationshipType.interfaces.orEmpty(),
                interfaceTypeResolver,
                relationshipTypePropertyResolverContext,
                operationResolverContext
            )
            val validTargetTypes = relationshipType.valid_target_types ?: baseType.valid_target_types
            return ResolvedRelationshipType(name, properties, attributes, interfaces, validTargetTypes)
        }.addExceptionContextInfo { "Can not resolve Relationship Type: $name" }
    }
}
