package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class RequirementAssignment @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    val capability: String? = null,
    val node: String? = null,
    val relationship: Relationship? = null,
    val node_filter: NodeFilter? = null,
    val occurrences: Range? = null
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(node: String) : this(null, node)
}

@JsonDeserialize(using = RelationshipDeserializer::class)
sealed class Relationship {
    data class RelationshipNameOrType(val value: String) : Relationship()

    @JsonDeserialize(`as` = RelationshipOverride::class)
    data class RelationshipOverride(
        val type: String?,
        val properties: Map<String, PropertyAssignment>?,
        val interfaces: Map<String, InterfaceAssignment>?
    ) : Relationship()
}


class RelationshipDeserializer : JsonDeserializer<Relationship>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Relationship {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT -> jsonParser.readValueAs(Relationship.RelationshipOverride::class.java)
            JsonToken.VALUE_STRING -> Relationship.RelationshipNameOrType(jsonParser.valueAsString)
            else -> throw deserializationContext.wrongTokenException(
                jsonParser,
                Relationship::class.java,
                JsonToken.VALUE_STRING,
                "Expected either String or Object"
            )
        }
    }
}
