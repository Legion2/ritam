package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

data class OperationImplementationDefinition(
    val primary: ArtifactDefinitionOrName? = null,
    val dependencies: List<ArtifactDefinitionOrName>? = null,
    val timeout: Int? = null,
    val operation_host: String? = null
)

class OperationImplementationDefinitionDeserializer : JsonDeserializer<OperationImplementationDefinition>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): OperationImplementationDefinition {
        return when (jsonParser.currentToken) {
            JsonToken.START_OBJECT -> deserializationContext.readValue(
                jsonParser,
                OperationImplementationDefinition::class.java
            )
            JsonToken.VALUE_STRING -> OperationImplementationDefinition(
                ArtifactDefinitionOrName.ArtifactNameOrFileURI(
                    jsonParser.valueAsString
                )
            )
            else -> deserializationContext.handleUnexpectedToken(
                OperationImplementationDefinition::class.java,
                jsonParser
            ) as OperationImplementationDefinition
        }
    }
}

@JsonDeserialize(using = ArtifactDefinitionOrStringDeserializer::class)
sealed class ArtifactDefinitionOrName {
    @JsonDeserialize(`as` = Definition::class)
    data class Definition @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val definition: ArtifactDefinition.Definition) :
        ArtifactDefinitionOrName()

    data class ArtifactNameOrFileURI(val name: String) : ArtifactDefinitionOrName()
}

class ArtifactDefinitionOrStringDeserializer : JsonDeserializer<ArtifactDefinitionOrName>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): ArtifactDefinitionOrName {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT -> deserializationContext.readValue(
                jsonParser,
                ArtifactDefinitionOrName.Definition::class.java
            )
            JsonToken.VALUE_STRING -> ArtifactDefinitionOrName.ArtifactNameOrFileURI(jsonParser.valueAsString)
            else -> deserializationContext.handleUnexpectedToken(
                ArtifactDefinitionOrName::class.java,
                jsonParser
            ) as ArtifactDefinitionOrName
        }
    }
}
