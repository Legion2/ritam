package io.github.legion2.tosca_orchestrator.tosca.definitions.type

import io.github.legion2.tosca_orchestrator.tosca.definitions.PropertyDefinition

data class ArtifactType(
    val derived_from: String,
    val version: String?,
    val metadata: Map<String, String>?,
    val description: String?,
    val mime_type: String?,
    val file_ext: List<String>?,
    val properties: Map<String, PropertyDefinition>?
)
