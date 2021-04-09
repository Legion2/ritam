package io.github.legion2.tosca_orchestrator.tosca.model.resolved.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.definitions.type.DataType
import io.github.legion2.tosca_orchestrator.tosca.model.property.PrimitiveType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.PropertyResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedSchema
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.refine

data class ResolvedDataType(
    val name: String,
    val primitiveType: PrimitiveType,
    val derivedFrom: ResolvedDataType? = null,
    val constraints: List<ConstraintClause> = emptyList(),
    val properties: Map<String, ResolvedProperty> = emptyMap(),
    val key_schema: ResolvedSchema? = null,
    val entry_schema: ResolvedSchema? = null
) {
    companion object {
        fun resolve(
            name: String,
            dataType: DataType,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedDataType {
            val baseType = propertyResolverContext.dataTypeResolver(dataType.derivedFrom)

            if (baseType.primitiveType != PrimitiveType.COMPLEX && !dataType.properties.isNullOrEmpty()) {
                throw IllegalArgumentException("Only a complex Data Type can have properties.")
            }
            val properties = baseType.properties.refine(dataType.properties.orEmpty(), propertyResolverContext)
            val constraints = baseType.constraints + dataType.constraints.orEmpty()
            if (dataType.key_schema != null && baseType.key_schema != null) {
                throw IllegalArgumentException("key_schema can not be refined by a subtype: $dataType")
            }
            if (baseType.primitiveType != PrimitiveType.MAP && dataType.key_schema != null) {
                throw IllegalArgumentException("Only a map Data Type can have a key_schema")
            }
            if (dataType.entry_schema != null && baseType.entry_schema != null) {
                throw IllegalArgumentException("entry_schema can not be refined by a subtype: $dataType")
            }
            if (baseType.primitiveType != PrimitiveType.MAP && baseType.primitiveType != PrimitiveType.LIST && dataType.key_schema != null) {
                throw IllegalArgumentException("Only a map or list Data Type can have an entry_schema")
            }
            val entrySchema =
                dataType.entry_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) }
                    ?: baseType.entry_schema
            val keySchema =
                dataType.key_schema?.let { ResolvedSchema.from(it, propertyResolverContext.dataTypeResolver) }
                    ?: baseType.key_schema

            return ResolvedDataType(
                name,
                baseType.primitiveType,
                baseType,
                constraints,
                properties,
                keySchema,
                entrySchema
            )
        }
    }
}

tailrec fun isCompatibleType(base: ResolvedDataType, subDataType: ResolvedDataType): Boolean {
    if (base == subDataType) {
        return true
    }
    if (subDataType.derivedFrom == null) {
        return false
    }
    return isCompatibleType(base, subDataType.derivedFrom)
}
