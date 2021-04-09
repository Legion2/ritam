package io.github.legion2.tosca_orchestrator.client

import io.github.legion2.tosca_orchestrator.orchestrator.model.Application
import io.github.legion2.tosca_orchestrator.orchestrator.model.ApplicationResource
import io.github.legion2.tosca_orchestrator.orchestrator.model.ApplicationStatus
import javax.ws.rs.Path

@Path("/api/resources/applications")
interface DeviceApplicationClient : GenericClient<Application, ApplicationStatus, ApplicationResource>