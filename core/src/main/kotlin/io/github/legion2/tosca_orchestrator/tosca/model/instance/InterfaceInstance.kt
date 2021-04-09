package io.github.legion2.tosca_orchestrator.tosca.model.instance

import io.github.legion2.tosca_orchestrator.tosca.definitions.InterfaceAssignment
import io.github.legion2.tosca_orchestrator.tosca.definitions.NotificationDefinition
import io.github.legion2.tosca_orchestrator.tosca.model.combine
import io.github.legion2.tosca_orchestrator.tosca.model.property.ExpressionResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.OperationResolverContext
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.ResolvedInterface
import io.github.legion2.tosca_orchestrator.tosca.model.resolved.type.ResolvedInterfaceType

data class InterfaceInstance(
    val name: String,
    val type: ResolvedInterfaceType,
    val inputs: Map<String, PropertyInstance> = emptyMap(),
    val operations: Map<String, OperationInstance> = emptyMap(),
    val notifications: Map<String, NotificationDefinition> = emptyMap()
) {
    companion object {
        fun validate(
            interfaceAssignments: Map<String, InterfaceAssignment>,
            interfaces: Map<String, ResolvedInterface>,
            expressionResolverContext: ExpressionResolverContext,
            operationResolverContext: OperationResolverContext
        ): Map<String, InterfaceInstance> {
            return interfaces.combine(interfaceAssignments) { name, resolvedInterface, interfaceAssignment ->
                resolvedInterface
                    ?: throw IllegalArgumentException("Interface Assignments must have matching Interface definitions: $name")
                validate(
                    name,
                    interfaceAssignment,
                    resolvedInterface,
                    expressionResolverContext,
                    operationResolverContext
                )
            }
        }

        private fun validate(
            name: String,
            interfaceAssignment: InterfaceAssignment?,
            resolvedInterface: ResolvedInterface,
            expressionResolverContext: ExpressionResolverContext,
            operationResolverContext: OperationResolverContext
        ): InterfaceInstance {
            if (!interfaceAssignment?.deprecatedOperationsSyntax.isNullOrEmpty()) {
                throw IllegalArgumentException("Deprecated Interface assignment without 'operations' keyname is not supported: $interfaceAssignment")
            }

            val inputs = PropertyInstance.validate(
                interfaceAssignment?.inputs.orEmpty(),
                resolvedInterface.inputs,
                expressionResolverContext
            )
            val operations = OperationInstance.validate(
                interfaceAssignment?.operations.orEmpty(),
                resolvedInterface.operations,
                expressionResolverContext,
                operationResolverContext
            )
            return InterfaceInstance(name, resolvedInterface.type, inputs, operations, resolvedInterface.notifications)
        }
    }
}
