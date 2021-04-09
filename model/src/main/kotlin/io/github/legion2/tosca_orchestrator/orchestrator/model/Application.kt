package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import java.net.URI
import java.time.OffsetDateTime

@optics
data class ApplicationResource(
    override val metadata: Metadata,
    override val spec: Application,
    override val status: ApplicationStatus? = null
) : Resource<Application, ApplicationStatus> {
    companion object
}

@optics
data class Application(
    val serviceTemplate: URI,
    val inputs: Map<String, String> = emptyMap()
) {
    companion object
}

@optics
data class ApplicationStatus(
        val message: String,
        val lastReconciled: OffsetDateTime = OffsetDateTime.now(),
        val nodes: Map<String, String> = emptyMap(),
        val outputs: Map<String, String> = emptyMap()
) {
    companion object
}
