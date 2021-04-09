package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import io.github.legion2.tosca_orchestrator.tosca.definitions.datatypes.Credential

data class RepositoryDefinition
@JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
constructor(
    val description: String?,
    val url: String,
    val credential: Credential?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    constructor(url: String) : this(null, url, null)
}
