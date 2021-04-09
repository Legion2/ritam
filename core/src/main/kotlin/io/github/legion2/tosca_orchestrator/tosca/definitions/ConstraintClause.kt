package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

data class ConstraintClause(
    val equal: Any? = null,
    val greater_than: Any? = null,
    val greater_or_equal: Any? = null,
    val less_than: Any? = null,
    val less_or_equal: Any? = null,
    val in_range: Range? = null,
    val valid_values: List<Any>? = null,
    val length: Int? = null,
    val min_length: Int? = null,
    val max_length: Int? = null,
    val pattern: String? = null,
    val schema: String? = null
)

class ConstraintClauseDeserializer : JsonDeserializer<ConstraintClause>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): ConstraintClause {
        return when (jsonParser.currentToken) {
            JsonToken.START_OBJECT -> deserializationContext.readValue(jsonParser, ConstraintClause::class.java)
            JsonToken.VALUE_STRING -> ConstraintClause(jsonParser.valueAsString)
            JsonToken.VALUE_NUMBER_INT -> ConstraintClause(jsonParser.valueAsInt)
            JsonToken.VALUE_NUMBER_FLOAT -> ConstraintClause(jsonParser.valueAsDouble)
            JsonToken.VALUE_FALSE -> ConstraintClause(jsonParser.valueAsBoolean)
            JsonToken.VALUE_TRUE -> ConstraintClause(jsonParser.valueAsBoolean)
            else -> deserializationContext.handleUnexpectedToken(
                ConstraintClause::class.java,
                jsonParser
            ) as ConstraintClause
        }
    }
}
