package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.AttributeMapping
import io.github.legion2.tosca_orchestrator.tosca.definitions.Entity
import io.github.legion2.tosca_orchestrator.tosca.model.property.ResolvedPropertyNameOrIndex
import io.github.legion2.tosca_orchestrator.tosca.model.refineOrAdd

data class ResolvedAttributeMapping(
    val entity: ResolvedEntity,
    val optional_capability_name: String? = null,
    val attribute_name: List<ResolvedPropertyNameOrIndex>
) {
    companion object {
        fun from(mapping: AttributeMapping): ResolvedAttributeMapping {
            val resolvedEntity = when (mapping.entity) {
                Entity.SELF -> ResolvedEntity.SELF
                Entity.SOURCE -> ResolvedEntity.SOURCE
                Entity.TARGET -> ResolvedEntity.TARGET
            }

            return ResolvedAttributeMapping(
                resolvedEntity,
                mapping.optional_capability_name,
                ResolvedPropertyNameOrIndex.from(mapping.attribute_name)
            )
        }

        fun Map<String, ResolvedAttributeMapping>.refine(other: Map<String, AttributeMapping>): Map<String, ResolvedAttributeMapping> {
            return this.refineOrAdd(other, { from(it) }, { from(it) })//TODO
        }
    }
}

enum class ResolvedEntity {
    SELF,
    SOURCE,
    TARGET
}
