package io.github.legion2.tosca_orchestrator.client

import io.github.legion2.tosca_orchestrator.orchestrator.model.Resource
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
interface GenericClient<T, S, R : Resource<T, S>> {
    @GET
    fun getResources(): List<R>

    @GET
    @Path("{name}")
    fun getResource(@PathParam("name") name: String): R

    @POST
    @Path("{name}")
    fun createResource(@PathParam("name") name: String, resource: R): R

    @PUT
    @Path("{name}")
    fun updateResource(@PathParam("name") name: String, resource: R): R

    @DELETE
    @Path("{name}")
    @APIResponse(responseCode = "204", description = "Resource marked for deletion")
    fun deleteResource(@PathParam("name") name: String)
}
