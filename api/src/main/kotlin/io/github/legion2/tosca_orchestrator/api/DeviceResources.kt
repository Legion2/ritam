package io.github.legion2.tosca_orchestrator.api

import io.github.legion2.tosca_orchestrator.orchestrator.model.Device
import io.github.legion2.tosca_orchestrator.orchestrator.model.DeviceResource
import io.github.legion2.tosca_orchestrator.orchestrator.model.DeviceStatus
import io.github.legion2.tosca_orchestrator.orchestrator.storage.DeviceStorage
import org.eclipse.microprofile.openapi.annotations.tags.Tag
import javax.ws.rs.Path

@Tag(name = "devices")
@Path("/api/resources/devices")
class DeviceResources : GenericResources<Device, DeviceStatus, DeviceResource, DeviceStorage>()