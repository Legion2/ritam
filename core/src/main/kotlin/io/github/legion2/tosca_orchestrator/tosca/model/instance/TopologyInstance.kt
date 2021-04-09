package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.definitions.TopologyTemplateDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.*
import io.github.legion2.tosca_orchestrator.tosca.model.property.ContextReferences
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression.Companion.resolve
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.*
import java.net.URI

data class TopologyInstance(
    val inputs: Map<String, PropertyInstance>,
    val nodes: List<NodeInstance>,
    val relationships: List<RelationshipInstance>,
    val outputs: Map<String, ResolvedExpression>
)

fun createInstanceModel(serviceTemplateFilePath: URI, inputs: Map<String, String>): TopologyInstance {
    return runCatching {
        val (importedServiceTemplate, allImportedServiceTemplates) = importAll(serviceTemplateFilePath)

        val reusableTypes = getAllTypes(allImportedServiceTemplates)

        val topologyTemplateDefinition = importedServiceTemplate.serviceTemplateDefinition.topology_template
            ?: throw IllegalArgumentException("Service Template must contain a Topology Template")
        val typeResolverContext = TypeResolverContext(
            importedServiceTemplate.namespace,
            importedServiceTemplate.namespaceAliases,
            importedServiceTemplate.unnamedImports
        )
        createInstanceModel(
            topologyTemplateDefinition,
            inputs,
            ReusableTypesWithResolverContext(reusableTypes, typeResolverContext),
            importedServiceTemplate.contextPath
        )
    }.addExceptionContextInfo { "Could not create Instance Model of: $serviceTemplateFilePath" }
}

private fun createInstanceModel(
    topologyTemplateDefinition: TopologyTemplateDefinition,
    inputs: Map<String, String>,
    reusableTypesWithResolverContext: ReusableTypesWithResolverContext,
    contextPath: URI
): TopologyInstance {
    val dataTypeResolver = reusableTypesWithResolverContext.createDataTypeResolver()
    val nodeTypeResolver = reusableTypesWithResolverContext.createNodeTypeResolver()
    val relationshipTypeResolver = reusableTypesWithResolverContext.createRelationshipTypeResolver()
    val capabilityTypeResolver = reusableTypesWithResolverContext.createCapabilityTypeResolver()
    val nodeTypeReferenceResolver = reusableTypesWithResolverContext.createNodeTypeReferenceResolver()
    val artifactTypeResolver = reusableTypesWithResolverContext.createArtifactTypeResolver()
    val artifactFileExtensionResolver = reusableTypesWithResolverContext.createArtifactFileExtensionResolver()
    val artifactResolverContext =
        ArtifactResolverContext(artifactTypeResolver, contextPath, artifactFileExtensionResolver)
    val expressionResolverContext = ExpressionResolverContext(ContextReferences(), nodeTypeReferenceResolver)
    val propertyResolverContext = PropertyResolverContext(dataTypeResolver, expressionResolverContext)
    val inputValues = validateInput(inputs, topologyTemplateDefinition, propertyResolverContext)

    val resolvedNodeTemplates = topologyTemplateDefinition.node_templates.orEmpty().mapValues { (name, nodeTemplate) ->
        ResolvedNodeTemplate.from(
            name,
            nodeTemplate,
            nodeTypeResolver,
            capabilityTypeResolver,
            relationshipTypeResolver,
            nodeTypeReferenceResolver,
            artifactResolverContext,
            expressionResolverContext.copy(contextReferences = ContextReferences(EntityReference.NodeReference(name)))
        )
    }

    val resolvedRelationshipTemplates =
        topologyTemplateDefinition.relationship_templates.orEmpty().mapValues { (name, relationshipTemplate) ->
            ResolvedRelationshipTemplate.from(
                name,
                relationshipTemplate,
                relationshipTypeResolver,
                artifactResolverContext,
                expressionResolverContext.copy(
                    contextReferences = ContextReferences(
                        EntityReference.RelationshipReference(name)
                    )
                )
            )
        }

    val (nodes, relationships) = completeTopology(
        resolvedNodeTemplates,
        resolvedRelationshipTemplates,
        reusableTypesWithResolverContext.reusableTypes,
        expressionResolverContext
    )

    val outputs =
        topologyTemplateDefinition.outputs.orEmpty().mapValues { it.value.value.resolve(expressionResolverContext) }

    return TopologyInstance(inputValues, nodes, relationships, outputs)
}

fun validateInput(
    inputs: Map<String, String>,
    topologyTemplateDefinition: TopologyTemplateDefinition,
    propertyResolverContext: PropertyResolverContext
): Map<String, PropertyInstance> {
    val inputValues =
        topologyTemplateDefinition.inputs.orEmpty().mapValues { (inputParameterName, inputParameterDefinition) ->
            runCatching {
                val resolvedProperty = ResolvedProperty.from(
                    inputParameterDefinition.propertyDefinition.copy(
                        type = inputParameterDefinition.propertyDefinition.type ?: "string"
                    ), propertyResolverContext
                )
                PropertyInstance.validate(
                    inputs[inputParameterName]?.let { ResolvedExpression.Literal.String(it) },
                    resolvedProperty
                )
            }.addExceptionContextInfo { "Can not validate input: $inputParameterName" }
        }
    val unknownInputs = inputs - inputValues.keys
    if (unknownInputs.isNotEmpty()) {
        throw IllegalArgumentException("Inputs were provided but not defined in the Topology Template: $unknownInputs")
    }
    return inputValues
}