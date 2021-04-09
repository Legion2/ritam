package io.github.legion2.tosca_orchestrator.tosca.model

import io.github.legion2.tosca_orchestrator.tosca.model.instance.*
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedNodeTemplate
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedRelationshipTemplate
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.isSubtypeOf

data class CompletedRelationship(
    val source: String,
    val target: String,
    val requirement: String,
    val capability: String,
    val relationshipTemplate: ResolvedRelationshipTemplate
)

data class CompletedNode(
    val host: String?,
    val nodeTemplate: ResolvedNodeTemplate
)

fun completeTopology(
    resolvedNodeTemplates: Map<String, ResolvedNodeTemplate>,
    resolvedRelationshipTemplates: Map<String, ResolvedRelationshipTemplate>,
    reusableTypes: ReusableTypes,
    expressionResolverContext: ExpressionResolverContext
): Pair<List<NodeInstance>, List<RelationshipInstance>> {
    val map = resolvedNodeTemplates.mapValues { (nodeTemplateName, nodeTemplate) ->
        try {
            val relationships = nodeTemplate.requirements.flatMap { (requirementName, requirement) ->
                val minimumNumberOfRelations = requirement.occurrences.lower_bound.value
                (1..minimumNumberOfRelations).map {
                    val (targetNodeTemplate, targetCapabilityName) = getTargetNodeTemplateAndCapability(
                        requirement,
                        requirementName,
                        nodeTemplate,
                        resolvedNodeTemplates,
                        reusableTypes
                    )

                    val relationshipTemplate = resolvedRelationshipTemplate(
                        requirement,
                        nodeTemplate,
                        targetNodeTemplate,
                        expressionResolverContext,
                        resolvedRelationshipTemplates
                    )

                    CompletedRelationship(
                        nodeTemplate.name,
                        targetNodeTemplate.name,
                        requirementName,
                        targetCapabilityName,
                        relationshipTemplate
                    )
                }
            }

            val host = relationships.find { it.requirement == "host" }?.target

            Pair(CompletedNode(host, nodeTemplate), relationships)
        } catch (e: RuntimeException) {
            throw IllegalArgumentException("Can not create Relations for node template: $nodeTemplateName", e)
        }
    }
    val nodes = map.map { it.value.first }.map { NodeInstance.from(it) }
    val relationships = map.flatMap { it.value.second }.map { RelationshipInstance.from(it) }
    return Pair(nodes, relationships)
}


private fun getTargetNodeTemplateAndCapability(
    requirement: RequirementInstance,
    requirementName: String,
    sourceNodeTemplate: ResolvedNodeTemplate,
    resolvedNodeTemplates: Map<String, ResolvedNodeTemplate>,
    reusableTypes: ReusableTypes
): Pair<ResolvedNodeTemplate, String> {
    return when (requirement.node) {
        is NodeRef.NodeTypeRef -> {
            resolvedNodeTemplates.values
                .filter { nodeTemplate ->
                    nodeTemplate.typeInfo.isSubtypeOf(reusableTypes.nodeTypes.get(requirement.node.type).typeInfo)
                }
                .ifEmpty { throw IllegalArgumentException("No matching node found in topology template with type ${requirement.node.type.name}") }
                .mapNotNull { targetNodeTemplate ->
                    val targetCapabilityName = try {
                        getTargetCapability(
                            requirement,
                            sourceNodeTemplate,
                            targetNodeTemplate,
                            reusableTypes
                        )
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                    targetCapabilityName?.let { targetNodeTemplate to it }
                }.single()
        }
        is NodeRef.NodeTemplateRef -> {
            val targetNodeName = requirement.node.name
            val targetNodeTemplate = (resolvedNodeTemplates[targetNodeName]
                ?: throw IllegalArgumentException("no target node template for requirement $requirementName"))
            val targetCapability = getTargetCapability(
                requirement,
                sourceNodeTemplate,
                targetNodeTemplate,
                reusableTypes
            )
            targetNodeTemplate to targetCapability
        }
    }
}

private fun getTargetCapability(
    requirement: RequirementInstance,
    sourceNodeTemplate: ResolvedNodeTemplate,
    targetNodeTemplate: ResolvedNodeTemplate,
    reusableTypes: ReusableTypes
): String {
    return when (requirement.capability) {
        is CapabilityRef.CapabilityTypeRef -> {
            targetNodeTemplate.capabilities.toList().find { (_, capability) ->
                capability.type.isSubtypeOf(requirement.capability.type) &&
                        capability.validSourceTypes.map { reusableTypes.nodeTypes.get(it) }
                            .any { sourceNodeTemplate.typeInfo.isSubtypeOf(it.typeInfo) }
            }?.first ?: throw IllegalArgumentException(
                "No matching capability found for requirement $requirement\n\non target node: $targetNodeTemplate"
            )
        }
        is CapabilityRef.CapabilityDefinitionRef -> {
            val name = requirement.capability.name
            val capability = (targetNodeTemplate.capabilities[name]
                ?: throw IllegalArgumentException("target node does not have capability ${requirement.capability}"))

            val validSourceTypes = capability.validSourceTypes.map { reusableTypes.nodeTypes.get(it) }
            if (validSourceTypes.none { sourceNodeTemplate.typeInfo.isSubtypeOf(it.typeInfo) }) {
                throw IllegalArgumentException("Source Node is not a subtype of any valid source type")
            }
            name
        }
    }
}

private fun resolvedRelationshipTemplate(
    requirement: RequirementInstance,
    sourceNodeTemplate: ResolvedNodeTemplate,
    targetNodeTemplate: ResolvedNodeTemplate,
    expressionResolverContext: ExpressionResolverContext,
    resolvedRelationshipTemplates: Map<String, ResolvedRelationshipTemplate>
): ResolvedRelationshipTemplate {
    return when (requirement.relationship) {
        is RelationshipRef.RelationshipTypeRef -> {
            ResolvedRelationshipTemplate.from(
                "${sourceNodeTemplate.name}-${targetNodeTemplate.name}",
                requirement.relationship.type,
                expressionResolverContext
            )
        }
        is RelationshipRef.RelationshipTemplateRef -> {
            resolvedRelationshipTemplates[requirement.relationship.name]
                ?: throw IllegalArgumentException("Relationship template does not exist: ${requirement.relationship}")
        }
    }
}
