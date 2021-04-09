package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyAssignment
import io.github.legion2.tosca_orchestrator.tosca.model.addExceptionContextInfo
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression.Companion.resolve
import io.github.legion2.tosca_orchestrator.tosca.model.property.ValidationInfo
import io.github.legion2.tosca_orchestrator.tosca.model.property.toValidationInfo
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty

data class PropertyInstance private constructor(
    val expression: ResolvedExpression?,
    val validationInfo: ValidationInfo
) {

    companion object {
        fun validate(resolvedExpression: ResolvedExpression?, resolvedProperty: ResolvedProperty): PropertyInstance {
            val expression = (resolvedExpression ?: resolvedProperty.default).also {
                if (it == null && resolvedProperty.required) {
                    throw IllegalArgumentException("No value provided for required property")
                }
            }
            return PropertyInstance(expression, resolvedProperty.toValidationInfo())
        }

        fun validate(
            propertyAssignments: Map<String, PropertyAssignment>,
            properties: Map<String, ResolvedProperty>,
            expressionResolverContext: ExpressionResolverContext
        ): Map<String, PropertyInstance> {
            val resolvedExpressions =
                propertyAssignments.mapValues { it.value.value.resolve(expressionResolverContext) }

            val populatedValues = properties.mapValues { (propertyName, resolvedProperty) ->
                runCatching {
                    validate(
                        resolvedExpressions[propertyName],
                        resolvedProperty
                    )
                }.addExceptionContextInfo { "Can not validate Property: $propertyName" }
            }
            val unknownProperties = resolvedExpressions - populatedValues.keys
            if (unknownProperties.isNotEmpty()) {
                throw IllegalArgumentException("Property values were provided but not defined: $unknownProperties")
            }
            return populatedValues
        }
    }
}
