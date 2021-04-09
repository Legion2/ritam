package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedExpression.Companion.resolve
import io.github.legion2.tosca_orchestrator.tosca.model.refineOrAdd
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.isCompatibleType

data class ResolvedProperty(
    val type: ResolvedDataType,
    val required: Boolean,
    val default: ResolvedExpression? = null,
    val constraints: List<ConstraintClause> = emptyList(),
    val key_schema: ResolvedSchema? = null,
    val entry_schema: ResolvedSchema? = null,
    val externalSchema: String? = null
) {
    companion object {
        fun from(
            propertyDefinition: PropertyDefinition,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedProperty {
            return propertyDefinition.run {
                type
                    ?: throw IllegalArgumentException("Properties must declare a type if they do not refine an existing Property")
                ResolvedProperty(
                    propertyResolverContext.dataTypeResolver(type),
                    required ?: true,
                    default?.resolve(propertyResolverContext.expressionResolverContext),
                    constraints.orEmpty(),
                    key_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) },
                    entry_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) },
                    externalSchema
                )
            }
        }
    }
}

data class PropertyResolverContext(
    val dataTypeResolver: TypeResolver<ResolvedDataType>,
    val expressionResolverContext: ExpressionResolverContext
)

fun ResolvedProperty.refine(
    propertyDefinition: PropertyDefinition,
    propertyResolverContext: PropertyResolverContext
): ResolvedProperty {
    val dataType = propertyDefinition.type?.let {
        val dataType = propertyResolverContext.dataTypeResolver(propertyDefinition.type)
        if (!isCompatibleType(type, dataType)) {
            throw IllegalArgumentException("Property refinements must have a compatible type")
        }
        dataType
    }
    val type = dataType ?: type
    val default = propertyDefinition.default?.resolve(propertyResolverContext.expressionResolverContext) ?: default
    val constraints = constraints + propertyDefinition.constraints.orEmpty()
    if (required && propertyDefinition.required == false) {
        throw IllegalArgumentException("Required property can not be turned into an optional property.")
    }
    val required = required || propertyDefinition.required == true

    //TODO schemas
    return copy(type = type, default = default, constraints = constraints, required = required)
}

/**
 * 3.6.10.6 Refining Property Definitions
 */
fun Map<String, ResolvedProperty>.refine(
    properties: Map<String, PropertyDefinition>,
    propertyResolverContext: PropertyResolverContext
): Map<String, ResolvedProperty> {
    return refineOrAdd(
        properties,
        { refine(it, propertyResolverContext) },
        { ResolvedProperty.from(it, propertyResolverContext) })
}
