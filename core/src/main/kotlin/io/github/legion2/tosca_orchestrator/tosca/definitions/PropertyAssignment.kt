package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

data class PropertyAssignment @JsonCreator(mode = JsonCreator.Mode.DELEGATING) constructor(@JsonValue val value: Expression)
