package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import java.net.URI

data class ImportDefinition @JsonCreator(mode = JsonCreator.Mode.PROPERTIES) constructor(
    val file: URI,
    val repository: String?,
    val namespace_prefix: String?,
    val namespace_uri: URI?
) {
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    private constructor(file: String) : this(URI.create(file))

    constructor(file: URI) : this(file, null, null, null)
}
