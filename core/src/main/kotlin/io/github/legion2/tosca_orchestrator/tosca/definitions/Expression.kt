package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.TreeNode
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import com.fasterxml.jackson.databind.util.StdConverter

@JsonDeserialize(using = ExpressionDeserializer::class)
sealed class Expression {
    @JsonDeserialize(using = JsonDeserializer.None::class)
    sealed class Literal : Expression() {
        data class String(@JsonValue val value: kotlin.String) : Literal()
        data class Boolean(@JsonValue val value: kotlin.Boolean) : Literal()
        data class Float(@JsonValue val value: Double) : Literal()
        data class Integer(@JsonValue val value: Int) : Literal()
        data class Map @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: kotlin.collections.Map<kotlin.String, Expression>) :
            Literal()

        data class List @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: kotlin.collections.List<Expression>) :
            Literal()
    }

    @JsonDeserialize(using = FunctionDeserializer::class)
    sealed class Function : Expression() {
        data class Concat(@JsonProperty("concat") val string_value_expressions: List<Expression>) : Function()
        data class Join(val string_value_expressions: List<Expression>, val delimiter: String?) : Function()
        data class Token(
            val string_with_token: Expression,
            val string_of_token_chars: String,
            val substring_index: Int
        ) : Function()

        data class GetInput(val property_name: List<PropertyNameOrIndex>) : Function()
        data class GetProperty(
            val entity_name: EntityName,
            val req_or_cap_name: String?,
            val property_name: List<PropertyNameOrIndex>
        ) : Function()

        data class GetAttribute(
            val entity_name: EntityName,
            val req_or_cap_name: String?,
            val attribute_name: List<PropertyNameOrIndex>
        ) : Function()

        data class GetOperationOutput(
            val entity_name: EntityName,
            val interface_name: String,
            val operation_name: String,
            val output_variable_name: String
        ) : Function()

        data class GetNodesOfType(@JsonProperty("get_node_of_type") val node_type_name: String) : Function()
        data class GetArtifact(
            val entity_name: EntityName,
            val artifact_name: String,
            val location: Location?,
            val remove: Boolean?
        ) : Function()
    }
}

sealed class PropertyNameOrIndex {
    data class PropertyName(@JsonValue val name: String) : PropertyNameOrIndex()
    data class PropertyIndex(@JsonValue val index: Int) : PropertyNameOrIndex()

    companion object {
        @JvmStatic
        @JsonCreator
        fun from(name: String) = PropertyName(name)

        @JvmStatic
        @JsonCreator
        fun from(index: Int) = PropertyIndex(index)
    }
}

@JsonDeserialize(converter = StringToEntityNameConverter::class)
@JsonSerialize(converter = EntityNameToStringConverter::class)
sealed class EntityName {
    data class ModelableEntityName(val name: String) : EntityName()
    object SELF : EntityName()
    object SOURCE : EntityName()
    object TARGET : EntityName()
    object HOST : EntityName()
}

@JsonDeserialize(converter = StringToLocationConverter::class)
@JsonSerialize(converter = LocationToStringConverter::class)
sealed class Location {
    data class Path(val path: String) : Location()
    object LOCAL_FILE : Location()
}

class ExpressionDeserializer : JsonDeserializer<Expression>() {
    override fun deserialize(jsonParser: JsonParser, deserializationContext: DeserializationContext): Expression {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT, JsonToken.FIELD_NAME -> {
                val mapper = jsonParser.codec as ObjectMapper
                val root: TreeNode = mapper.readTree(jsonParser)
                return try {
                    mapper.convertValue(root, Expression.Function::class.java)
                } catch (e: IllegalArgumentException) {
                    mapper.convertValue(root, Expression.Literal.Map::class.java)
                }
            }
            JsonToken.VALUE_STRING -> Expression.Literal.String(jsonParser.valueAsString)
            JsonToken.VALUE_TRUE -> Expression.Literal.Boolean(jsonParser.valueAsBoolean)
            JsonToken.VALUE_FALSE -> Expression.Literal.Boolean(jsonParser.valueAsBoolean)
            JsonToken.VALUE_NUMBER_FLOAT -> Expression.Literal.Float(jsonParser.valueAsDouble)
            JsonToken.VALUE_NUMBER_INT -> Expression.Literal.Integer(jsonParser.valueAsInt)
            JsonToken.START_ARRAY -> jsonParser.readValueAs(Expression.Literal.List::class.java)
            JsonToken.END_OBJECT -> deserializationContext.handleUnexpectedToken(
                Expression::class.java,
                jsonParser
            ) as Expression
            else -> deserializationContext.handleUnexpectedToken(Expression::class.java, jsonParser) as Expression
        }
    }
}


