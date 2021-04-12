package io.github.legion2.tosca_orchestrator.cli

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.kotlin.treeToValue
import com.github.ajalt.clikt.core.CliktError
import com.github.fge.jsonpatch.mergepatch.JsonMergePatch
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import java.io.InputStream
import java.io.StringWriter
import java.net.ConnectException
import java.net.URI

class RitamClient(private val uri: URI) {

    private val client = HttpClient(Java) {
        install(JsonFeature)
        addDefaultResponseValidation()
        HttpResponseValidator {
            handleResponseException { exception ->
                when (exception) {
                    is ResponseException -> throw CliktError(
                        exception.response.call.run { "API server can not process request: ${request.method.value} ${request.url} -> ${response.status.value}\n${response.receive<String>()}" },
                        exception
                    )
                    is ConnectException -> throw CliktError("Can not send request to API server.", exception)
                }
            }
        }
    }

    fun getClient(resourcePath: String): ResourceClient {
        return ResourceClient(client, uri, resourcePath)
    }
}

data class Resource(val metadata: Map<String, Any>, val spec: Any, val status: Any?)

val Resource.name: String get() = metadata.getValue("name") as String

val objectMapper by lazy {
    YAMLMapper(
        YAMLFactory()
            .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
            .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
    ).apply {
        findAndRegisterModules()
        setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}

val Iterable<Resource>.echoString: String
    get():String = StringWriter().use { stringWriter ->
        objectMapper.writer().writeValues(stringWriter).writeAll(this)
        stringWriter.toString()
    }

val Resource.echoString get(): String = objectMapper.writeValueAsString(this)

fun InputStream.readResource(): Resource {
    return objectMapper.readValue(this)
}

fun Resource.patch(otherResource: Resource): Resource {
    val jsonMergePatch = objectMapper.readValue<JsonMergePatch>(objectMapper.writeValueAsString(this))
    val jsonNode = objectMapper.readValue<JsonNode>(objectMapper.writeValueAsString(otherResource))
    val resource = objectMapper.treeToValue<Resource>(jsonMergePatch.apply(jsonNode))!!
    return resource.copy(status = otherResource.status)
}

class ResourceClient(private val client: HttpClient, private val uri: URI, private val resourcePath: String) {
    private fun url(vararg components: String): Url {
        return URLBuilder().takeFrom(uri).path(components.asList()).build()
    }

    suspend fun getAllResources(): List<Resource> = client.get(url("api", "resources", resourcePath))
    suspend fun getResource(name: String): Resource = client.get(url("api", "resources", resourcePath, name))
    suspend fun createResources(name: String, resource: Resource): Resource =
        client.post(url("api", "resources", resourcePath, name)) {
            body = resource
            contentType(ContentType.Application.Json)
        }

    suspend fun updateResources(name: String, resource: Resource): Resource =
        client.put(url("api", "resources", resourcePath, name)) {
            body = resource
            contentType(ContentType.Application.Json)
        }

    suspend fun deleteResources(name: String): Unit = client.delete(url("api", "resources", resourcePath, name))
}