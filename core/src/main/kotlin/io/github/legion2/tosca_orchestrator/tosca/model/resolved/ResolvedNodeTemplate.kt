package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.NodeTemplate
import io.github.legion2.tosca_orchestrator.tosca.model.TypeInfo
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.instance.*
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedCapabilityType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedRelationshipType

//TODO
data class ResolvedNodeTemplate(
    val name: String,
    val typeInfo: TypeInfo<ResolvedNodeType>,
    val properties: Map<String, PropertyInstance>,
    val attributes: Map<String, AttributeInstance>,
    val interfaces: Map<String, InterfaceInstance>,
    val requirements: Map<String, RequirementInstance>,
    val capabilities: Map<String, CapabilityInstance>,
    val artifacts: Map<String, ResolvedArtifact>
) {
    companion object {
        fun from(
            name: String,
            nodeTemplate: NodeTemplate,
            nodeTypeResolver: TypeResolver<ResolvedNodeType>,
            capabilityTypeResolver: TypeResolver<ResolvedCapabilityType>,
            relationshipTypeResolver: TypeResolver<ResolvedRelationshipType>,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>,
            artifactResolverContext: ArtifactResolverContext,
            expressionResolverContext: ExpressionResolverContext
        ): ResolvedNodeTemplate {
            try {
                val nodeType = nodeTypeResolver(nodeTemplate.type)
                val properties = PropertyInstance.validate(
                    nodeTemplate.properties.orEmpty(),
                    nodeType.properties,
                    expressionResolverContext
                )

                val attributes = AttributeInstance.validate(
                    nodeTemplate.attributes.orEmpty(),
                    nodeType.attributes,
                    expressionResolverContext,
                    properties
                )
                val artifacts = ResolvedArtifact.validate(
                    nodeTemplate.artifacts.orEmpty(),
                    nodeType.artifacts,
                    artifactResolverContext
                )
                val operationResolverContext =
                    OperationResolverContext(artifactResolverContext) { artifactName: String ->
                        artifacts[artifactName]
                            ?: throw IllegalArgumentException("Artifact does not exist in this context: $artifactName")
                    }
                val interfaces = InterfaceInstance.validate(
                    nodeTemplate.interfaces.orEmpty(),
                    nodeType.interfaces,
                    expressionResolverContext,
                    operationResolverContext
                )
                val capabilities = CapabilityInstance.validate(
                    nodeTemplate.capabilities.orEmpty(),
                    nodeType.capabilities,
                    expressionResolverContext
                )
                val requirements = RequirementInstance.validate(
                    nodeTemplate.requirements.orEmpty(),
                    nodeType.requirements,
                    capabilityTypeResolver,
                    relationshipTypeResolver,
                    nodeTypeReferenceResolver
                )

                return ResolvedNodeTemplate(
                    name,
                    nodeType.typeInfo,
                    properties,
                    attributes,
                    interfaces,
                    requirements,
                    capabilities,
                    artifacts
                )
            } catch (e: RuntimeException) {
                throw IllegalArgumentException("Can not resolve Node Template: $name", e)
            }
        }
    }
}