class FunctionDeserializer : JsonDeserializer<Expression.Function>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): Expression.Function {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT, JsonToken.FIELD_NAME -> {
                val field =
                    if (jsonParser.isExpectedStartObjectToken) jsonParser.nextFieldName() else jsonParser.currentName()
                jsonParser.nextToken()
                val expression = when (field) {
                    "concat" -> parseConcat(jsonParser)
                    "join" -> parseJoin(deserializationContext)
                    "token" -> parseToken(deserializationContext)
                    "get_input" -> parseGetInput(jsonParser)
                    "get_property" -> parseGetProperty(jsonParser, deserializationContext)
                    "get_attribute" -> parseGetAttribute(jsonParser, deserializationContext)
                    "get_operation_output" -> parseGetOperationOutput(deserializationContext)
                    "get_node_of_type" -> parseGetNodeOfType(jsonParser)
                    "get_artifact" -> parseGetArtifact(deserializationContext)
                    else -> deserializationContext.reportInputMismatch<Expression.Function>(
                        Expression.Function::class.java,
                        "The specified function '$field' does not exist"
                    )
                }
                if (jsonParser.nextToken() !== JsonToken.END_OBJECT) {
                    deserializationContext.reportWrongTokenException(
                        expression::class.java,
                        JsonToken.END_OBJECT,
                        "Expected end of Expression"
                    )
                }
                expression
            }
            JsonToken.END_OBJECT -> deserializationContext.handleUnexpectedToken(
                Expression::class.java,
                jsonParser
            ) as Expression.Function
            else -> deserializationContext.handleUnexpectedToken(
                Expression.Function::class.java,
                jsonParser
            ) as Expression.Function
        }
    }

    private fun parseGetArtifact(deserializationContext: DeserializationContext): Expression.Function.GetArtifact {
        with(deserializationContext) {
            if (parser.currentToken() !== JsonToken.START_ARRAY) {
                reportWrongTokenException(
                    Expression.Function.Join::class.java,
                    JsonToken.START_ARRAY,
                    "Expected start of Array"
                )
            }
            parser.nextToken()
            val entityName = parser.readValueAs(EntityName::class.java)
            if (parser.nextToken() == JsonToken.END_ARRAY) {
                reportWrongTokenException(
                    Expression.Function.Join::class.java,
                    JsonToken.END_ARRAY,
                    "Unexpected end of Array"
                )
            }
            val artifactName = parser.valueAsString
            val location = if (parser.nextToken() != JsonToken.END_ARRAY) {
                parser.readValueAs(Location::class.java)
            } else {
                return Expression.Function.GetArtifact(entityName, artifactName, null, null)
            }
            //TODO location can be not given but remove
            val remove = if (parser.nextToken() != JsonToken.END_ARRAY) {
                parser.valueAsBoolean
            } else {
                return Expression.Function.GetArtifact(entityName, artifactName, location, null)
            }

            if (parser.nextToken() != JsonToken.END_ARRAY) {
                reportWrongTokenException(
                    Expression.Function.Join::class.java,
                    JsonToken.END_ARRAY,
                    "Expected end of Array"
                )
            }

            return Expression.Function.GetArtifact(entityName, artifactName, location, remove)
        }
    }

    private fun parseGetNodeOfType(jsonParser: JsonParser): Expression.Function.GetNodesOfType {
        return Expression.Function.GetNodesOfType(jsonParser.valueAsString)
    }

    private fun parseGetOperationOutput(deserializationContext: DeserializationContext): Expression.Function.GetOperationOutput {
        val array = deserializationContext.parseArrayAs(
            { it.readValueAs(EntityName::class.java) },
            { it.valueAsString },
            { it.valueAsString },
            { it.valueAsString })
        return Expression.Function.GetOperationOutput(
            array[0] as EntityName,
            array[1] as String,
            array[2] as String,
            array[3] as String
        )
    }

    private fun parseGetAttribute(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): Expression.Function.GetAttribute {
        val temp = parseGetProperty(jsonParser, deserializationContext)
        return Expression.Function.GetAttribute(temp.entity_name, temp.req_or_cap_name, temp.property_name)
    }

    private fun parseGetProperty(
        parser: JsonParser,
        deserializationContext: DeserializationContext
    ): Expression.Function.GetProperty {
        if (parser.currentToken() !== JsonToken.START_ARRAY) {
            deserializationContext.reportWrongTokenException(
                Expression.Function.Join::class.java,
                JsonToken.START_ARRAY,
                "Expected end of Array"
            )
        }

        if (parser.nextToken() == JsonToken.END_ARRAY) {
            deserializationContext.reportWrongTokenException(
                Expression.Function.Join::class.java,
                JsonToken.NOT_AVAILABLE,
                "Did not expect end of Array, are you missing some values?"
            )
        }
        val entityName = parser.readValueAs(EntityName::class.java)
        if (parser.nextToken() == JsonToken.END_ARRAY) {
            deserializationContext.reportWrongTokenException(
                Expression.Function.Join::class.java,
                JsonToken.NOT_AVAILABLE,
                "Did not expect end of Array, are you missing some values?"
            )
        }
        var reqOrCapName: String? = null
        var endToken = false
        val propertyName = if (parser.currentToken() == JsonToken.START_ARRAY) {
            parser.readValueAs<List<PropertyNameOrIndex>>(object : TypeReference<List<PropertyNameOrIndex>>() {})
        } else {
            val temp = parser.readValueAs(PropertyNameOrIndex::class.java)

            when (parser.nextToken()) {
                JsonToken.START_ARRAY -> {
                    reqOrCapName = (temp as PropertyNameOrIndex.PropertyName).name
                    parser.readValueAs<List<PropertyNameOrIndex>>(object :
                        TypeReference<List<PropertyNameOrIndex>>() {})
                }
                JsonToken.VALUE_STRING -> {
                    reqOrCapName = (temp as PropertyNameOrIndex.PropertyName).name
                    listOf(parser.readValueAs(PropertyNameOrIndex::class.java))
                }
                JsonToken.END_ARRAY -> {
                    endToken = true
                    listOf(temp)
                }
                else -> deserializationContext.reportInputMismatch(this, "Expected end of Array")
            }
        }

        if (!endToken && parser.nextToken() !== JsonToken.END_ARRAY) {
            deserializationContext.reportWrongTokenException(
                Expression.Function.Join::class.java,
                JsonToken.END_ARRAY,
                "Expected end of Array"
            )
        }

        return Expression.Function.GetProperty(entityName, reqOrCapName, propertyName)
    }

    private fun parseGetInput(jsonParser: JsonParser): Expression.Function.GetInput {
        val names = if (jsonParser.currentToken() == JsonToken.START_ARRAY) {
            jsonParser.readValueAs<List<PropertyNameOrIndex>>(object : TypeReference<List<PropertyNameOrIndex>>() {})
        } else {
            listOf(jsonParser.readValueAs(PropertyNameOrIndex::class.java))
        }

        return Expression.Function.GetInput(names)
    }

    private fun parseToken(deserializationContext: DeserializationContext): Expression.Function.Token {
        val array = deserializationContext.parseArrayAs({ it.readValueAs(Expression::class.java) },
            { it.valueAsString },
            { it.numberValue })
        return Expression.Function.Token(array[0] as Expression, array[1] as String, array[2] as Int)
    }

    private fun parseConcat(jsonParser: JsonParser): Expression.Function.Concat {
        val expressions = jsonParser.readValueAs<List<Expression>>(object : TypeReference<List<Expression>>() {})
        return Expression.Function.Concat(expressions)
    }

    private fun parseJoin(deserializationContext: DeserializationContext): Expression.Function.Join {
        with(deserializationContext) {
            if (parser.currentToken() !== JsonToken.START_ARRAY) {
                reportWrongTokenException(
                    Expression.Function.Join::class.java,
                    JsonToken.START_ARRAY,
                    "Expected end of Array"
                )
            }
            parser.nextToken()
            val list = parser.readValueAs<List<Expression>>(object : TypeReference<List<Expression>>() {})
            val delimiter = if (parser.nextToken() != JsonToken.END_ARRAY) {
                parser.valueAsString.also {
                    if (parser.nextToken() != JsonToken.END_ARRAY) {
                        reportWrongTokenException(
                            Expression.Function.Join::class.java,
                            JsonToken.END_ARRAY,
                            "Expected end of Array"
                        )
                    }
                }
            } else null

            return Expression.Function.Join(list, delimiter)
        }
    }
}

