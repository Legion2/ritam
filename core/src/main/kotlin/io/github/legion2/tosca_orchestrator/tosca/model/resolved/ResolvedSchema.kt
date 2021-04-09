package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.definitions.SchemaDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.TypeResolver
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedDataType

data class ResolvedSchema(
    val type: ResolvedDataType,
    val constraints: List<ConstraintClause> = emptyList(),
    val key_schema: ResolvedSchema? = null,
    val entry_schema: ResolvedSchema? = null
) {
    companion object {
        fun from(schemaDefinition: SchemaDefinition, dataTypeResolver: TypeResolver<ResolvedDataType>): ResolvedSchema {
            val type = dataTypeResolver(schemaDefinition.type)
            val keySchema = schemaDefinition.key_schema?.let { from(it, dataTypeResolver) }
            val entrySchema = schemaDefinition.entry_schema?.let { from(it, dataTypeResolver) }
            return ResolvedSchema(type, schemaDefinition.constraints.orEmpty(), keySchema, entrySchema)
        }
    }
}
