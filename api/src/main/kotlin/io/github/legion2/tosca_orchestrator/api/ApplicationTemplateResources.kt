package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ApplicationTemplateStorage
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import javax.ws.rs.Path

@Tag(name = "application-templates")
@Path("/api/resources/application-templates")
class ApplicationTemplateResources : GenericResources<ApplicationTemplate, ApplicationTemplateStatus, ApplicationTemplateResource, ApplicationTemplateStorage>()