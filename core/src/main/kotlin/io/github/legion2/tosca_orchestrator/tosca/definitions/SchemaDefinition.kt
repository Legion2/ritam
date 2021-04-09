package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class SchemaDefinition(
    val type: String,
    val description: String? = null,
    @JsonDeserialize(contentUsing = ConstraintClauseDeserializer::class) val constraints: List<ConstraintClause>? = null,
    val key_schema: SchemaDefinition? = null,
    val entry_schema: SchemaDefinition? = null
)
