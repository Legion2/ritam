package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import org.eclipse.microprofile.openapi.annotations.media.Schema

interface Resource<T, S> {
    val metadata: Metadata
    val spec: T
    val status: S?
}

@Schema(
    example = """
    {
        "name": "my-resource",
        "labels": {
            "project": "demo"
        }
    }
"""
)
@optics
data class Metadata(
        val name: String,
        val labels: Map<String, String> = emptyMap(),
        val finalizers: Set<String> = emptySet(),
        val deletion: Boolean = false,
        val parentRef: String? = null,
        val resourceVersion: String? = null
) {
    companion object
}
