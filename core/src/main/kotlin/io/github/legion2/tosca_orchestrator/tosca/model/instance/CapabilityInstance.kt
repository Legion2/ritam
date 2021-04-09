package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.definitions.CapabilityAssignment
import io.github.legion2.tosca_orchestrator.tosca.definitions.Range
import io.github.legion2.tosca_orchestrator.tosca.definitions.refine
import io.github.legion2.tosca_orchestrator.tosca.model.TypeReference
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedCapability
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedCapabilityType
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedNodeType

data class CapabilityInstance(
    val type: ResolvedCapabilityType,
    val properties: Map<String, PropertyInstance>,
    val attributes: Map<String, AttributeInstance>,
    val validSourceTypes: List<TypeReference<ResolvedNodeType>>,
    val occurrences: Range
) {
    companion object {
        fun validate(
            capabilityAssignments: Map<String, CapabilityAssignment>,
            capabilities: Map<String, ResolvedCapability>,
            evaluationContext: ExpressionResolverContext
        ): Map<String, CapabilityInstance> {
            return capabilities.combine(capabilityAssignments) { name, capability, capabilityAssignment ->
                capability
                    ?: throw IllegalArgumentException("Capability Assignment must have matching Capability definition: $name")
                val properties = PropertyInstance.validate(
                    capabilityAssignment?.properties.orEmpty(),
                    capability.properties,
                    evaluationContext
                )
                val attributes = AttributeInstance.validate(
                    capabilityAssignment?.attributes.orEmpty(),
                    capability.attributes,
                    evaluationContext,
                    properties
                )
                val validSourceTypes = capability.validSourceTypes
                val occurrences = capability.occurrences.refine(capabilityAssignment?.occurrences)
                CapabilityInstance(capability.type, properties, attributes, validSourceTypes, occurrences)
            }
        }
    }
}
