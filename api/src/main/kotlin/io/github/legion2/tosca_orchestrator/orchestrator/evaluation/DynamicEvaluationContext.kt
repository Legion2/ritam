package io.github.legion2.tosca_orchestrator.orchestrator.evaluation

import io.github.legion2.tosca_orchestrator.orchestrator.model.DeploymentArtifact
import io.github.legion2.tosca_orchestrator.orchestrator.model.DynamicExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.EntityReference
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedPropertyNameOrIndex
import io.github.legion2.tosca_orchestrator.tosca.model.property.Value

data class ComponentContext(
    val attributes: Map<String, Value>,
)

data class RelationshipContext(
    val attributes: Map<String, Value>,
)

data class DynamicEvaluationContext(
    private val nodes: Map<String, ComponentContext>,
    private val relationships: Map<String, RelationshipContext>,
    private val nodeSelf: ComponentContext? = null,
    private val relationshipSelf: RelationshipContext? = null,
    private val registerDeploymentArtifact: (deploymentArtifact: DeploymentArtifact) -> Unit,
) {

    fun evaluate(expression: DynamicExpression): Value {
        return when (expression) {
            is DynamicExpression.Literal.String -> Value.String(expression.value)
            is DynamicExpression.Literal.Boolean -> Value.Boolean(expression.value)
            is DynamicExpression.Literal.Float -> Value.Float(expression.value)
            is DynamicExpression.Literal.Integer -> Value.Integer(expression.value)
            is DynamicExpression.Literal.Map -> Value.Map(expression.value.mapValues { entry -> evaluate(entry.value) })
            is DynamicExpression.Literal.List -> Value.List(expression.value.map { evaluate(it) })
            is DynamicExpression.Function.Join -> join(expression)
            is DynamicExpression.Function.Token -> split(expression)
            is DynamicExpression.Function.GetAttribute -> getAttribute(expression) //TODO dynamic
            is DynamicExpression.Function.GetArtifact -> getArtifact(expression)
        }
    }

    private fun getArtifact(expression: DynamicExpression.Function.GetArtifact): Value.String {
        registerDeploymentArtifact(expression.deploymentArtifact)
        return Value.String(expression.deploymentArtifact.location)
    }

    private fun getAttribute(expression: DynamicExpression.Function.GetAttribute): Value {
        val attributes = when (val entityName = expression.entityName) {
            is EntityReference.NodeReference -> nodes[entityName.name]?.attributes
                ?: throw IllegalArgumentException("There is no node with the name: $entityName")
            EntityReference.NodeTypeSelfReference -> nodeSelf?.attributes ?: error("node self reference not defined")
            is EntityReference.RelationshipReference -> relationships[entityName.name]?.attributes
                ?: throw IllegalArgumentException("There is no relationship with the name: $entityName")
            EntityReference.RelationshipTypeSelfReference -> relationshipSelf?.attributes
                ?: error("relationship self reference not defined")
        }
        return attributes.getAttributeValue(expression.attribute_name)
    }

    private fun Map<String, Value>.getAttributeValue(attributeName: List<ResolvedPropertyNameOrIndex>): Value {
        val name = (attributeName[0] as ResolvedPropertyNameOrIndex.PropertyName).name
        //TODO use other list elements
        return this[name] ?: throw IllegalArgumentException("Attribute '$name' was not declared")
    }

    private fun split(token: DynamicExpression.Function.Token): Value.String {
        val value = evaluate(token.string_with_token)
        if (value !is Value.String) {
            throw IllegalArgumentException()
        }
        return Value.String(value.value.split(token.string_of_token_chars)
            .getOrElse(token.substring_index) { throw IllegalArgumentException("Substring with index '${token.substring_index}' does not exist in the string '${token.string_with_token}' when using token '${token.string_of_token_chars}'") })
    }

    private fun join(expression: DynamicExpression.Function.Join): Value.String {
        return Value.String(expression.string_value_expressions.map { evaluate(it) }
            .filterIsInstance<Value.String>()
            .joinToString(expression.delimiter) { it.value })
    }
}

