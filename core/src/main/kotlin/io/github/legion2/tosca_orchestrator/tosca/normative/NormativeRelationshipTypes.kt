package io.github.legion2.tosca_orchestrator.tosca.normative

val relationshipTypeShortNameAliases = mapOf(
    "DependsOn" to "tosca.relationships.DependsOn",
    "HostedOn" to "tosca.relationships.HostedOn",
    "ConnectsTo" to "tosca.relationships.ConnectsTo",
    "AttachesTo" to "tosca.relationships.AttachesTo",
    "RoutesTo" to "tosca.relationships.RoutesTo"
)

enum class NormativeRelationshipStates(val value: String) {
    INITIAL("initial")
}
