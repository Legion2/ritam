package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.type.NodeType
import io.github.legion2.tosca_orchestrator.tosca.model.*
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.*

data class ResolvedNodeType(
    override val typeInfo: TypeInfo<ResolvedNodeType>,
    val properties: Map<String, ResolvedProperty> = emptyMap(),
    val attributes: Map<String, ResolvedAttribute> = emptyMap(),
    val requirements: Map<String, ResolvedRequirement> = emptyMap(),
    val capabilities: Map<String, ResolvedCapability> = emptyMap(),
    val interfaces: Map<String, ResolvedInterface> = emptyMap(),
    val artifacts: Map<String, ResolvedArtifact> = emptyMap()
) : Type<ResolvedNodeType> {
    companion object {
        fun resolve(
            name: TypeReference<ResolvedNodeType>,
            nodeType: NodeType,
            resolveNodeType: TypeResolver<ResolvedNodeType>,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>,
            propertyResolverContext: PropertyResolverContext,
            interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
            capabilityTypeResolver: TypeResolver<ResolvedCapabilityType>,
            artifactResolverContext: ArtifactResolverContext
        ): ResolvedNodeType = kotlin.runCatching {
            val nodeTypePropertyResolverContext = propertyResolverContext.copy(
                expressionResolverContext = propertyResolverContext.expressionResolverContext.copy(
                    contextReferences = propertyResolverContext.expressionResolverContext.contextReferences.copy(
                        self = EntityReference.NodeTypeSelfReference
                    )
                )
            )

            val baseType = resolveNodeType(nodeType.derived_from)
            val refinedProperties =
                baseType.properties.refine(nodeType.properties.orEmpty(), nodeTypePropertyResolverContext)
            val attributes = baseType.attributes.refine(nodeType.attributes.orEmpty(), nodeTypePropertyResolverContext)
            val requirements = baseType.requirements.addNew(nodeType.requirements.orEmpty()) {
                ResolvedRequirement.from(
                    it,
                    nodeTypeReferenceResolver
                )
            }
            val capabilities = baseType.capabilities.refineOrAdd(
                nodeType.capabilities.orEmpty(),
                { refine(it, capabilityTypeResolver, nodeTypeReferenceResolver, nodeTypePropertyResolverContext) },
                {
                    ResolvedCapability.from(
                        it,
                        capabilityTypeResolver,
                        nodeTypeReferenceResolver,
                        nodeTypePropertyResolverContext
                    )
                })
            val artifacts = baseType.artifacts.refineOrAdd(
                nodeType.artifacts.orEmpty(),
                { this },
                { ResolvedArtifact.from(it, artifactResolverContext) })
            val operationResolverContext = OperationResolverContext(artifactResolverContext) { artifactName: String ->
                artifacts[artifactName]
                    ?: throw IllegalArgumentException("Artifact does not exist in this context: $artifactName")
            }
            val interfaces = baseType.interfaces.refine(
                nodeType.interfaces.orEmpty(),
                interfaceTypeResolver,
                nodeTypePropertyResolverContext,
                operationResolverContext
            )

            ResolvedNodeType(
                TypeInfo(name, baseType.typeInfo),
                refinedProperties,
                attributes,
                requirements,
                capabilities,
                interfaces,
                artifacts
            )
        }.addExceptionContextInfo { "Could not resolve node type: $name" }
    }
}
