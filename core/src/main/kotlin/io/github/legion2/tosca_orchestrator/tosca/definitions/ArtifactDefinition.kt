package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

@JsonDeserialize(using = ArtifactDefinitionDeserializer::class)
sealed class ArtifactDefinition {
    @JsonDeserialize(`as` = Definition::class)
    data class Definition(
        val type: String,
        val file: String,
        val repository: String?,
        val description: String?,
        val deploy_path: String?,
        @JsonAlias("artifact_version") val version: String?,
        val checksum: String?,
        val checksum_algorithm: String?,
        val properties: Map<String, PropertyAssignment>?
    ) : ArtifactDefinition()


    data class FileURI(val file: String) : ArtifactDefinition()
}

class ArtifactDefinitionDeserializer : JsonDeserializer<ArtifactDefinition>() {
    override fun deserialize(
        jsonParser: JsonParser,
        deserializationContext: DeserializationContext
    ): ArtifactDefinition {
        return when (jsonParser.currentToken()) {
            JsonToken.START_OBJECT -> deserializationContext.readValue(
                jsonParser,
                ArtifactDefinition.Definition::class.java
            )
            JsonToken.VALUE_STRING -> ArtifactDefinition.FileURI(jsonParser.valueAsString)
            else -> deserializationContext.handleUnexpectedToken(
                ArtifactDefinition::class.java,
                jsonParser
            ) as ArtifactDefinition
        }
    }
}
