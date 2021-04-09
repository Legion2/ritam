package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class PropertyDefinition(
    val type: String?,
    val description: String? = null,
    val required: Boolean? = null,
    val default: Expression? = null,
    val status: Status = Status.SUPPORTED,
    @JsonDeserialize(contentUsing = ConstraintClauseDeserializer::class) val constraints: List<ConstraintClause>? = null,
    val key_schema: SchemaDefinition? = null,
    val entry_schema: SchemaDefinition? = null,
    @JsonProperty("external-schema")
    val externalSchema: String? = null,
    val metadata: Map<String, String>? = null
)
