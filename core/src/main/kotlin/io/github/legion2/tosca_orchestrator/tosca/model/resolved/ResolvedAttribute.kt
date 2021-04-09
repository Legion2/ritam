package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.AttributeDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.addNew
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression.Companion.resolve
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType

data class ResolvedAttribute(
    val type: ResolvedDataType,
    val default: ResolvedExpression?,
    val key_schema: ResolvedSchema?,
    val entry_schema: ResolvedSchema?
) {
    companion object {
        fun from(
            attributeDefinition: AttributeDefinition,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedAttribute {
            return attributeDefinition.run {
                ResolvedAttribute(
                    propertyResolverContext.dataTypeResolver(type),
                    default?.resolve(propertyResolverContext.expressionResolverContext),
                    key_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) },
                    entry_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) })
            }
        }
    }
}

fun Map<String, ResolvedAttribute>.refine(
    attributeDefinitions: Map<String, AttributeDefinition>,
    propertyResolverContext: PropertyResolverContext
) = addNew(attributeDefinitions) { ResolvedAttribute.from(it, propertyResolverContext) }
