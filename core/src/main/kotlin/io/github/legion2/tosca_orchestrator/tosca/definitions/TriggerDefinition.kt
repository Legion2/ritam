package io.github.legion2.tosca_orchestrator.tosca.definitions

import io.github.legion2.tosca_orchestrator.tosca.definitions.datatypes.TimeInterval

data class TriggerDefinition(
    val description: String?,
    val event: String,
    val schedule: TimeInterval,
    val target_filter: EventFilterDefinition?,
    val condition: ConditionClauseDefinition?,
    val action: List<ActivityDefinition>
)
