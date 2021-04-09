package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedArtifact
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping
import java.time.Duration

@optics
data class Reconciler(
    val reconcile: ReconcileFunction,
    val deletion: ReconcileFunction
) {
    companion object
}

@optics
data class ReconcileFunction(
        val artifact: ResolvedArtifact? = null,
        val inputs: Map<String, DynamicExpression> = emptyMap(),
        val outputs: Map<String, ResolvedAttributeMapping> = emptyMap(),
        val timeout: Duration? = null,
        val dependencies: List<ResolvedArtifact> = emptyList(),
) {
    companion object
}

