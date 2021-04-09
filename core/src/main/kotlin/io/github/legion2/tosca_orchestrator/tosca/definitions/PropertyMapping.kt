package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class PropertyMapping @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val mapping: List<String>)



