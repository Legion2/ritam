package io.github.legion2.tosca_orchestrator.tosca.definitions

//TODO
data class ConditionClauseDefinition(
    val and: List<ConditionClauseDefinition>?,
    val or: List<ConditionClauseDefinition>?,
    val not: List<ConditionClauseDefinition>?
)