package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.definitions.OperationAssignment
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.OperationResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedAttributeMapping.Companion.refine
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperation
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperationImplementation
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedOperationImplementation.Companion.refine

data class OperationInstance(
    val implementation: ResolvedOperationImplementation,
    val inputs: Map<String, PropertyInstance>,
    val outputs: Map<String, ResolvedAttributeMapping>
) {
    companion object {
        fun validate(
            operationAssignments: Map<String, OperationAssignment>,
            operations: Map<String, ResolvedOperation>,
            evaluationContext: ExpressionResolverContext,
            operationResolverContext: OperationResolverContext
        ): Map<String, OperationInstance> {
            return operations.combine(operationAssignments) { name, operation, operationAssignment ->
                operation
                    ?: throw IllegalArgumentException("Operation Assignments must have matching Operation definition: $name")
                val implementation =
                    operation.implementation.refine(operationAssignment?.implementation, operationResolverContext)
                val inputs = PropertyInstance.validate(
                    operationAssignment?.inputs.orEmpty(),
                    operation.inputs,
                    evaluationContext
                )
                val outputs =
                    operation.outputs.refine(operationAssignment?.outputs.orEmpty())

                OperationInstance(implementation, inputs, outputs)
            }
        }
    }
}
