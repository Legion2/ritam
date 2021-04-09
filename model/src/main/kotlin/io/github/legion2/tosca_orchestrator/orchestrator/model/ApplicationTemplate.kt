package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import org.eclipse.microprofile.openapi.annotations.media.Schema
import java.net.URI
import java.time.OffsetDateTime

@Schema(example = """
    {
        "deviceSelector": {
            "location": "office",
            "type": "raspberry-pi"
        },
        "inputs": {
            "interval": "10"
        },
        "serviceTemplate": ""
    }
""")
@optics
data class ApplicationTemplate(
    val deviceSelector: Map<String, String>,
    val inputs: Map<String, String>,
    val serviceTemplate: URI
) {
    companion object
}

@Schema(example = """
    {
        "message": "Successfully reconciled",
        "instances": 3,
        "devices": {
            "pi-1": "creating",
            "pi-3": "updated",
            "pi-4": "running"
        }
    }
""")
@optics
data class ApplicationTemplateStatus(
        val message: String,
        val lastReconciled: OffsetDateTime,
        val instances: Int,
        val devices: Map<String, String> = emptyMap(),
) {
    companion object
}

@optics
data class ApplicationTemplateResource(
    override val metadata: Metadata,
    override val spec: ApplicationTemplate,
    override val status: ApplicationTemplateStatus? = null
) : Resource<ApplicationTemplate, ApplicationTemplateStatus> {
    companion object
}
