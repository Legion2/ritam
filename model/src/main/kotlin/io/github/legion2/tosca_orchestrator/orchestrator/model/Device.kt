package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import java.net.URI
import java.time.OffsetDateTime

@optics
data class Device(val properties: Map<String, String>, val url: URI) {
    companion object
}

@optics
data class DeviceStatus(
        val message: String,
        val lastReconciled: OffsetDateTime,
) {
    companion object
}

@optics
data class DeviceResource(
        override val metadata: Metadata,
        override val spec: Device,
        override val status: DeviceStatus? = null
) : Resource<Device, DeviceStatus> {
    companion object
}
