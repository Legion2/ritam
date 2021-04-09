package io.github.legion2.tosca_orchestrator.tosca.definitions.datatypes

data class Credential(
    val protocol: String?,
    val token_type: String = "password",
    val token: String,
    val keys: Map<String, String>?,
    val user: String?
)
