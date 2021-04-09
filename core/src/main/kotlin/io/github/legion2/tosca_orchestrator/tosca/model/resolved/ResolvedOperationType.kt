package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.OperationTypeDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.refineOrAdd
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping.Companion.refine

data class ResolvedOperationType(
    val inputs: Map<String, ResolvedProperty>,
    val outputs: Map<String, ResolvedAttributeMapping>
) {
    companion object {
        fun from(
            operationTypeDefinition: OperationTypeDefinition,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedOperationType {
            val inputs = operationTypeDefinition.inputs.orEmpty()
                .mapValues { ResolvedProperty.from(it.value.propertyDefinition, propertyResolverContext) }
            val outputs =
                operationTypeDefinition.outputs.orEmpty().mapValues { ResolvedAttributeMapping.from(it.value) }
            return ResolvedOperationType(inputs, outputs)
        }

        fun Map<String, ResolvedOperationType>.refine(
            operations: Map<String, OperationTypeDefinition>,
            propertyResolverContext: PropertyResolverContext
        ): Map<String, ResolvedOperationType> {
            return refineOrAdd(
                operations,
                { refine(it, propertyResolverContext) },
                { from(it, propertyResolverContext) })
        }

        private fun ResolvedOperationType.refine(
            operationTypeDefinition: OperationTypeDefinition,
            propertyResolverContext: PropertyResolverContext
        ): ResolvedOperationType {
            val inputs =
                inputs.refine(
                    operationTypeDefinition.inputs.orEmpty().mapValues { it.value.propertyDefinition },
                    propertyResolverContext
                )
            val outputs = outputs.refine(operationTypeDefinition.outputs.orEmpty())
            return ResolvedOperationType(inputs, outputs)
        }
    }
}
