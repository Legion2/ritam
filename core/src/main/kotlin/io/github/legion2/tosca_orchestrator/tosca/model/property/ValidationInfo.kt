package io.github.legion2.tosca_orchestrator.tosca.model.property

import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttribute
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedProperty
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedSchema
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType

data class ValidationInfo(
    val primitiveType: PrimitiveType,
    val properties: Map<String, ResolvedProperty>,
    val constraints: List<ConstraintClause> = emptyList(),
    val key_schema: ResolvedSchema? = null,
    val entry_schema: ResolvedSchema? = null
)

fun ResolvedProperty.toValidationInfo(): ValidationInfo {
    val resolvedType = type
    val entrySchema = entry_schema ?: resolvedType.entry_schema
    val keySchema = key_schema ?: resolvedType.key_schema
    return ValidationInfo(
        type.primitiveType,
        type.properties,
        resolvedType.constraints + constraints,
        keySchema,
        entrySchema
    )
}

fun ResolvedAttribute.toValidationInfo(): ValidationInfo {
    val resolvedType = type
    val entrySchema = entry_schema ?: resolvedType.entry_schema
    val keySchema = key_schema ?: resolvedType.key_schema
    return ValidationInfo(type.primitiveType, type.properties, resolvedType.constraints, keySchema, entrySchema)
}

fun ResolvedSchema.toValidationInfo(): ValidationInfo {
    val resolvedType = type
    val entrySchema = entry_schema ?: resolvedType.entry_schema
    val keySchema = key_schema ?: resolvedType.key_schema
    return ValidationInfo(
        type.primitiveType,
        type.properties,
        resolvedType.constraints + constraints,
        keySchema,
        entrySchema
    )
}

fun ResolvedDataType.toValidationInfo(): ValidationInfo {
    return ValidationInfo(primitiveType, properties, constraints, key_schema, entry_schema)
}
