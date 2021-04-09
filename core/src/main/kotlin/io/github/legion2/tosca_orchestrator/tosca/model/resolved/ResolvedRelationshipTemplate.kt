package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.RelationshipTemplate
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.instance.AttributeInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.InterfaceInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.PropertyInstance
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedArtifactType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedRelationshipType
import java.net.URI

data class ResolvedRelationshipTemplate(
    val name: String,
    val properties: Map<String, PropertyInstance>,
    val attributes: Map<String, AttributeInstance>,
    val interfaces: Map<String, InterfaceInstance>
) {
    companion object {
        fun from(
            name: String,
            relationshipTemplate: RelationshipTemplate,
            relationshipTypes: (String) -> ResolvedRelationshipType,
            artifactResolverContext: ArtifactResolverContext,
            expressionResolverContext: ExpressionResolverContext
        ): ResolvedRelationshipTemplate {
            try {
                val resolvedRelationshipType = relationshipTypes(relationshipTemplate.type)

                val properties = PropertyInstance.validate(
                    relationshipTemplate.properties.orEmpty(),
                    resolvedRelationshipType.properties,
                    expressionResolverContext
                )
                val attributes = AttributeInstance.validate(
                    relationshipTemplate.attributes.orEmpty(),
                    resolvedRelationshipType.attributes,
                    expressionResolverContext,
                    properties
                )
                val operationResolverContext =
                    OperationResolverContext(artifactResolverContext) { throw IllegalArgumentException("This context does not support artifact references") }
                val interfaces = InterfaceInstance.validate(
                    relationshipTemplate.interfaces.orEmpty(),
                    resolvedRelationshipType.interfaces,
                    expressionResolverContext,
                    operationResolverContext
                )
                return ResolvedRelationshipTemplate(name, properties, attributes, interfaces)
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Can not instantiate Relationship: $name", e)
            }
        }

        /**
         * TODO
         */
        fun from(
            name: String,
            resolvedRelationshipType: ResolvedRelationshipType,
            expressionResolverContext: ExpressionResolverContext
        ): ResolvedRelationshipTemplate {
            try {
                val properties = PropertyInstance.validate(
                    emptyMap(),
                    resolvedRelationshipType.properties,
                    expressionResolverContext
                )
                val attributes = AttributeInstance.validate(
                    emptyMap(),
                    resolvedRelationshipType.attributes,
                    expressionResolverContext,
                    properties
                )
                val fakeArtifactTypeResolver: TypeResolver<ResolvedArtifactType> =
                    { throw  IllegalArgumentException("Artifacts must not be resolved in this context") }
                val artifactResolverContext = ArtifactResolverContext(fakeArtifactTypeResolver, URI(""), fakeArtifactTypeResolver)
                val operationResolverContext =
                    OperationResolverContext(artifactResolverContext) { throw IllegalArgumentException("This context does not support artifact references") }
                val interfaces = InterfaceInstance.validate(
                    emptyMap(),
                    resolvedRelationshipType.interfaces,
                    expressionResolverContext,
                    operationResolverContext
                )
                return ResolvedRelationshipTemplate(name, properties, attributes, interfaces)
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Can not instantiate Relationship: $name", e)
            }
        }
    }
}
