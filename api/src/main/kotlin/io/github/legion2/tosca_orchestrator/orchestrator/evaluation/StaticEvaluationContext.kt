package io.github.legion2.tosca_orchestrator.orchestrator.evaluation

import io.github.legion2.tosca_orchestrator.orchestrator.model.DeploymentArtifact
import io.github.legion2.tosca_orchestrator.orchestrator.model.DynamicExpression
import io.github.legion2.tosca_orchestrator.tosca.definitions.Location
import io.github.legion2.tosca_orchestrator.tosca.model.addExceptionContextInfo
import io.github.legion2.tosca_orchestrator.tosca.model.instance.NodeInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.PropertyInstance
import io.github.legion2.tosca_orchestrator.tosca.model.instance.RelationshipInstance
import io.github.legion2.tosca_orchestrator.tosca.model.isSubtypeOf
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedPropertyNameOrIndex

data class StaticEvaluationContext(
    private val inputs: Map<String, PropertyInstance>,
    private val nodes: Map<String, NodeInstance>,
    private val relationships: Map<String, RelationshipInstance>,
    private val nodeSelf: NodeInstance? = null,
    private val relationshipSelf: RelationshipInstance? = null
) {

    fun evaluate(expression: ResolvedExpression): DynamicExpression {
        return when (expression) {
            is ResolvedExpression.Literal.String -> DynamicExpression.Literal.String(expression.value)
            is ResolvedExpression.Literal.Boolean -> DynamicExpression.Literal.Boolean(expression.value)
            is ResolvedExpression.Literal.Float -> DynamicExpression.Literal.Float(expression.value)
            is ResolvedExpression.Literal.Integer -> DynamicExpression.Literal.Integer(expression.value)
            is ResolvedExpression.Literal.Map -> DynamicExpression.Literal.Map(expression.value.mapValues { entry ->
                evaluate(
                    entry.value
                )
            })
            is ResolvedExpression.Literal.List -> DynamicExpression.Literal.List(expression.value.map { evaluate(it) })
            is ResolvedExpression.Function.Concat -> join(expression.string_value_expressions)
            is ResolvedExpression.Function.Join -> join(expression.string_value_expressions, expression.delimiter)
            is ResolvedExpression.Function.Token -> split(expression)
            is ResolvedExpression.Function.GetInput -> getInput(expression)
            is ResolvedExpression.Function.GetProperty -> getProperty(expression)
            is ResolvedExpression.Function.GetAttribute -> getAttribute(expression)
            is ResolvedExpression.Function.GetOperationOutput -> error("This is deprecated in tosca standard and not supported here")
            is ResolvedExpression.Function.GetNodesOfType -> getNodesOfType(expression)
            is ResolvedExpression.Function.GetArtifact -> getArtifact(expression)
        }
    }

    private fun getNodesOfType(expression: ResolvedExpression.Function.GetNodesOfType) =
        DynamicExpression.Literal.List(nodes.values.filter { it.typeInfo.isSubtypeOf(expression.nodeType) }
            .map { DynamicExpression.Literal.String(it.name) })

    private fun getArtifact(expression: ResolvedExpression.Function.GetArtifact): DynamicExpression.Function.GetArtifact {
        val nodeInstance = when (val entityName = expression.entityName) {
            is EntityReference.NodeReference -> nodes[entityName.name]
                ?: throw IllegalArgumentException("There is no node with the name: $entityName")
            EntityReference.NodeTypeSelfReference -> nodeSelf ?: error("node self reference not defined")
            is EntityReference.RelationshipReference -> error("Relationships do not have artifacts")
            EntityReference.RelationshipTypeSelfReference -> error("Relationships do not have artifacts")
        }
        val artifact = nodeInstance.artifacts[expression.artifact_name]
            ?: throw IllegalArgumentException("Artifact not defined: ${expression.artifact_name}")

        val location = when (val location = expression.location) {
            Location.LOCAL_FILE -> "${nodeInstance.name}-${expression.artifact_name}"
            is Location.Path -> location.path
            null -> artifact.deployPath
                ?: throw IllegalArgumentException("Location of Artifact not set: ${nodeInstance.name} ${expression.artifact_name}")
        }

        val remove = expression.remove ?: expression.location == Location.LOCAL_FILE

        return DynamicExpression.Function.GetArtifact(DeploymentArtifact(artifact, location, remove))
    }

    private fun getProperty(expression: ResolvedExpression.Function.GetProperty): DynamicExpression =
        kotlin.runCatching {
            val properties = when (val entityName = expression.entityName) {
                is EntityReference.NodeReference -> nodes[entityName.name]?.properties
                EntityReference.NodeTypeSelfReference -> nodeSelf?.properties
                    ?: error("node self reference not defined")
                is EntityReference.RelationshipReference -> relationships[entityName.name]?.properties
                EntityReference.RelationshipTypeSelfReference -> relationshipSelf?.properties
                    ?: error("relationship self reference not defined")
            }
                ?: throw IllegalArgumentException("There is no node or relationship with the name: ${expression.entityName}")
            val propertyInstance = properties.getProperty(expression.property_name)
            propertyInstance.getValue(this)
        }
            .addExceptionContextInfo { "Can not get Property '${expression.property_name}' from '${expression.entityName}'" }


    private fun getAttribute(expression: ResolvedExpression.Function.GetAttribute): DynamicExpression.Function.GetAttribute {
        return DynamicExpression.Function.GetAttribute(
            expression.entityName,
            expression.req_or_cap_name,
            expression.attribute_name
        )
    }

    private fun getInput(expression: ResolvedExpression.Function.GetInput): DynamicExpression = kotlin.runCatching {
        val propertyInstance = inputs.getProperty(expression.propertyName)
        propertyInstance.getValue(this)
    }.addExceptionContextInfo { "Can not get Input of '${expression.propertyName}'" }


    private fun Map<String, PropertyInstance>.getProperty(propertyName: List<ResolvedPropertyNameOrIndex>): PropertyInstance {
        val name = (propertyName[0] as ResolvedPropertyNameOrIndex.PropertyName).name
        //TODO use other list elements
        return this[name] ?: throw IllegalArgumentException("Property '$name' was not declared")
    }

    private fun split(token: ResolvedExpression.Function.Token): DynamicExpression.Function.Token {
        val value = evaluate(token.string_with_token)
        return DynamicExpression.Function.Token(value, token.string_of_token_chars, token.substring_index)
    }

    private fun join(values: List<ResolvedExpression>, delimiter: String? = null): DynamicExpression.Function.Join {
        return DynamicExpression.Function.Join(values.map { evaluate(it) }, delimiter ?: "")
    }
}

fun PropertyInstance.getValue(evaluationContext: StaticEvaluationContext): DynamicExpression {
    val resolvedExpression = expression ?: throw IllegalArgumentException("There is no value for the Property")
    return evaluationContext.evaluate(resolvedExpression)
}
