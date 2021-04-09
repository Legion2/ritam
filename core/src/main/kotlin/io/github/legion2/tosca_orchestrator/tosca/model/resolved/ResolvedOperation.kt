package io.github.legion2.tosca_orchestrator.tosca.model.resolved

import io.github.legion2.tosca_orchestrator.tosca.definitions.OperationDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.refineExisting
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping.Companion.refine
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperationImplementation.Companion.refine

data class ResolvedOperation(
    val implementation: ResolvedOperationImplementation,
    val inputs: Map<String, ResolvedProperty>,
    val outputs: Map<String, ResolvedAttributeMapping>
)

private fun ResolvedOperation.refine(
    operationDefinition: OperationDefinition,
    propertyResolverContext: PropertyResolverContext,
    operationResolverContext: OperationResolverContext
): ResolvedOperation {
    val resolvedOperationImplementation =
        implementation.refine(operationDefinition.implementation, operationResolverContext)
    val inputs = inputs.refine(
        operationDefinition.inputs.orEmpty().mapValues { it.value.propertyDefinition },
        propertyResolverContext
    )
    val outputs = outputs.refine(operationDefinition.outputs.orEmpty())

    return ResolvedOperation(resolvedOperationImplementation, inputs, outputs)
}

fun Map<String, ResolvedOperation>.refine(
    operations: Map<String, OperationDefinition>,
    propertyResolverContext: PropertyResolverContext,
    operationResolverContext: OperationResolverContext
): Map<String, ResolvedOperation> {
    return refineExisting(operations) { operationDefinition ->
        refine(
            operationDefinition,
            propertyResolverContext,
            operationResolverContext
        )
    }
}

fun Map<String, ResolvedOperationType>.addImplementation(
    operations: Map<String, OperationDefinition>,
    propertyResolverContext: PropertyResolverContext,
    operationResolverContext: OperationResolverContext
): Map<String, ResolvedOperation> {
    return combine(operations) { name, operationType, operationDefinition ->
        operationType ?: throw IllegalArgumentException("Operations must be defined in the interface type: $name")
        val implementation =
            ResolvedOperationImplementation.from(operationDefinition?.implementation, operationResolverContext)
        val inputs = operationType.inputs.refine(
            operationDefinition?.inputs.orEmpty().mapValues { it.value.propertyDefinition },
            propertyResolverContext
        )
        val outputs = operationType.outputs.refine(operationDefinition?.outputs.orEmpty())

        ResolvedOperation(implementation, inputs, outputs)
    }
}

