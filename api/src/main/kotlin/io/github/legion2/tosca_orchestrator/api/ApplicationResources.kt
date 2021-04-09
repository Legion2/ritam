package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationStorage
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import javax.ws.rs.Path

@Tag(name = "applications")
@Path("/api/resources/applications")
class ApplicationResources : GenericResources<Application, ApplicationStatus, ApplicationResource, ApplicationStorage>()