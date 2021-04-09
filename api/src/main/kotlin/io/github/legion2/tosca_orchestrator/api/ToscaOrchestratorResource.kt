package io.github.legion2.tosca_orchestrator.api

import com.fasterxml.jackson.module.kotlin.readValue
import io.github.legion2.tosca_orchestrator.orchestrator.model.Application
import io.github.legion2.tosca_orchestrator.orchestrator.model.ApplicationResource
import io.github.legion2.tosca_orchestrator.orchestrator.model.Metadata
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationStorage
import io.github.legion2.tosca_orchestrator.tosca.definitions.ServiceTemplateDefinition
import io.github.legion2.tosca_orchestrator.tosca.definitions.toscaYamlMapper
import io.github.legion2.tosca_orchestrator.tosca.model.loadMetadata
import io.github.legion2.tosca_orchestrator.tosca.model.storeCsar
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType
import org.eclipse.microprofile.openapi.annotations.media.Content
import org.eclipse.microprofile.openapi.annotations.media.Schema
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm
import org.jboss.resteasy.annotations.providers.multipart.PartType
import java.io.InputStream
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Tag(name = "orchestrator")
@Path("/api/orchestrator")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class ToscaOrchestratorResource {

    @Inject
    protected lateinit var applicationStorage: ApplicationStorage

    @POST
    @Consumes("application/yaml")
    @Produces("application/yaml")
    @Path("validate")
    fun validate(serviceTemplate: String): String {
        val serviceTemplateYaml = toscaYamlMapper.readValue<ServiceTemplateDefinition>(serviceTemplate)
        return toscaYamlMapper.writeValueAsString(serviceTemplateYaml)
    }

    data class CsarOrchestrationRequest(
        @field:FormParam("csar")
        @field:PartType(MediaType.APPLICATION_OCTET_STREAM)
        @field:Schema(type = SchemaType.STRING, format = "binary", description = "csar file")
        val csar: InputStream = InputStream.nullInputStream()
        //@field:FormParam("inputs")
        //@field:PartType(MediaType.APPLICATION_JSON)
        //val inputs: Map<String, String> = emptyMap()
    )

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("applications/{name}/csar")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Application created"),
        APIResponse(responseCode = "400", description = "Bad input"),
        APIResponse(responseCode = "409", description = "Application already exists"),
        APIResponse(responseCode = "500", description = "CSAR could not be uploaded")
    )
    fun orchestrateCsar(
        @PathParam("name") name: String,
        @RequestBody(
            content = [Content(
                mediaType = MediaType.MULTIPART_FORM_DATA,
                schema = Schema(implementation = CsarOrchestrationRequest::class)
            )]
        )
        @MultipartForm orchestrationRequest: CsarOrchestrationRequest
    ): ApplicationResource {
        //val (csar, inputs) = orchestrationRequest
        val (csar) = orchestrationRequest
        val inputs = emptyMap<String, String>()
        val csarBasePath = try {
            storeCsar(csar)
        } catch (e: Exception) {
            throw WebApplicationException(e.message, 500)
        }
        val metaFile = try {
            loadMetadata(csarBasePath)
        } catch (e: IllegalArgumentException) {
            throw WebApplicationException(e.message, 400)
        }

        return try {
            applicationStorage.postResource(
                ApplicationResource(
                    Metadata(name, emptyMap()),
                    Application(metaFile.entryDefinitions, inputs)
                )
            )
        } catch (e: IllegalArgumentException) {
            throw WebApplicationException(e.message, 400)
        } catch (e: IllegalStateException) {
            throw WebApplicationException(e.message, 409)
        }
    }
}
