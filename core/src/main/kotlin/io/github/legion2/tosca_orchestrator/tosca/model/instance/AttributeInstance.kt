package io.github.legion2.tosca_orchestrator.tosca.model.instance

import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.github.legion2.tosca_orchestrator.tosca.definitions.AttributeAssignment
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression.Companion.resolve
import io.github.legion2.tosca_orchestrator.tosca.model.property.ValidationInfo
import io.github.legion2.tosca_orchestrator.tosca.model.property.toValidationInfo
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttribute

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = JsonTypeInfo.As.WRAPPER_OBJECT)
sealed class AttributeInstance {
    data class AttributeExpression(val expression: ResolvedExpression, val validationInfo: ValidationInfo?) :
        AttributeInstance()

    data class ReflectedProperty(val expression: ResolvedExpression?, val validationInfo: ValidationInfo) :
        AttributeInstance()

    data class NoValue(val validationInfo: ValidationInfo?) : AttributeInstance()

    companion object {
        fun validate(
            attributeAssignments: Map<String, AttributeAssignment>,
            attributes: Map<String, ResolvedAttribute>,
            expressionResolverContext: ExpressionResolverContext,
            properties: Map<String, PropertyInstance>
        ): Map<String, AttributeInstance> {
            val propertiesReflection =
                properties.mapValues { ReflectedProperty(it.value.expression, it.value.validationInfo) }
            val definedAttributes = attributes.mapValues { (attributeName, attribute) ->
                val expression = attributeAssignments[attributeName]?.value?.resolve(expressionResolverContext)
                    ?: attribute.default
                if (expression == null) {
                    NoValue(attribute.toValidationInfo())
                } else {
                    AttributeExpression(expression, attribute.toValidationInfo())
                }
            }

            val assignedAttributes = attributeAssignments.mapValues { (_, propertyAssignment) ->
                AttributeExpression(propertyAssignment.value.resolve(expressionResolverContext), null)
            }

            return propertiesReflection + assignedAttributes + definedAttributes
        }
    }
}
