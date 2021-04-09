package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.definitions.ServiceTemplateDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.property.ContextReferences
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ArtifactResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.PropertyResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.*

data class ReusableNamespace(
    val namespace: Namespace,
    val dataTypes: Map<String, ResolvedDataType>,
    val nodeTypes: Map<String, ResolvedNodeType>,
    val relationshipTypes: Map<String, ResolvedRelationshipType>,
    val interfaceTypes: Map<String, ResolvedInterfaceType>,
    val capabilityTypes: Map<String, ResolvedCapabilityType>,
    val artifactTypes: Map<String, ResolvedArtifactType>,
    val policyTypes: Map<String, ResolvedPolicyType>,
    val namespaceAliases: Map<String, Namespace>
) {
    companion object {
        /**
         * Helper data class used as container for important information which is required everywhere and therefore is provided as context.
         */
        private data class InternalContext(
            val serviceTemplateDefinition: ServiceTemplateDefinition,
            val reusableTypesWithResolverContext: ReusableTypesWithResolverContext,
            val propertyResolverContext: PropertyResolverContext
        )

        fun from(serviceTemplateDefinition: ImportedServiceTemplate, reusableTypes: ReusableTypes): ReusableNamespace {
            return kotlin.runCatching {
                val typeResolverContext = TypeResolverContext(
                    serviceTemplateDefinition.namespace,
                    serviceTemplateDefinition.namespaceAliases,
                    serviceTemplateDefinition.unnamedImports
                )
                val reusableTypesWithResolverContext =
                    ReusableTypesWithResolverContext(reusableTypes, typeResolverContext)
                val nodeTypeReferenceResolver =
                    reusableTypesWithResolverContext.createNodeTypeReferenceResolver(serviceTemplateDefinition.serviceTemplateDefinition.node_types.orEmpty().keys)
                val expressionResolverContext =
                    ExpressionResolverContext(ContextReferences(), nodeTypeReferenceResolver)
                val dataTypes = getDataTypes(
                    serviceTemplateDefinition.serviceTemplateDefinition,
                    reusableTypesWithResolverContext,
                    expressionResolverContext
                )
                val dataTypeResolver = reusableTypesWithResolverContext.createDataTypeResolver(dataTypes)
                val propertyResolverContext = PropertyResolverContext(dataTypeResolver, expressionResolverContext)
                val internalContext = InternalContext(
                    serviceTemplateDefinition.serviceTemplateDefinition,
                    reusableTypesWithResolverContext,
                    propertyResolverContext
                )

                with(internalContext) {
                    val artifactTypes = getArtifactTypes()
                    val artifactTypeResolver =
                        reusableTypesWithResolverContext.createArtifactTypeResolver(artifactTypes)
                    val artifactTypeFileExtensionResolver =
                        reusableTypesWithResolverContext.createArtifactFileExtensionResolver(artifactTypes)
                    val artifactResolverContext =
                        ArtifactResolverContext(artifactTypeResolver, serviceTemplateDefinition.contextPath, artifactTypeFileExtensionResolver)
                    val interfaceTypes = getInterfaceTypes()
                    val interfaceTypeResolver =
                        reusableTypesWithResolverContext.createInterfaceTypeResolver(interfaceTypes)
                    val capabilityTypes = getCapabilityTypes(nodeTypeReferenceResolver)
                    val capabilityTypeResolver =
                        reusableTypesWithResolverContext.createCapabilityTypeResolver(capabilityTypes)
                    val nodeTypes = getNodeTypes(
                        interfaceTypeResolver,
                        capabilityTypeResolver,
                        nodeTypeReferenceResolver,
                        artifactResolverContext
                    )
                    val relationshipTypes = getRelationshipTypes(interfaceTypeResolver, artifactResolverContext)
                    val policyTypes = getPolicyTypes()

                    ReusableNamespace(
                        serviceTemplateDefinition.namespace,
                        dataTypes,
                        nodeTypes,
                        relationshipTypes,
                        interfaceTypes,
                        capabilityTypes,
                        artifactTypes,
                        policyTypes,
                        serviceTemplateDefinition.namespaceAliases
                    )
                }
            }
                .addExceptionContextInfo { "Can not resolve the types of Service Template: ${serviceTemplateDefinition.uri}" }
        }

        private fun getDataTypes(
            serviceTemplateDefinition: ServiceTemplateDefinition,
            reusableTypesWithResolverContext: ReusableTypesWithResolverContext,
            expressionResolverContext: ExpressionResolverContext
        ): Map<String, ResolvedDataType> {
            return topologicalSort(serviceTemplateDefinition.data_types.orEmpty()) {
                listOf(it.derivedFrom)
            }.entries.fold(emptyMap()) { resolvedTypes, (name, dataType) ->
                resolvedTypes + (name to ResolvedDataType.resolve(
                    name,
                    dataType,
                    PropertyResolverContext(
                        reusableTypesWithResolverContext.createDataTypeResolver(resolvedTypes),
                        expressionResolverContext
                    )
                ))
            }
        }

        private fun InternalContext.getInterfaceTypes(): Map<String, ResolvedInterfaceType> {
            return topologicalSort(serviceTemplateDefinition.interface_types.orEmpty()) {
                listOf(it.derived_from)
            }.entries.fold(emptyMap()) { resolvedTypes, (name, interfaceType) ->
                resolvedTypes + (name to ResolvedInterfaceType.resolve(
                    name,
                    interfaceType,
                    reusableTypesWithResolverContext.createInterfaceTypeResolver(resolvedTypes),
                    propertyResolverContext
                ))
            }
        }

        private fun InternalContext.getCapabilityTypes(nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>): Map<String, ResolvedCapabilityType> {
            return topologicalSort(serviceTemplateDefinition.capability_types.orEmpty()) {
                listOf(it.derived_from)
            }.entries.fold(emptyMap()) { resolvedTypes, (name, capabilityType) ->
                resolvedTypes + (name to ResolvedCapabilityType.resolve(
                    name,
                    capabilityType,
                    reusableTypesWithResolverContext.createCapabilityTypeResolver(resolvedTypes),
                    propertyResolverContext,
                    nodeTypeReferenceResolver
                ))
            }
        }

        private fun InternalContext.getNodeTypes(
            interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
            capabilityTypeResolver: TypeResolver<ResolvedCapabilityType>,
            nodeTypeReferenceResolver: TypeResolver<TypeReference<ResolvedNodeType>>,
            artifactResolverContext: ArtifactResolverContext
        ): Map<String, ResolvedNodeType> {
            return topologicalSort(serviceTemplateDefinition.node_types.orEmpty()) {
                listOf(it.derived_from)
            }.entries.fold(emptyMap()) { resolvedNodeTypes, (name, nodeType) ->
                resolvedNodeTypes + (name to ResolvedNodeType.resolve(
                    TypeReference(
                        reusableTypesWithResolverContext.typeResolverContext.namespace,
                        name
                    ),
                    nodeType,
                    reusableTypesWithResolverContext.createNodeTypeResolver(resolvedNodeTypes),
                    nodeTypeReferenceResolver,
                    propertyResolverContext,
                    interfaceTypeResolver,
                    capabilityTypeResolver,
                    artifactResolverContext
                ))
            }
        }

        private fun InternalContext.getRelationshipTypes(
            interfaceTypeResolver: TypeResolver<ResolvedInterfaceType>,
            artifactResolverContext: ArtifactResolverContext
        ): Map<String, ResolvedRelationshipType> {
            return topologicalSort(serviceTemplateDefinition.relationship_types.orEmpty()) { relationshipType ->
                listOf(relationshipType.derived_from)
            }.entries.fold(emptyMap()) { resolvedRelationshipTypes, (name, relationshipType) ->
                resolvedRelationshipTypes + (name to ResolvedRelationshipType.resolve(
                    name,
                    relationshipType,
                    reusableTypesWithResolverContext.createRelationshipTypeResolver(resolvedRelationshipTypes),
                    propertyResolverContext,
                    interfaceTypeResolver,
                    artifactResolverContext
                ))
            }
        }

        private fun InternalContext.getArtifactTypes(): Map<String, ResolvedArtifactType> {
            return topologicalSort(serviceTemplateDefinition.artifact_types.orEmpty()) {
                listOf(it.derived_from)
            }.entries.fold(emptyMap()) { resolvedArtifactTypes, (name, artifactType) ->
                resolvedArtifactTypes + (name to ResolvedArtifactType.resolve(
                    TypeReference(
                        reusableTypesWithResolverContext.typeResolverContext.namespace,
                        name
                    ),
                    artifactType,
                    reusableTypesWithResolverContext.createArtifactTypeResolver(resolvedArtifactTypes),
                    propertyResolverContext
                ))
            }
        }

        private fun InternalContext.getPolicyTypes(): Map<String, ResolvedPolicyType> {
            return topologicalSort(serviceTemplateDefinition.policy_types.orEmpty()) {
                listOf(it.derived_from)
            }.entries.fold(emptyMap()) { resolvedArtifactTypes, (_, _) ->
                resolvedArtifactTypes //TODO + (name to TODO())
            }
        }
    }
}