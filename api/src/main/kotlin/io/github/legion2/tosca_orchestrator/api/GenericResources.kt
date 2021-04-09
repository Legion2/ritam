package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.Resource
import io.github.legion2.tosca_orchestrator.orchestrator.storage.common.ResourceStorage
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON, "application/yaml")
@Consumes(MediaType.APPLICATION_JSON, "application/yaml")
abstract class GenericResources<T, S, R : Resource<T, S>, B : ResourceStorage<T, S, R>> {

    @Inject
    protected lateinit var storage: B

    @GET
    @APIResponse(responseCode = "200", description = "All Resources")
    fun getResources(): List<R> {
        return storage.getResources(emptyMap())
    }

    @GET
    @Path("{name}")
    @APIResponses(
        APIResponse(responseCode = "200", description = "The Resource"),
        APIResponse(responseCode = "404", description = "Resource not found")
    )
    fun getResource(@PathParam("name") name: String): R {
        return storage.getResource(name) ?: throw WebApplicationException(404)
    }

    @POST
    @Path("{name}")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Resource created"),
        APIResponse(responseCode = "400", description = "Bad input"),
        APIResponse(responseCode = "409", description = "Resource already exists")
    )
    fun createResource(@PathParam("name") name: String, resource: R): R {
        if (name != resource.metadata.name) throw WebApplicationException(400)
        try {
            return storage.postResource(resource)
        } catch (e: IllegalStateException) {
            throw WebApplicationException(e, 409)
        }
    }

    @PUT
    @Path("{name}")
    @APIResponses(
        APIResponse(responseCode = "200", description = "Resource updated"),
        APIResponse(responseCode = "400", description = "Bad input"),
        APIResponse(responseCode = "404", description = "Resource does not exists"),
        APIResponse(responseCode = "409", description = "Resource version mismatch")
    )
    fun updateResource(@PathParam("name") name: String, resource: R): R {
        if (name != resource.metadata.name) throw WebApplicationException(400)
        try {
            return storage.putResource(resource)
        } catch (e: NoSuchElementException) {
            throw WebApplicationException(e, 404)
        } catch (e: IllegalStateException) {
            throw WebApplicationException(e, 409)
        }
    }

    @DELETE
    @Path("{name}")
    @APIResponse(responseCode = "204", description = "Resource marked for deletion")
    fun deleteResource(@PathParam("name") name: String) {
        return storage.deleteResource(name)
    }
}
