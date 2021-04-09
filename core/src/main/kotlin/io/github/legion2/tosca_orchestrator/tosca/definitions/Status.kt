package io.github.legion2.tosca_orchestrator.tosca.definitions

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Status @JsonCreator constructor(@JsonValue val status: String) {
    SUPPORTED("supported"),
    UNSUPPORTED("unsupported"),
    EXPERIMENTAL("experimental"),
    DEPRECATED("deprecated")
}