fun <T> DeserializationContext.parseArrayAs(vararg deserializer: (jsonParser: JsonParser) -> T): List<T> {
    if (parser.currentToken() !== JsonToken.START_ARRAY) {
        reportWrongTokenException(Expression.Function.Join::class.java, JsonToken.START_ARRAY, "Expected end of Array")
    }
    val array = deserializer.map {
        if (parser.nextToken() == JsonToken.END_ARRAY) {
            reportWrongTokenException(
                Expression.Function.Join::class.java,
                JsonToken.NOT_AVAILABLE,
                "Did not expect end of Array, are you missing some values?"
            )
        }
        it.invoke(parser)
    }

    if (parser.nextToken() !== JsonToken.END_ARRAY) {
        reportWrongTokenException(Expression.Function.Join::class.java, JsonToken.END_ARRAY, "Expected end of Array")
    }
    return array
}

class EntityNameToStringConverter : StdConverter<EntityName, String>() {
    override fun convert(value: EntityName): String {
        return when (value) {
            is EntityName.ModelableEntityName -> value.name
            EntityName.HOST -> "HOST"
            EntityName.SELF -> "SELF"
            EntityName.SOURCE -> "SOURCE"
            EntityName.TARGET -> "TARGET"
        }
    }
}

class StringToEntityNameConverter : StdConverter<String, EntityName>() {
    override fun convert(value: String): EntityName {
        return when (value) {
            "HOST" -> EntityName.HOST
            "SELF" -> EntityName.SELF
            "SOURCE" -> EntityName.SOURCE
            "TARGET" -> EntityName.TARGET
            else -> EntityName.ModelableEntityName(value)
        }
    }
}

class LocationToStringConverter : StdConverter<Location, String>() {
    override fun convert(value: Location): String {
        return when (value) {
            is Location.Path -> value.path
            Location.LOCAL_FILE -> "LOCAL_FILE"
        }
    }
}

class StringToLocationConverter : StdConverter<String, Location>() {
    override fun convert(value: String): Location {
        return when (value) {
            "LOCAL_FILE" -> Location.LOCAL_FILE
            else -> Location.Path(value)
        }
    }
}