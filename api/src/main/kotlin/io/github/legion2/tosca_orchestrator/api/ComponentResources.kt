package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.*
import io.github.legion2.tosca_orchestrator.orchestrator.storage.ComponentStorage
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import javax.ws.rs.Path

@Tag(name = "components")
@Path("/api/resources/components")
class ComponentResources : GenericResources<Component, ComponentStatus, ComponentResource, ComponentStorage>()