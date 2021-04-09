package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClause
import io.github.legion2.tosca_orchestrator.tosca.definitions.ConstraintClauseDeserializer
import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.SchemaDefinition

data class DataType(
    private val derived_from: String?,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    @JsonDeserialize(contentUsing = ConstraintClauseDeserializer::class) val constraints: List<ConstraintClause>?,
    val properties: Map<String, PropertyDefinition>?,
    val key_schema: SchemaDefinition?,
    val entry_schema: SchemaDefinition?
) {
    init {
        if (derived_from == null && properties.isNullOrEmpty()) {
            throw IllegalArgumentException("A valid datatype definition MUST have either a valid derived_from declaration or at least one valid property definition.")
        }
        if (properties != null && properties.isEmpty()) {
            throw IllegalArgumentException(" If a properties keyname is provided, it SHALL contain one or more valid property definitions.")
        }
    }

    val derivedFrom get() = derived_from ?: "tosca.datatype.Root"
}