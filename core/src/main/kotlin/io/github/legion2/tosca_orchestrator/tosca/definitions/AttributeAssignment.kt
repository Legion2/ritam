package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = AttributeAssignmentDeserializer::class)
data class AttributeAssignment(val value: Expression, val description: String? = null)

private data class AttributeAssignmentDeserializable(val value: Expression, val description: String? = null)

class AttributeAssignmentDeserializer : JsonDeserializer<AttributeAssignment>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): AttributeAssignment {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT, JsonToken.FIELD_NAME -> {
                val mapper = jsonParser.codec as ObjectMapper
                val root: TreeNode = mapper.readTree(jsonParser)
                try {
                    mapper.convertValue(root, AttributeAssignmentDeserializable::class.java).run {
                        AttributeAssignment(value, description)
                    }
                } catch (e: IllegalArgumentException) {
                    AttributeAssignment(mapper.convertValue(root, Expression::class.java))
                }
            }
            else -> AttributeAssignment(jsonParser.readValueAs(Expression::class.java))
        }
    }
}
