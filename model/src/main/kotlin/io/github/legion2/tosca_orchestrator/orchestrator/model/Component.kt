package io.github.legion2.tosca_orchestrator.orchestrator.model

import arrow.optics.optics
import io.github.legion2.tosca_orchestrator.tosca.model.property.Value
import java.time.OffsetDateTime

/**
 * @property dependencies list of component resource names
 */
@optics
data class Component(
        val reconciler: Reconciler,
        val dependencies: List<String>,
        val bindings: Map<String, Boolean> = emptyMap(),
        val operationFinalizer: Boolean = false
) {
    companion object
}

@optics
data class ComponentStatus(
        val message: String,
        val lastReconciled: OffsetDateTime,
        val attributes: Map<String, Value> = emptyMap()) {
    companion object
}

@optics
data class ComponentResource(
        override val metadata: Metadata,
        override val spec: Component,
        override val status: ComponentStatus? = null
) : Resource<Component, ComponentStatus> {
    companion object
}
